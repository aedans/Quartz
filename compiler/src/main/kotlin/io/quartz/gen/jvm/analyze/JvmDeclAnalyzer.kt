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
        val (foralls, constraints, returnType) = it.scheme.jvm()
        if (constraints.isNotEmpty())
            TODO()
        JvmDecl.Method(
                it.name,
                foralls,
                emptyList(),
                returnType
        )
    }
    JvmClass(
            qualifiedName,
            decls,
            foralls = foralls,
            annotations = listOf(JvmAnnotation("quartz.lang.Trait".qualifiedName)),
            isInterface = true
    )
}

fun DeclI.Value.jvm(qualifier: Qualifier, symTable: JvmSymTable) = run {
    val qualifiedName = name.qualify(qualifier)
    val (foralls, constraints, returnType) = scheme.jvm()
    if (constraints.isNotEmpty())
        TODO()
    val jvmExpr = expr.jvm(symTable)
    val method = JvmDecl.Method(
            varGetterName,
            foralls,
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
