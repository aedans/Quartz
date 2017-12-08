package io.quartz.env

import io.quartz.err.*
import io.quartz.foldString
import io.quartz.tree.ir.*
import io.quartz.tree.util.*

interface Env {
    fun getType(name: QualifiedName): Result<TypeI>?
    fun getValue(name: QualifiedName): Result<DeclI.Value>?
    fun getTrait(name: QualifiedName): Result<DeclI.Trait>?
    fun getInstances(name: QualifiedName): Sequence<Result<DeclI.Instance>>
}

fun Env.getTypeOrErr(name: QualifiedName) = getType(name) ?: err { "could not find constraint $name" }
fun Env.getValueOrErr(name: QualifiedName) = getValue(name) ?: err { "could not find value $name" }
fun Env.getTraitOrErr(name: QualifiedName) = getTrait(name) ?: err { "could not find trait $name" }

fun Env.mapTypes(map: (QualifiedName, Result<TypeI>?) -> Result<TypeI>?) = object : Env by this {
    override fun getType(name: QualifiedName) = map(name, this@mapTypes.getType(name))
    override fun toString() = "${this@mapTypes} mapType"
}

fun Env.mapValues(map: (QualifiedName, Result<DeclI.Value>?) -> Result<DeclI.Value>?) = object : Env by this {
    override fun getValue(name: QualifiedName) = map(name, this@mapValues.getValue(name))
    override fun toString() = "${this@mapValues} mapValues"
}

fun Env.withType(name: QualifiedName, type: () -> Result<TypeI>?) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getType(name: QualifiedName) = if (name == _name) type() else this@withType.getType(name)
        override fun toString() = "${this@withType} withType ${name to type()?.foldString()}"
    }
}

fun Env.withValue(name: QualifiedName, value: () -> Result<DeclI.Value>?) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getValue(name: QualifiedName) = if (name == _name) value() else this@withValue.getValue(name)
        override fun toString() = "${this@withValue} withValue ${name to value()?.foldString()}"
    }
}

private var acc = 0
fun fresh() = "_${acc++}".name
