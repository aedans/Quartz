package io.quartz.env.jvm

import io.quartz.analyze.type.SchemeK
import io.quartz.env.*
import io.quartz.gen.jvm.util.*
import io.quartz.tree.util.QualifiedName
import kategory.right
import java.io.File
import java.net.URLClassLoader

class JvmEnv(classpath: List<File>) : Env {
    private val classLoader = classpath.map { it.toURI().toURL() }.toTypedArray().let(::URLClassLoader)

    override fun getType(name: QualifiedName) = try {
        val clazzName = name.qualifiedString
        val clazz = classLoader.loadClass(clazzName)
        TypeInfo(SchemeK(emptyList(), clazz.typeK)).right()
    } catch (e: ClassNotFoundException) {
        null
    }

    override fun getVar(name: QualifiedName) = try {
        val clazzName = name.varClassName
        val clazz = classLoader.loadClass(clazzName.qualifiedString)
        val method = clazz.getMethod(varGetterName.string)
        VarInfo(SchemeK(emptyList(), method.returnType.typeK)).right()
    } catch (e: ClassNotFoundException) {
        null
    }
}
