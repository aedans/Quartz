package io.quartz.interop

import io.quartz.tree.QualifiedName
import java.io.File
import java.net.URL
import java.net.URLClassLoader

/**
 * @author Aedan Smith
 */

interface ClassPath {
    fun getClass(name: QualifiedName): Class<*>?
}

fun List<File>.classPath() = map { it.toURI().toURL() }.toTypedArray().classPath()
fun Array<URL>.classPath() = object : ClassPath {
    override fun getClass(name: QualifiedName) = try {
        URLClassLoader(this@classPath).loadClass(name.toString())
    } catch (_: ClassNotFoundException) {
        null
    }
}