package io.quartz

import kategory.*

/** The empty list, used for pattern matching */
val nil = emptyList<Nothing>()

inline fun <A, B, S> Iterable<A>.foldMap(s: S, fn: (S, A) -> Tuple2<S, B>) =
        fold(s toT emptyList<B>()) { (s, bs), a ->
            fn(s, a).let { (ns, nb) -> ns toT bs + nb }
        }

fun Either<*, *>.foldString() = fold({ it.toString() }, { it.toString() })

fun <A> A.singletonList() = listOf(this)

fun <A, B, C> Pair<Pair<A, B>, C>.tup() = Tuple3(first.first, first.second, second)
fun <A, B, C, D> Pair<Pair<Pair<A, B>, C>, D>.tup() = Tuple4(first.first.first, first.first.second, first.second, second)
