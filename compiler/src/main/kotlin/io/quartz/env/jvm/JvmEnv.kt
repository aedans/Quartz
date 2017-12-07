package io.quartz.env.jvm

import io.quartz.analyze.tree.DeclK
import io.quartz.env.Env
import io.quartz.err.Result
import io.quartz.gen.jvm.util.*
import io.quartz.tree.util.*
import kategory.right
import java.io.File
import java.net.URLClassLoader

class JvmEnv(classpath: List<File>) : Env {
    private val classLoader = classpath.map { it.toURI().toURL() }.toTypedArray().let(::URLClassLoader)

    override fun getType(name: QualifiedName) = try {
        val clazzName = name.qualifiedString
        val clazz = classLoader.loadClass(clazzName)
        clazz.typeK.right()
    } catch (e: ClassNotFoundException) {
        null
    }

    override fun getValue(name: QualifiedName) = try {
        val clazzName = name.varClassName
        val clazz = classLoader.loadClass(clazzName.qualifiedString)
        val method = clazz.getMethod(varGetterName.string)
        DeclK.Value(method.returnType.typeK.scheme).right()
    } catch (e: ClassNotFoundException) {
        null
    }

    override fun getTrait(name: QualifiedName) = try {
        val clazzName = name.qualifiedString
        val clazz = classLoader.loadClass(clazzName)
        DeclK.Trait(clazz.qualifiedName).right()
    } catch (e: ClassNotFoundException) {
        null
    }

    // TODO
    override fun getInstances(name: QualifiedName) = emptySequence<Result<DeclK.Instance>>()
}
