package io.quartz.cli

import com.xenomachina.argparser.*
import io.quartz.nil
import io.quartz.target.*
import java.io.File

class Options(vararg args: String) {
    private val parser = ArgParser(args)

    val src by parser.positionalList(
            "SOURCE",
            help = "source files"
    ) { File(this) }

    val target by parser.storing(
            "-t", "--target",
            help = "specify the language target ${targets.keys}"
    ) { targets[this] ?: throw Exception("Could not find target $this") }
            .default(JvmTarget)

    val options by parser.storing(
            "-x", "--options",
            help = "specify target-specific options"
    ) { split(";") }
            .default(nil)

    val sp by parser.storing(
            "-s", "--sourcepath",
            help = "specify where to find input source files"
    ) { split(';').map(::File) }
            .default(listOf(File(".")))

    val bp by parser.storing(
            "-b", "--binarypath",
            help = "specify where to find binary files"
    ) { split(';').map(::File) }
            .default(listOf(File(".")))

    val out by parser.storing(
            "-o", "--out",
            help = "specify where to place generated files"
    ) { File(this) }
            .default(File("."))
}
