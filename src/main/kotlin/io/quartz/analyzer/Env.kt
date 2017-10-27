package io.quartz.analyzer

import io.quartz.analyzer.type.SchemeK
import io.quartz.analyzer.type.TypeK
import io.quartz.tree.ir.ExprI

/**
 * @author Aedan Smith
 */

interface Env {
    fun getType(name: String): SchemeK?
    fun getVar(name: String): SchemeK?
    fun getMemLoc(name: String): MemLoc?
}

sealed class MemLoc {
    data class Arg(val index: Int) : MemLoc()
    data class Global(val name: String) : MemLoc()
    data class Field(val name: String) : MemLoc()
}

fun Env.mapTypes(map: (SchemeK) -> SchemeK) = object : Env by this {
    override fun getType(name: String) = this@mapTypes.getType(name)?.let(map)
}

fun Env.withTypes(list: List<Pair<String, SchemeK>>) = list.fold(this) { e, (a, b) -> e.withType(a, b) }
fun Env.withType(name: String, scheme: SchemeK) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getType(name: String) = if (name == _name) scheme else this@withType.getType(name)
    }
}

fun Env.withVars(list: List<Pair<String, SchemeK>>) = list.fold(this) { e, (a, b) -> e.withVar(a, b) }
fun Env.withVar(name: String, scheme: SchemeK) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getVar(name: String) = if (name == _name) scheme else this@withVar.getVar(name)
    }
}

fun Env.withMemLocs(list: List<Pair<String, MemLoc>>) = list.fold(this) { e, (a, b) -> e.withMemLoc(a, b) }
fun Env.withMemLoc(name: String, memLoc: MemLoc) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getMemLoc(name: String) = if (name == _name) memLoc else this@withMemLoc.getMemLoc(name)
    }
}

fun env(
        typeMap: Env.(String) -> SchemeK?,
        varMap: Env.(String) -> SchemeK?,
        memLocMap: Env.(String) -> MemLoc?
): Env = object : Env {
    override fun getType(name: String) = typeMap(name)
    override fun getVar(name: String) = varMap(name)
    override fun getMemLoc(name: String) = memLocMap(name)
}

var fresh = 0
fun fresh() = "$${fresh++}"
