package io.quartz.env

import io.quartz.analyze.tree.*
import io.quartz.err.*
import io.quartz.foldString
import io.quartz.tree.util.*

interface Env {
    fun getType(name: QualifiedName): Result<TypeK>?
    fun getVar(name: QualifiedName): Result<SchemeK>?
    fun getTrait(name: QualifiedName): Result<DeclK.Trait>?
    fun getInstances(name: QualifiedName): Sequence<Result<DeclK.Instance>>
}

fun Env.getTypeOrErr(name: QualifiedName) = getType(name) ?: err { "could not find instance $name" }
fun Env.getVarOrErr(name: QualifiedName) = getVar(name) ?: err { "could not find var $name" }

fun Env.mapTypes(map: (QualifiedName, Result<TypeK>?) -> Result<TypeK>?) = object : Env by this {
    override fun getType(name: QualifiedName) = map(name, this@mapTypes.getType(name))
    override fun toString() = "${this@mapTypes} mapType"
}

fun Env.mapVars(map: (QualifiedName, Result<SchemeK>?) -> Result<SchemeK>?) = object : Env by this {
    override fun getVar(name: QualifiedName) = map(name, this@mapVars.getVar(name))
    override fun toString() = "${this@mapVars} mapVars"
}

fun Env.withType(name: QualifiedName, type: () -> Result<TypeK>?) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getType(name: QualifiedName) = if (name == _name) type() else this@withType.getType(name)
        override fun toString() = "${this@withType} withType ${name to type()?.foldString()}"
    }
}

fun Env.withVar(name: QualifiedName, value: () -> Result<SchemeK>?) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getVar(name: QualifiedName) = if (name == _name) value() else this@withVar.getVar(name)
        override fun toString() = "${this@withVar} withVar ${name to value()?.foldString()}"
    }
}

private var acc = 0
fun fresh() = "_${acc++}".name
