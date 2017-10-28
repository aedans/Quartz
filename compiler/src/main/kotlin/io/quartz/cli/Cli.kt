package io.quartz.cli

import com.xenomachina.argparser.ShowHelpException
import io.quartz.analyzer.analyze
import io.quartz.generator.asm.ProgramGenerator
import io.quartz.generator.generate
import io.quartz.interop.GlobalEnv
import io.quartz.interop.classPath
import io.quartz.interop.sourcePath
import io.quartz.parser.QuartzGrammar
import io.quartz.parser.fileT
import io.quartz.tree.nil
import java.io.File

/** The main entry point for the Quartz compiler */
object Cli {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val options = Options(args)
            val classPath = options.cp.classPath()
            val sourcePath = options.sp.sourcePath()
            val globalEnv = GlobalEnv(classPath, sourcePath, nil)

            options.src.forEach {
                val grammar = QuartzGrammar.create(it.name) { fileT }
                val ast = grammar.parse(it.reader())
                val ir = ast.analyze(globalEnv)
                ir.generate(ProgramGenerator { File(options.out, "${it.info.name}.class").writeBytes(it.toByteArray()) })
            }
        } catch (e: ShowHelpException) {
            val writer = System.out.writer()
            e.printUserMessage(writer, null, 80)
            writer.flush()
        }
    }
}
