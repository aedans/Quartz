package io.quartz.cli

import com.xenomachina.argparser.ShowHelpException
import io.quartz.analyze.analyze
import io.quartz.analyze.import
import io.quartz.err.flat
import io.quartz.err.resultMonad
import io.quartz.err.write
import io.quartz.gen.asm.ProgramGenerator
import io.quartz.gen.generate
import io.quartz.interop.GlobalEnv
import io.quartz.interop.classPath
import io.quartz.interop.decls
import io.quartz.interop.sourcePath
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

            resultMonad().binding {
                val globalEnv = GlobalEnv(options.cp.classPath(), options.sp.sourcePath(), pg)

                val it = options.src.decls().map { (p, imports, decl) ->
                    decl.analyze(globalEnv.import(imports), p)
                }.flat().bind()

                yields(it)
            }.ev().bimap(
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
