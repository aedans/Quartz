package io.quartz.interop

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.quartz.parser.QuartzGrammar
import io.quartz.parser.fileT
import io.quartz.tree.QualifiedName
import io.quartz.tree.ast.DeclT
import io.quartz.tree.qualify
import java.io.File

interface SourcePath {
    fun getDecl(name: QualifiedName): DeclT?
}

fun List<File>.sourcePath() = object : SourcePath {
    val decls = this@sourcePath.flatMap { it.decls() }.toMap()

    override fun getDecl(name: QualifiedName) = decls[name]
}

fun File.decls(): List<Pair<QualifiedName, DeclT>> = if (isFile) {
    val grammar = QuartzGrammar.create(name) { fileT }
    val file = grammar.parseToEnd(reader())
    file.decls.map { it.name.qualify(file.`package`) to it }
} else {
    listFiles()
            .filter { it.extension == "qz" }
            .flatMap { it.decls() }
}
