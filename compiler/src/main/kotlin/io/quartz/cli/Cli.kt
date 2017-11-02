package io.quartz.cli

import com.xenomachina.argparser.ShowHelpException
import io.quartz.analyzer.analyze
import io.quartz.analyzer.import
import io.quartz.analyzer.monadErrorE
import io.quartz.foldMap
import io.quartz.generator.asm.ProgramGenerator
import io.quartz.generator.generate
import io.quartz.interop.ClassPathEnv
import io.quartz.interop.classPath
import io.quartz.interop.withSource
import io.quartz.parser.QuartzGrammar
import io.quartz.parser.fileT
import kategory.Either
import kategory.binding
import kategory.ev
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

            val ir = Either.monadErrorE().binding {
                val globalEnv = ClassPathEnv(options.cp.classPath())
                        .withSource(options.sp, pg).bind()

                val it = options.src.flatMap {
                    val grammar = QuartzGrammar.create(it.name) { fileT }
                    val fileT = grammar.parse(it.reader())
                    val localEnv = globalEnv.import(fileT.imports).bind()
                    fileT.decls
                            .foldMap(localEnv) { env, decl -> decl.analyze(env, fileT.`package`) }.b
                            .map { it.bind() }
                }
                yields(it)
            }.ev()

            ir.bimap(
                    { it.printStackTrace() },
                    { it.forEach { it.generate(pg) } }
            )
        } catch (e: ShowHelpException) {
            val writer = System.out.writer()
            e.printUserMessage(writer, null, 80)
            writer.flush()
        }
    }
}
