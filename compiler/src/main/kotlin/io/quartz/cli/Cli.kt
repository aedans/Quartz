package io.quartz.cli

import com.xenomachina.argparser.ShowHelpException
import io.quartz.analyze.analyze
import io.quartz.analyze.compose
import io.quartz.analyze.emptyEnv
import io.quartz.analyze.import
import io.quartz.err.flat
import io.quartz.err.resultMonad
import io.quartz.err.write
import io.quartz.foldMap
import io.quartz.gen.asm.ProgramGenerator
import io.quartz.gen.generate
import io.quartz.interop.ClassPathEnv
import io.quartz.interop.classPath
import io.quartz.interop.withSource
import io.quartz.parse.QuartzGrammar
import io.quartz.parse.fileT
import kategory.binding
import kategory.ev
import org.funktionale.memoization.memoize
import java.io.File

/** The main entry point for the Quartz compiler */
object Cli {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val options = Options(args)

            val pg = ProgramGenerator {
                val locatableName = it.info.name
                File(options.out, "$locatableName.class")
                        .also { it.parentFile.mkdirs() }
                        .writeBytes(it.cw.toByteArray())
            }

            val ir = resultMonad().binding {
                val globalEnv = (emptyEnv compose { ClassPathEnv(options.cp.classPath()) }.memoize())
                        .withSource(options.sp, pg).bind()

                val it = options.src.flatMap {
                    val grammar = QuartzGrammar.create(it.name) { fileT }
                    val fileT = grammar.parse(it.reader())
                    val localEnv = globalEnv.import(fileT)
                    fileT.decls
                            .foldMap(localEnv) { env, decl -> decl.analyze(env, fileT.`package`) }.b
                            .flat()
                            .bind()
                }
                yields(it)
            }.ev()

            ir.bimap(
                    { it.write() },
                    { it.generate(pg) }
            )
        } catch (e: ShowHelpException) {
            val writer = System.out.writer()
            e.printUserMessage(writer, null, 80)
            writer.flush()
        }
    }
}
