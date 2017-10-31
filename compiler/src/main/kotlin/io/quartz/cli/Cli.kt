package io.quartz.cli

import com.xenomachina.argparser.ShowHelpException
import io.quartz.analyzer.analyze
import io.quartz.analyzer.monadErrorE
import io.quartz.generator.generate
import io.quartz.interop.GlobalEnv
import io.quartz.interop.classPath
import io.quartz.interop.sourcePath
import io.quartz.parser.QuartzGrammar
import io.quartz.parser.fileT
import io.quartz.tree.nil
import kategory.Either
import kategory.binding
import kategory.ev

/** The main entry point for the Quartz compiler */
object Cli {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val options = Options(args)
            val classPath = options.cp.classPath()
            val sourcePath = options.sp.sourcePath()
            val globalEnv = GlobalEnv(classPath, sourcePath, nil)

            val ir = Either.monadErrorE().binding {
                val it = options.src.flatMap {
                    val grammar = QuartzGrammar.create(it.name) { fileT }
                    val ast = grammar.parse(it.reader())
                    ast.analyze(globalEnv).bind()
                }
                yields(it)
            }.ev()

            ir.bimap(
                    {
                        System.err.println(it.message)
                    },
                    {
                        it.generate(options.out)
                    }
            )
        } catch (e: ShowHelpException) {
            val writer = System.out.writer()
            e.printUserMessage(writer, null, 80)
            writer.flush()
        }
    }
}
