package io.quartz.err

fun List<CompilerError>.write() = forEach { it.write() }

fun CompilerError.write() {
    println(messageF())
    location?.let { println("at $it") }
    cause?.let {
        print("Caused by ")
        it.printStackTrace(System.out)
    }
    println()
}
