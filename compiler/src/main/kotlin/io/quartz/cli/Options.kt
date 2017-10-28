package io.quartz.cli

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File

/**
 * @author Aedan Smith
 */

class Options(args: Array<out String>) {
    val parser = ArgParser(args)

    val src by parser.positionalList(
            "SOURCE",
            help = "source files"
    ) { File(this) }

    val cp by parser.storing(
            "-c", "--classpath",
            help = "specify where to find user class files"
    ) { split(';').map(::File) }
            .default(listOf(File(".")))

    val sp by parser.storing(
            "-s", "--sourcepath",
            help = "specify where to find input source files"
    ) { split(';').map(::File) }
            .default(listOf(File(".")))

    val out by parser.storing(
            "-o", "--output",
            help = "specify where to place generated class files"
    ) { File(this) }
            .default(File("."))
}
