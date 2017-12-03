package io.quartz.gen.jvm

import io.quartz.gen.Generator
import io.quartz.gen.jvm.analyze.jvm
import io.quartz.gen.jvm.asm.generate
import io.quartz.gen.jvm.sym.JvmSymTable
import io.quartz.gen.jvm.tree.JvmClass
import io.quartz.tree.ir.DeclI
import io.quartz.tree.util.Context
import java.io.File

class JvmGenerator(private val out: File) : Generator {
    override fun generate(declContext: Context<DeclI>) {
        val (qualifier, _, decl) = declContext
        val jvmClass = when (decl) {
            is DeclI.Trait -> decl.jvm(qualifier)
            is DeclI.Value -> decl.jvm(qualifier, JvmSymTable.default)
            is DeclI.Instance -> TODO()
        }
        generate(jvmClass)
    }

    fun generate(jvmClass: JvmClass) {
        val cw = jvmClass.generate(this)
        File(out, "${jvmClass.name.locatableString}.class")
                .also { it.parentFile.mkdirs() }
                .writeBytes(cw.toByteArray())
    }
}
