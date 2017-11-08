package io.quartz.interop

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.quartz.analyze.Env
import io.quartz.analyze.analyze
import io.quartz.analyze.import
import io.quartz.err.Err
import io.quartz.err.errMonad
import io.quartz.gen.asm.ProgramGenerator
import io.quartz.gen.generate
import io.quartz.parse.QuartzGrammar
import io.quartz.parse.fileT
import kategory.binding
import kategory.ev
import java.io.File

fun Env.withSource(files: List<File>, pg: ProgramGenerator) = errMonad().binding {
    yields(files.fold(this@withSource) { a, b -> a.withSource(b, pg).bind() })
}.ev()

fun Env.withSource(file: File, pg: ProgramGenerator): Err<Env> = errMonad().binding {
    val it = if (!file.isDirectory) {
        val grammar = QuartzGrammar.create(file.name) { fileT }
        val fileT = grammar.parseToEnd(file.reader())
        val localEnv = import(fileT.imports)
        fileT.decls.fold(localEnv) { env, decl ->
            decl.analyze(env, fileT.`package`, false).let { it ->
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
