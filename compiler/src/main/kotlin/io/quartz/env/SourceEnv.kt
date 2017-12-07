package io.quartz.env

import io.quartz.analyze.*
import io.quartz.analyze.tree.DeclK
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
    val instances = ast
            .mapNotNull { (a, b, c) -> (c as? DeclT.Instance)?.let { Context(a, b, it) } }
            .groupBy { it.value.instance.qualify(it.qualifier) }
    val it: Env = object : Env {
        override fun getType(name: QualifiedName) = this@sourceEnv.getType(name)

        override fun getTrait(name: QualifiedName) = traits[name]?.let { ast ->
            val env = import(ast.imports, ast.qualifier)
            ast.analyze(this).flatMap { ir ->
                generator.generate(ir)
                resultMonad().binding {
                    val qualifiedName = ast.value.name.qualify(ast.qualifier)
                    yields(DeclK.Trait(qualifiedName))
                }.ev()
            }
        } ?: this@sourceEnv.getTrait(name)

        override fun getValue(name: QualifiedName) = vars[name]?.let { ast ->
            val env = import(ast.imports, ast.qualifier)
            ast.analyze(this).flatMap { ir ->
                generator.generate(ir)
                resultMonad().binding {
                    val schemeK = ast.value.schemeK(env, ast.qualifier).bind()
                    yields(DeclK.Value(schemeK))
                }.ev()
            }
        } ?: this@sourceEnv.getValue(name)

        override fun getInstances(name: QualifiedName) = instances[name]?.let { asts ->
            asts.asSequence().map { ast ->
                val env = import(ast.imports, ast.qualifier)
                ast.analyze(this).flatMap { ir ->
                    generator.generate(ir)
                    resultMonad().binding {
                        val traitK = env.getTraitOrErr(ast.value.instance.qualifiedLocal).bind()
                        val instance = traitK.qualifiedName
                        yields(DeclK.Instance(instance))
                    }.ev()
                }
            }
        } ?: emptySequence()
    }
    yields(it)
}.ev()
