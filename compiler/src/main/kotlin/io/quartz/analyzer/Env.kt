package io.quartz.analyzer

import io.quartz.analyzer.type.SchemeK
import io.quartz.tree.Name
import io.quartz.tree.QualifiedName
import io.quartz.tree.Qualifier
import io.quartz.tree.name

interface Env {
    val `package`: Qualifier
    fun getType(name: QualifiedName): EitherE<SchemeK>
    fun getVar(name: QualifiedName): EitherE<SchemeK>
    fun getMemLoc(name: QualifiedName): EitherE<MemLoc>
}

fun Env.withPackage(`package`: Qualifier) = object : Env by this {
    override val `package` = `package`
}

sealed class MemLoc {
    data class Arg(val index: Int) : MemLoc()
    data class Global(val name: QualifiedName) : MemLoc()
    data class Field(val name: Name) : MemLoc()
}

fun Env.mapTypes(map: (SchemeK) -> SchemeK) = object : Env by this {
    override fun getType(name: QualifiedName) = this@mapTypes.getType(name).map(map)
}

fun Env.withType(name: QualifiedName, scheme: EitherE<SchemeK>) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getType(name: QualifiedName) = if (name == _name) scheme else this@withType.getType(name)
    }
}

fun Env.withVar(name: QualifiedName, scheme: EitherE<SchemeK>, memLoc: EitherE<MemLoc>) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getVar(name: QualifiedName) = if (name == _name) scheme else this@withVar.getVar(name)
    }.withMemLoc(name, memLoc)
}

fun Env.withMemLocs(list: List<Pair<QualifiedName, EitherE<MemLoc>>>) = list.fold(this) { e, (a, b) -> e.withMemLoc(a, b) }
fun Env.withMemLoc(name: QualifiedName, memLoc: EitherE<MemLoc>) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getMemLoc(name: QualifiedName) = if (name == _name) memLoc else this@withMemLoc.getMemLoc(name)
    }
}

private var fresh = 0
fun fresh() = "$${fresh++}".name
