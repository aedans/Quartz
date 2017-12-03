package io.quartz.gen.jvm.sym

import io.quartz.tree.util.QualifiedName

interface JvmSymTable {
    fun getMemLoc(name: QualifiedName): JvmMemLoc

    companion object {
        val default = object : JvmSymTable {
            override fun getMemLoc(name: QualifiedName) = JvmMemLoc.Global(name)
        }
    }
}

fun JvmSymTable.mapMemLoc(map: (QualifiedName, JvmMemLoc) -> JvmMemLoc) =
        object : JvmSymTable {
            override fun getMemLoc(name: QualifiedName) = map(name, this@mapMemLoc.getMemLoc(name))
        }

fun JvmSymTable.withMemLoc(name: QualifiedName, jvmMemLoc: JvmMemLoc) = run {
    @Suppress("UnnecessaryVariable", "LocalVariableName")
    val _name = name
    object : JvmSymTable {
        override fun getMemLoc(name: QualifiedName) = if (name == _name) jvmMemLoc else this@withMemLoc.getMemLoc(name)
    }
}
