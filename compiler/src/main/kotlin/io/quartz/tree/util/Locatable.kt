package io.quartz.tree.util

import io.quartz.err.*

interface Locatable {
    val location: Location?

    fun <T> Result<T>.qualify() = qualify(this@Locatable)
}
