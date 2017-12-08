package io.quartz.env

import io.quartz.analyze.analyze
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
    val instances = ast
            .mapNotNull { (a, b, c) -> (c as? DeclT.Instance)?.let { Context(a, b, it) } }
            .groupBy { it.value.instance.qualify(it.qualifier) }
    val values = ast
            .mapNotNull { (a, b, c) -> (c as? DeclT.Value)?.let { Context(a, b, it) } }
            .associateBy { it.value.name.qualify(it.qualifier) }
    val it: Env = object : Env {
        override fun getType(name: QualifiedName) = this@sourceEnv.getType(name)

        override fun getTrait(name: QualifiedName) = traits[name]?.let { (qualifier, imports, value) ->
            val env = import(imports, qualifier)
            value.analyze(env, qualifier).flatMap { ir ->
                generator.generate(Context(qualifier, imports, ir))
                ir.right()
            }
        } ?: this@sourceEnv.getTrait(name)

        override fun getValue(name: QualifiedName) = values[name]?.let { (qualifier, imports, value) ->
            val env = import(imports, qualifier)
            value.analyze(env, qualifier).flatMap { ir ->
                generator.generate(Context(qualifier, imports, ir))
                ir.right()
            }
        } ?: this@sourceEnv.getValue(name)

        override fun getInstances(name: QualifiedName) = instances[name]?.let { asts ->
            asts.asSequence().map { (qualifier, imports, value) ->
                val env = import(imports, qualifier)
                value.analyze(env, qualifier).flatMap { ir ->
                    generator.generate(Context(qualifier, imports, ir))
                    ir.right()
                }
            }
        } ?: emptySequence()
    }
    yields(it)
}.ev()
