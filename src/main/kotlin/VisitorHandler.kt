import org.tabooproject.reflex.ReflexClass
import java.util.*
import java.util.function.Supplier

@Suppress("MemberVisibilityCanBePrivate", "unused")
class VisitorHandler(val factory: ProjectFactory) {

    private val visitors: NavigableMap<Byte, VisitorGroup> = Collections.synchronizedNavigableMap(TreeMap())
    private val classes: MutableList<Class<*>> = ArrayList()

    fun register(visitor: Visitor) {
        val group = visitors.computeIfAbsent(visitor.getPriority(), ::VisitorGroup)
        group.getAll().add(visitor)
    }

    fun injectAll(clazz: Class<*>) {
        for (entry in visitors.entries) {
            inject(clazz, entry.value, null)
        }
    }

    fun injectAll(stage: LifeCycle) {
        for (entry in visitors.entries) {
            for (clazz in getClasses()) {
                inject(clazz, entry.value, stage)
            }
        }
    }

    fun inject(clazz: Class<*>, group: VisitorGroup, stage: LifeCycle?) {
        if (clazz.isAnnotationPresent(Ghost::class.java)) {
            return
        }
        if (stage != null && clazz.isAnnotationPresent(SkipTo::class.java)) {
            val skip = clazz.getAnnotation(SkipTo::class.java).value.ordinal
            if (skip > stage.ordinal) {
                return
            }
        }

        val instance = factory.getInstance(clazz, false)
        val reflexClass: ReflexClass
        try {
            reflexClass = ReflexClass.of(clazz, true)
        } catch (ex: Throwable) {
            VisitException(clazz, ex).printStackTrace()
            return
        }
        enter(clazz, group, stage, instance)
        visitField(clazz, group, stage, reflexClass, instance)
        visitMethod(clazz, group, stage, reflexClass, instance)
        exit(clazz, group, stage, instance)
    }

    private fun enter(clazz: Class<*>, group: VisitorGroup, stage: LifeCycle?, instance: Supplier<*>?) {
        for (visitor in group[stage]) {
            try {
                visitor.enter(clazz, instance)
            } catch (ex: Throwable) {
                VisitException(clazz, group, stage, ex).printStackTrace()
            }
        }
    }

    private fun visitField(clazz: Class<*>, group: VisitorGroup, stage: LifeCycle?, reflexClass: ReflexClass, instance: Supplier<*>?) {
        for (visitor in group[stage]) {
            for (field in reflexClass.structure.fields) {
                try {
                    visitor.visit(field, clazz, instance)
                } catch (ex: Throwable) {
                    VisitException(clazz, group, stage, field, ex).printStackTrace()
                }
            }
        }
    }

    private fun visitMethod(clazz: Class<*>, group: VisitorGroup, stage: LifeCycle?, reflexClass: ReflexClass, instance: Supplier<*>?) {
        for (visitor in group[stage]) {
            for (method in reflexClass.structure.methods) {
                try {
                    visitor.visit(method, clazz, instance)
                } catch (ex: Throwable) {
                    VisitException(clazz, group, stage, method, ex).printStackTrace()
                }
            }
        }
    }

    private fun exit(clazz: Class<*>, group: VisitorGroup, stage: LifeCycle?, instance: Supplier<*>?) {
        for (visitor in group[stage]) {
            try {
                visitor.exit(clazz, instance)
            } catch (ex: Throwable) {
                VisitException(clazz, group, stage, ex).printStackTrace()
            }
        }
    }

    private fun getClasses(): List<Class<*>> {
        if (classes.isEmpty()) {
            for (runningClass in factory.runningClasses) {
                // 额外的检查
                classes.add(runningClass)
            }
        }
        return classes
    }

}