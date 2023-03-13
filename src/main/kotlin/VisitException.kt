import org.tabooproject.reflex.ClassField
import org.tabooproject.reflex.ClassMethod

class VisitException : RuntimeException {

    constructor(clazz: Class<*>, cause: Throwable) : super(clazz.toString(), cause)

    constructor(clazz: Class<*>, group: VisitorGroup, stage: LifeCycle?, cause: Throwable) : super("$clazz: $group ($stage)", cause)

    constructor(
        clazz: Class<*>,
        group: VisitorGroup,
        stage: LifeCycle?,
        field: ClassField,
        cause: Throwable
    ) : super("$clazz#${field.name}: $group ($stage)", cause)

    constructor(
        clazz: Class<*>,
        group: VisitorGroup,
        stage: LifeCycle?,
        method: ClassMethod,
        cause: Throwable
    ) : super("$clazz#${method.name}: $group ($stage)", cause)

}