import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.tabooproject.reflex.ReflexClass
import scanner.Scanner
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Supplier

@Suppress("MemberVisibilityCanBePrivate", "unused")
class ProjectFactory(val scanner: Scanner) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val awoken: ConcurrentMap<String, Any> = ConcurrentHashMap()
    val visitors: VisitorHandler by lazy { VisitorHandler(this) }
    val runningClasses: List<Class<*>> by lazy { scanner.scan() }

    fun init() {
        var timestamp = System.currentTimeMillis()
        runningClasses.parallelStream().forEach {
            if (it.isAnnotationPresent(Awake::class.java)) {
                val instance = getInstance(it, true)?.get() ?: return@forEach
                if (Visitor::class.java.isAssignableFrom(it)) {
                    visitors.register(instance as Visitor)
                }
                awoken[it.name] = instance
            }
        }
        logger.info("自唤醒完成({}ms)", System.currentTimeMillis() - timestamp)

        timestamp = System.currentTimeMillis()
        visitors.injectAll(LifeCycle.CONST)
        logger.info("CONST 注入完成({}ms)", System.currentTimeMillis() - timestamp)
    }

    /**
     * 取该类在当前项目中被加载的任何实例
     * 例如：@Awake 自唤醒类，或是 Kotlin Companion Object、Kotlin Object 对象
     *
     * @param newInstance 若无任何已加载的实例，是否实例化
     */
    @Suppress("UNCHECKED_CAST")
    internal fun <T> getInstance(clazz: Class<T>, newInstance: Boolean = false): Supplier<T>? {
        // 是否为自唤醒类
        try {
            val awoken = awoken[clazz.name] as? T
            if (awoken != null) {
                return Supplier { awoken }
            }
        } catch (ex: ClassNotFoundException) {
            return null
        } catch (ex: NoClassDefFoundError) {
            return null
        } catch (ex: InternalError) {
            println(this)
            ex.printStackTrace()
            return null
        }

        return try {
            // 获取 Kotlin Companion 字段
            val field = if (clazz.simpleName == "Companion") {
                val companion = Class.forName(clazz.name.substringBeforeLast('$'), false, ProjectFactory::class.java.classLoader)
                ReflexClass.of(companion).getField("Companion", findToParent = false, remap = false)
            }
            // 获取 Kotlin Object 字段
            else {
                ReflexClass.of(clazz).getField("INSTANCE", findToParent = false, remap = false)
            }
            CachedSupplier { field.get() as T }
        } catch (ex: NoSuchFieldException) {
            // 是否创建实例
            if (newInstance) CachedSupplier { clazz.getDeclaredConstructor().newInstance() as T } else null
        } catch (ex: NoClassDefFoundError) {
            null
        } catch (ex: ClassNotFoundException) {
            null
        } catch (ex: IllegalAccessError) {
            null
        } catch (ex: IncompatibleClassChangeError) {
            null
        } catch (ex: ExceptionInInitializerError) {
            println(this)
            ex.printStackTrace()
            null
        } catch (ex: InternalError) {
            // 非常奇怪的错误
            if (ex.message != "Malformed class name") {
                println(this)
                ex.printStackTrace()
            }
            null
        }
    }

    private class CachedSupplier<T>(supplier: Supplier<T>) : Supplier<T> {
        private val cached by lazy { supplier.get() }
        override fun get(): T = cached
    }

}