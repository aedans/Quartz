package io.quartz.interop

import io.quartz.parser.QuartzGrammar
import io.quartz.parser.programT
import io.quartz.tree.ast.DeclT
import java.io.File

/**
 * @author Aedan Smith
 */

interface SourcePath {
    fun getDecl(name: String): DeclT?
}

fun String.sourcePath() = split(';').toList().map { File(it) }.sourcePath()
fun List<File>.sourcePath() = object : SourcePath {
    val decls = this@sourcePath.flatMap {
        it.listFiles()
                .filter { it.extension == "qz" }
                .flatMap {
                    QuartzGrammar.create(it.name) { programT }
                            .parse(it.bufferedReader())
                }
    }.associateBy { it.name }

    override fun getDecl(name: String) = decls[name]
}
