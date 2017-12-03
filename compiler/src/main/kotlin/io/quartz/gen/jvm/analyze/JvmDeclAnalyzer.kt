package io.quartz.gen.jvm.analyze

import io.quartz.gen.jvm.sym.JvmSymTable
import io.quartz.gen.jvm.tree.*
import io.quartz.gen.jvm.util.*
import io.quartz.singletonList
import io.quartz.tree.ir.DeclI
import io.quartz.tree.util.*

fun DeclI.Trait.jvm(qualifier: Qualifier): JvmClass = run {
    val qualifiedName = name.qualify(qualifier)
    val decls = members.map {
        val (generics, returnType) = it.scheme.jvm()
        JvmDecl.Method(
                it.name,
                generics,
                emptyList(),
                returnType
        )
    }
    JvmClass(
            qualifiedName,
            decls,
            annotations = listOf(JvmAnnotation("quartz.lang.Trait".qualifiedName)),
            isInterface = true
    )
}

fun DeclI.Value.jvm(qualifier: Qualifier, symTable: JvmSymTable) = run {
    val qualifiedName = name.qualify(qualifier)
    val (generics, returnType) = scheme.jvm()
    val jvmExpr = expr.jvm(symTable)
    val method = JvmDecl.Method(
            varGetterName,
            generics,
            emptyList(),
            returnType,
            expr = jvmExpr
    )
    JvmClass(
            qualifiedName.varClassName,
            method.singletonList(),
            annotations = listOf(JvmAnnotation("quartz.lang.Value".qualifiedName)),
            isFinal = true
    )
}
