package io.quartz.err

fun List<CompilerError>.write() = forEach { it.write() }

fun CompilerError.write() {
    println(messageF())
    println("at $location")
    println()
}
