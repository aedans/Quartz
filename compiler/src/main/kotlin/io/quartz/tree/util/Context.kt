package io.quartz.tree.util

data class Context<out T>(
        val qualifier: Qualifier,
        val imports: List<Import>,
        val value: T
)
