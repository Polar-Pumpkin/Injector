import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class VisitorGroup(private val priority: Byte) {

    private val visitors: MutableList<Visitor> = CopyOnWriteArrayList()

    fun getAll(): MutableList<Visitor> = visitors

    operator fun get(stage: LifeCycle?): List<Visitor> {
        val visitors = LinkedList<Visitor>()
        for (visitor in this.visitors) {
            if (stage == null || stage == visitor.getLifeCycle()) {
                visitors.add(visitor)
            }
        }
        return visitors
    }

    fun getPriority(): Byte = priority

    override fun toString(): String {
        return "VisitorGroup(priority=$priority, visitors=$visitors)"
    }

}