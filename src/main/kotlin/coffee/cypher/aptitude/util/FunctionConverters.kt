package coffee.cypher.aptitude.util

import com.mojang.datafixers.util.Function3
import java.util.function.BiFunction
import java.util.function.Function

val <T, R> ((T) -> R).codecFunction: Function<T, R>
    get() = Function { this(it) }

val <T1, T2, R> ((T1, T2) -> R).codecFunction: BiFunction<T1, T2, R>
    get() = BiFunction { arg1, arg2 -> this(arg1, arg2) }

val <T1, T2, T3, R> ((T1, T2, T3) -> R).codecFunction: Function3<T1, T2, T3, R>
    get() = Function3 { arg1, arg2, arg3 -> this(arg1, arg2, arg3) }
