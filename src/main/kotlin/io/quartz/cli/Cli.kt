package io.quartz.cli

import io.quartz.analyzer.MemLoc
import io.quartz.analyzer.analyze
import io.quartz.analyzer.env
import io.quartz.analyzer.type.schemeK
import io.quartz.analyzer.schemeK
import io.quartz.generator.asm.ProgramGenerator
import io.quartz.generator.generate
import io.quartz.interop.classPath
import io.quartz.interop.sourcePath
import io.quartz.parser.QuartzGrammar
import io.quartz.parser.programT
import io.quartz.tree.ast.DeclT
import java.io.File

/**
 * @author Aedan Smith
 */

fun main(args: Array<String>) {
    val file = File(args[0]).absoluteFile
    val classPath = "${file.path};./std/kobaltBuild/classes".classPath()
    val sourcePath = file.path.sourcePath()
    val env = env(
            { (sourcePath.getDecl(it) as? DeclT.Class)?.schemeK ?: classPath.getClass(it)?.schemeK },
            { (sourcePath.getDecl(it) as? DeclT.Value)?.schemeK(this) },
            { (sourcePath.getDecl(it) as? DeclT.Value)?.let { MemLoc.Global(it.name) } }
    )
    file.listFiles().filter { it.extension == "qz" }.forEach {
        val grammar = QuartzGrammar.create(it.name) { programT }
        val ast = grammar.parse(it.reader())
        val ir = ast.analyze(env)
        ir.generate(ProgramGenerator {
            File(file, "${it.info.name}.class").writeBytes(it.toByteArray())
        })
    }
}
