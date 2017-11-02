package io.quartz.interop

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.quartz.analyzer.*
import io.quartz.generator.asm.ProgramGenerator
import io.quartz.generator.generate
import io.quartz.parser.QuartzGrammar
import io.quartz.parser.fileT
import kategory.Either
import kategory.binding
import kategory.ev
import java.io.File

fun Env.withSource(files: List<File>, pg: ProgramGenerator) = Either.monadErrorE().binding {
    yields(files.fold(this@withSource) { a, b -> a.withSource(b, pg).bind() })
}.ev()

fun Env.withSource(file: File, pg: ProgramGenerator): EitherE<Env> = Either.monadErrorE().binding {
    val it = if (!file.isDirectory) {
        val grammar = QuartzGrammar.create(file.name) { fileT }
        val fileT = grammar.parseToEnd(file.reader())
        val localEnv = import(fileT.imports).bind()
        fileT.decls.fold(localEnv) { env, decl ->
            decl.analyze(env, fileT.`package`).let {
                it.b.map { it.generate(pg) }
                it.a
            }
        }
    } else {
        file
                .listFiles()
                .filter { it.isDirectory || it.extension == "qz" }
                .fold(this@withSource) { a, b -> a.withSource(b, pg).bind() }
    }
    yields(it)
}.ev()
