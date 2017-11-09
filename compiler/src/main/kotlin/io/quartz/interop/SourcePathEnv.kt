package io.quartz.interop

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.quartz.analyze.Env
import io.quartz.analyze.analyze
import io.quartz.analyze.import
import io.quartz.err.Errs
import io.quartz.err.errsMonad
import io.quartz.gen.asm.ProgramGenerator
import io.quartz.gen.generate
import io.quartz.parse.QuartzGrammar
import io.quartz.parse.fileT
import kategory.binding
import kategory.ev
import java.io.File

fun Env.withSource(files: List<File>, pg: ProgramGenerator) = errsMonad().binding {
    yields(files.fold(this@withSource) { a, b -> a.withSource(b, pg).bind() })
}.ev()

fun Env.withSource(file: File, pg: ProgramGenerator): Errs<Env> = errsMonad().binding {
    val it = if (!file.isDirectory) {
        val grammar = QuartzGrammar.create(file.name) { fileT }
        val fileT = grammar.parseToEnd(file.reader())
        val localEnv = import(fileT)
        fileT.decls.fold(localEnv) { env, decl ->
            decl.analyze(env, fileT.`package`, false).let { it ->
                it.b.bind().generate(pg)
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
