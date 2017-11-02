package io.quartz.interop

import io.quartz.analyzer.EitherE
import io.quartz.analyzer.UnknownType
import io.quartz.tree.QualifiedName
import kategory.left
import kategory.right
import java.io.File
import java.net.URL
import java.net.URLClassLoader

interface ClassPath {
    fun getClass(name: QualifiedName): EitherE<Class<*>>
}

fun List<File>.classPath() = map { it.toURI().toURL() }.toTypedArray().classPath()
fun Array<URL>.classPath() = object : ClassPath {
    override fun getClass(name: QualifiedName) = try {
        URLClassLoader(this@classPath).loadClass(name.toString()).right()
    } catch (_: ClassNotFoundException) {
        UnknownType(name).left()
    }
}
