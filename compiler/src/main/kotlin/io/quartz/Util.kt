package io.quartz

import kategory.Either
import kategory.Tuple2
import kategory.toT

/** The empty list, used for pattern matching */
val nil = emptyList<Nothing>()

inline fun <A, B, S> Iterable<A>.foldMap(s: S, fn: (S, A) -> Tuple2<S, B>) =
        fold(s toT emptyList<B>()) { (s, bs), a ->
            fn(s, a).let { (ns, nb) -> ns toT bs + nb }
        }

fun Either<*, *>.foldString() = fold({ it.toString() }, { it.toString() })
