package io.quartz.analyzer

import io.quartz.analyzer.type.SchemeK
import io.quartz.tree.Name
import io.quartz.tree.QualifiedName
import io.quartz.tree.name

interface Env {
    fun getType(name: QualifiedName): Err<TypeInfo>
    fun getVar(name: QualifiedName): Err<VarInfo>
}

/** ADT representing where an identifier is located */
sealed class VarLoc {
    data class Arg(val index: Int) : VarLoc()
    data class Global(val name: QualifiedName) : VarLoc()
    data class Field(val name: Name) : VarLoc()
}

data class VarInfo(
        val scheme: SchemeK,
        val varLoc: VarLoc
)

data class TypeInfo(
        val scheme: SchemeK
)

fun Env.mapTypes(map: (QualifiedName, Err<TypeInfo>) -> Err<TypeInfo>) = object : Env by this {
    override fun getType(name: QualifiedName) = map(name, this@mapTypes.getType(name))
}

fun Env.mapVars(map: (QualifiedName, Err<VarInfo>) -> Err<VarInfo>) = object : Env by this {
    override fun getVar(name: QualifiedName) = map(name, this@mapVars.getVar(name))
}

fun Env.withType(name: QualifiedName, scheme: Err<TypeInfo>) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getType(name: QualifiedName) = if (name == _name) scheme else this@withType.getType(name)
    }
}

fun Env.withVar(name: QualifiedName, varInfo: Err<VarInfo>) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getVar(name: QualifiedName) = if (name == _name) varInfo else this@withVar.getVar(name)
    }
}

fun Env.withVarLoc(name: QualifiedName, varLoc: VarLoc) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : Env by this {
        override fun getVar(name: QualifiedName) = if (name == _name)
            this@withVarLoc.getVar(name).map { it.copy(varLoc = varLoc) }
        else
            this@withVarLoc.getVar(name)
    }
}

private var fresh = 0
fun fresh() = "$${fresh++}".name
