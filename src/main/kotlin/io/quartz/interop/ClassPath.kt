package io.quartz.interop

import java.io.File
import java.net.URL
import java.net.URLClassLoader

/**
 * @author Aedan Smith
 */

interface ClassPath {
    fun getClass(name: String): Class<*>?
}

fun String.classPath() = split(';').toList().map { File(it) }.classPath()
fun List<File>.classPath() = map { it.toURI().toURL() }.toTypedArray().classPath()
fun Array<URL>.classPath() = object : ClassPath {
    override fun getClass(name: String) = try {
        URLClassLoader(this@classPath).loadClass(name)
    } catch (_: ClassNotFoundException) {
        null
    }
}