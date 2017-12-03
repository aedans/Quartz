package io.quartz.err

import io.quartz.*
import io.quartz.tree.util.*
import kategory.left

data class CompilerError(
        val messageF: () -> String,
        val cause: Throwable?,
        override val location: Location?
) : Locatable {
    override fun toString() = "CompilerError \"${messageF()}\""
}

fun err(cause: Throwable? = null, location: Location? = null, message: () -> String) =
        CompilerError(message, cause, location).singletonList().left()

fun <T> Result<T>.qualify(locatable: Locatable) = mapErr {
    it.map { error ->
        if (error.location == null) error.copy(location = locatable.location) else error
    }
}
