package io.quartz.interop

import io.quartz.analyze.IdInfo
import io.quartz.analyze.TypeInfo
import io.quartz.analyze.type.ConstraintK
import io.quartz.analyze.type.SchemeK
import io.quartz.analyze.type.TypeK
import io.quartz.err.Result
import io.quartz.err.resultMonad
import io.quartz.tree.QualifiedName
import io.quartz.tree.ir.ExprI
import io.quartz.tree.name
import io.quartz.tree.qualifiedName
import io.quartz.tree.unqualified
import kategory.binding
import kategory.ev
import kategory.right
import java.io.File
import java.net.URL
import java.net.URLClassLoader

interface ClassPath {
    fun getClass(name: QualifiedName): Result<Class<*>>?
}

fun List<File>.classPath() = map { it.toURI().toURL() }.toTypedArray().classPath()
fun Array<URL>.classPath() = object : ClassPath {
    override fun getClass(name: QualifiedName) = try {
        URLClassLoader(this@classPath).loadClass(name.toString())!!.right()
    } catch (_: ClassNotFoundException) {
        null
    }

    override fun toString() = this@classPath.contentDeepToString()
}

fun ClassPath.getType(name: QualifiedName) = run {
    val clazzO = getClass(name)
    clazzO?.let { clazz ->
        resultMonad().binding {
            val scheme = clazz.map { it.schemeK }.bind()
            val info = TypeInfo(scheme)
            yields(info)
        }.ev()
    }
}

fun ClassPath.getVar(name: QualifiedName) = run {
    val clazzO = getClass(name.varClassName())
    clazzO?.let { clazz ->
        resultMonad().binding {
            val scheme = clazz.map {
                it.getMethod(name.unqualified.varGetterName().string).returnType.schemeK
            }.bind()
            val varLoc  = clazz.map { ExprI.Id.Loc.Global(name) }.bind()
            val info = IdInfo(scheme, varLoc)
            yields(info)
        }.ev()
    }
}

val Class<*>.schemeK: SchemeK
    get() = run {
        val generics = typeParameters.map { ConstraintK(null, it.name.name) }
        val typeK = TypeK.Const(qualifiedName)
        SchemeK(generics, typeK)
    }
