@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class SpiderDsl

typealias Handler<T> = suspend (@SpiderDsl T).() -> Unit
typealias ExtraHandler<T, E> = suspend (@SpiderDsl T).(E) -> Unit
