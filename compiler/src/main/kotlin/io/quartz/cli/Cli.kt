package io.quartz.cli

import com.xenomachina.argparser.ShowHelpException
import io.quartz.analyze.analyze
import io.quartz.env.sourceEnv
import io.quartz.err.*
import io.quartz.mapErr
import io.quartz.parse.parse
import kategory.*

object Cli {
    @JvmStatic
    fun main(vararg args: String) {
        try {
            resultMonad().binding {
                val options = Options(*args)
                val target = options.target
                val generator = target.generator(options.out)
                val env = target.env(options.bp).sourceEnv(options.sp, generator).bind()
                val ast = options.src.parse().bind()
                val ir = ast.analyze(env).bind()
                ir.forEach { generator.generate(it) }
                yields(Unit)
            }.ev().mapErr { it.write() }
        } catch (e: ShowHelpException) {
            val writer = System.out.writer()
            e.printUserMessage(writer, null, 80)
            writer.flush()
        }
    }
}
