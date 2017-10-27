package io.quartz.interop

import io.quartz.parser.QuartzGrammar
import io.quartz.parser.fileT
import io.quartz.tree.ast.DeclT
import java.io.File

/**
 * @author Aedan Smith
 */

interface SourcePath {
    fun getDecl(name: String): DeclT?
}

fun List<File>.sourcePath() = object : SourcePath {
    val decls = this@sourcePath.flatMap {
        it.listFiles()
                .filter { it.extension == "qz" }
                .flatMap {
                    QuartzGrammar.create(it.name) { fileT }
                            .parse(it.bufferedReader())
                            .decls
                }
    }.associateBy { it.name }

    override fun getDecl(name: String) = decls[name]
}
