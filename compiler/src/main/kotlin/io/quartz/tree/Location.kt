package io.quartz.tree

import io.quartz.err.Result
import io.quartz.err.qualify

interface Locatable {
    val location: Location

    fun <T> Result<T>.qualify() = qualify(this@Locatable)
}

/** Class containing the source location of a token */
data class Location(
        val uri: String,
        val line: Int,
        val position: Int
) {
    override fun toString() = "$uri:$line:$position"

    companion object {
        /** Location for auto-generated stackTraceElement */
        val unknown = Location("<unknown>", 0, 0)
    }
}
