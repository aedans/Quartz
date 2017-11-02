package io.quartz

import kategory.Tuple2
import kategory.toT

inline fun <A, B, S> Iterable<A>.foldMap(s: S, fn: (S, A) -> Tuple2<S, B>) =
        fold(s toT emptyList<B>()) { (s, bs), a ->
            fn(s, a).let { (ns, nb) -> ns toT bs + nb }
        }
