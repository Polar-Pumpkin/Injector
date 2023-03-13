import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ClassMethod
import java.util.function.Supplier

abstract class Visitor(private val priority: Byte = 0) {

    abstract fun getLifeCycle(): LifeCycle

    fun enter(clazz: Class<*>, instance: Supplier<*>?) {
    }

    fun exit(clazz: Class<*>, instance: Supplier<*>?) {
    }

    fun visit(field: ClassField, clazz: Class<*>, instance: Supplier<*>?) {
    }

    fun visit(method: ClassMethod, clazz: Class<*>, instance: Supplier<*>?) {
    }

    fun getPriority(): Byte = this.priority

}