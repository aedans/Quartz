package io.quartz.err

import io.quartz.nil
import kategory.*

typealias Err<T> = Either<CompilerError, T>

fun errMonad() = Either.monadError<CompilerError>()

typealias Errs<T> = Either<List<CompilerError>, T>

fun errsMonad() = Either.monadError<List<CompilerError>>()

fun <T> Err<T>.errs(): Errs<T> = bimap({ listOf(it) }, ::identity)

/** Returns right if there are no errors, otherwise collects all errors and returns left */
fun <T> List<Errs<T>>.flat(): Errs<List<T>> = fold(emptyList<T>().right()) { a: Errs<List<T>>, b ->
    a.fold(
            { errs -> (errs + b.fold(::identity, { nil })).left() },
            { ts ->
                b.fold(
                        { it.left() },
                        { (ts + it).right() }
                )
            }
    )
}
