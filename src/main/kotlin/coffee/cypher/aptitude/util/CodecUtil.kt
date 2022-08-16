package coffee.cypher.aptitude.util

import com.mojang.datafixers.util.Either
import com.mojang.datafixers.util.Function3
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import java.util.function.BiFunction
import java.util.function.Function

val <T, R> ((T) -> R).codecFunction: Function<T, R>
    get() = Function { this(it) }

val <T1, T2, R> ((T1, T2) -> R).codecFunction: BiFunction<T1, T2, R>
    get() = BiFunction { arg1, arg2 -> this(arg1, arg2) }

val <T1, T2, T3, R> ((T1, T2, T3) -> R).codecFunction: Function3<T1, T2, T3, R>
    get() = Function3 { arg1, arg2, arg3 -> this(arg1, arg2, arg3) }

inline fun <S : Any, reified T : S, reified R : S> Codec<Either<T, R>>.asSuperType(): Codec<S> {
    return flatXmap(
        { either ->
            DataResult.success(
                either.map(
                    { it },
                    { it }
                )
            )

        },
        {
            when (it) {
                is T -> DataResult.success(Either.left(it))
                is R -> DataResult.success(Either.right(it))
                else -> DataResult.error("")
            }
        },
    )
}
