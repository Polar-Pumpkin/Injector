package scanner.impl

import ProjectFactory
import com.google.common.reflect.ClassPath
import scanner.Scanner

@Suppress("MemberVisibilityCanBePrivate", "unused")
class GuavaScanner(val external: Class<*>) : Scanner {
    override fun scan(): List<Class<*>> {
        val domain = external.packageName
        val internal = ProjectFactory::class.java.packageName
        return ClassPath.from(external.classLoader)
            .allClasses
            .stream()
            .filter { it.packageName.startsWith(domain) }
            .filter { !it.packageName.startsWith(internal) }
            .map { it.load() }
            .toList()
    }
}