package io.quartz.err

import io.quartz.nil
import kategory.*

typealias Result<T> = Either<List<CompilerError>, T>

fun resultMonad() = Either.monadError<List<CompilerError>>()

fun <T> List<Result<T>>.flat(): Result<List<T>> = fold(emptyList<T>().right()) { a: Result<List<T>>, b ->
    a.fold(
            { errs -> (errs + b.fold(::identity, { nil })).left() },
            { ts -> b.fold({ it.left() }, { (ts + it).right() }) }
    )
}
