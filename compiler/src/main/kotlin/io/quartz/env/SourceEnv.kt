package io.quartz.env

import io.quartz.analyze.*
import io.quartz.err.resultMonad
import io.quartz.gen.Generator
import io.quartz.getFiles
import io.quartz.parse.parse
import io.quartz.tree.ast.DeclT
import io.quartz.tree.util.*
import kategory.*
import java.io.File

fun Env.sourceEnv(sp: List<File>, generator: Generator) = resultMonad().binding {
    val ast = sp.getFiles().parse().bind()
    val traits = ast
            .mapNotNull { (a, b, c) -> (c as? DeclT.Trait)?.let { Context(a, b, it) } }
            .associateBy { it.value.name.qualify(it.qualifier) }
    val vars = ast
            .mapNotNull { (a, b, c) -> (c as? DeclT.Value)?.let { Context(a, b, it) } }
            .associateBy { it.value.name.qualify(it.qualifier) }
    val it: Env = object : Env {
        override fun getType(name: QualifiedName) = traits[name]?.let { ast ->
            val env = import(ast.imports, ast.qualifier)
            ast.analyze(this).flatMap { ir ->
                generator.generate(ir)
                ast.value.schemeK(env, ast.qualifier)
                        .map { TypeInfo(it) }
            }
        } ?: this@sourceEnv.getType(name)

        override fun getVar(name: QualifiedName) = vars[name]?.let { ast ->
            val env = import(ast.imports, ast.qualifier)
            ast.analyze(this).flatMap { ir ->
                generator.generate(ir)
                ast.value.schemeK(env)
                        .map { VarInfo(it) }
            }
        } ?: this@sourceEnv.getVar(name)
    }
    yields(it)
}.ev()
