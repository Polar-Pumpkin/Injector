package scanner

fun interface Scanner {
    fun scan(): List<Class<*>>
}