package io.quartz.tree

import io.quartz.err.Err
import io.quartz.err.Errs
import io.quartz.err.qualify
import io.quartz.err.qualifyAll

interface Locatable {
    val location: Location

    fun <T> Err<T>.qualify() = qualify(this@Locatable)
    fun <T> Errs<T>.qualifyAll() = qualifyAll(this@Locatable)
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
