package io.quartz.err

import io.quartz.singletonList
import io.quartz.tree.Locatable
import io.quartz.tree.Location
import kategory.Either
import kategory.identity
import kategory.left

/** Class representing all expected compiler errors */
data class CompilerError(
        val messageF: () -> String,
        val location: Location = Location.unknown
) {
    override fun toString() = "CompilerError(\"${messageF()}\")"
}

fun err(message: () -> String) = CompilerError(message).singletonList().left()

inline fun <A, B, C> Either<A, B>.mapErr(crossinline fn: (A) -> C) = bimap(fn, ::identity)

fun CompilerError.qualify(locatable: Locatable) = when (location) {
    Location.unknown -> copy(location = locatable.location)
    else -> this
}

fun <T> Result<T>.qualify(locatable: Locatable) = mapErr { it.map { it.qualify(locatable) } }
