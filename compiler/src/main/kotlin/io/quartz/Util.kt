package io.quartz

import kategory.*
import java.io.File

val nil = emptyList<Nothing>()

fun Either<*, *>.foldString() = fold({ it.toString() }, { it.toString() })

fun <A> A.singletonList() = listOf(this)

fun <A, B, C> Pair<Pair<A, B>, C>.tup() = Tuple3(first.first, first.second, second)
fun <A, B, C, D> Pair<Pair<Pair<A, B>, C>, D>.tup() = Tuple4(first.first.first, first.first.second, first.second, second)
fun <A, B, C, D, E> Pair<Pair<Pair<Pair<A, B>, C>, D>, E>.tup() = Tuple5(first.first.first.first, first.first.first.second, first.first.second, first.second, second)

inline fun <A, B, C> Either<A, B>.mapErr(crossinline fn: (A) -> C) = bimap(fn, ::identity)
inline fun <A, B> Either<A, B>.flatMapErr(crossinline fn: (A) -> Either<A, B>) = fold({ fn(it) }, { it.right() })

@Suppress("NOTHING_TO_INLINE")
inline fun <T> T.assertComplete() = this

fun List<File>.getFiles(): List<File> = flatMap(File::getFiles)
fun File.getFiles(): List<File> = when {
    isDirectory -> listFiles().flatMap { it.getFiles() }
    isFile -> singletonList()
    else -> throw IllegalStateException()
}
