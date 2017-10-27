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
            help = "Source files"
    ) { File(this) }

    val cp by parser.storing(
            "-c", "--classpath",
            help = "Specify where to find user class files"
    ) { split(';').map(::File) }
            .default(listOf(File(".")))

    val sp by parser.storing(
            "-s", "--sourcepath",
            help = "Specify where to find input source files"
    ) { split(';').map(::File) }
            .default(listOf(File(".")))

    val out by parser.storing(
            "-o", "--output",
            help = "Specify where to place generated class files"
    ) { File(this) }
            .default(File("."))
}
