package io.quartz.interop

import io.quartz.analyzer.Err
import io.quartz.analyzer.UnknownClass
import io.quartz.tree.QualifiedName
import kategory.left
import kategory.right
import java.io.File
import java.net.URL
import java.net.URLClassLoader

interface ClassPath {
    fun getClass(name: QualifiedName): Err<Class<*>>
}

fun List<File>.classPath() = map { it.toURI().toURL() }.toTypedArray().classPath()
fun Array<URL>.classPath() = object : ClassPath {
    override fun getClass(name: QualifiedName) = try {
        URLClassLoader(this@classPath).loadClass(name.toString())!!.right()
    } catch (_: ClassNotFoundException) {
        UnknownClass(name).left()
    }
}
