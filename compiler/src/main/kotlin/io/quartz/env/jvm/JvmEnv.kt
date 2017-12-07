package io.quartz.env.jvm

import io.quartz.env.Env
import io.quartz.err.Result
import io.quartz.gen.jvm.util.*
import io.quartz.tree.ir.DeclI
import io.quartz.tree.util.*
import kategory.right
import java.io.File
import java.net.URLClassLoader

class JvmEnv(classpath: List<File>) : Env {
    private val classLoader = classpath.map { it.toURI().toURL() }.toTypedArray().let(::URLClassLoader)

    override fun getType(name: QualifiedName) = try {
        val clazzName = name.qualifiedString
        val clazz = classLoader.loadClass(clazzName)
        clazz.typeI.right()
    } catch (e: ClassNotFoundException) {
        null
    }

    override fun getValue(name: QualifiedName) = try {
        val clazzName = name.varClassName
        val clazz = classLoader.loadClass(clazzName.qualifiedString)
        val method = clazz.getMethod(varGetterName.string)
        DeclI.Value(
                null,
                clazzName,
                method.returnType.typeI.scheme,
                null
        ).right()
    } catch (e: ClassNotFoundException) {
        null
    }

    override fun getTrait(name: QualifiedName) = try {
        val clazzName = name.qualifiedString
        val clazz = classLoader.loadClass(clazzName)
        DeclI.Trait(
                null,
                clazz.qualifiedName,
                clazz.typeI.scheme,
                clazz.methods.map {
                    DeclI.Trait.Member(
                            null,
                            it.name.name,
                            it.returnType.typeI.scheme
                    )
                }
        ).right()
    } catch (e: ClassNotFoundException) {
        null
    }

    // TODO
    override fun getInstances(name: QualifiedName) = emptySequence<Result<DeclI.Instance>>()
}
