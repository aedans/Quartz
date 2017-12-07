package io.quartz.gen.jvm.analyze

import io.quartz.gen.jvm.sym.JvmSymTable
import io.quartz.gen.jvm.tree.*
import io.quartz.gen.jvm.util.*
import io.quartz.singletonList
import io.quartz.tree.ir.DeclI
import io.quartz.tree.util.*

fun DeclI.jvm(symTable: JvmSymTable) = when (this) {
    is DeclI.Trait -> jvm()
    is DeclI.Value -> jvm(JvmSymTable.default)
    is DeclI.Instance -> jvm(symTable)
}

fun DeclI.Trait.jvm() = run {
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
            name,
            decls,
            foralls = scheme.foralls,
            annotations = listOf(JvmAnnotation("quartz.lang.Trait".qualifiedName)),
            isInterface = true
    )
}

fun DeclI.Value.jvm(symTable: JvmSymTable) = run {
    val (foralls, constraints, type) = scheme.jvm()
    if (constraints.isNotEmpty())
        TODO()
    val jvmExpr = expr?.jvm(symTable)
    val method = JvmDecl.Method(
            varGetterName,
            foralls,
            emptyList(),
            type,
            expr = jvmExpr,
            isStatic = true
    )
    JvmClass(
            name.varClassName,
            method.singletonList(),
            annotations = listOf(JvmAnnotation("quartz.lang.Value".qualifiedName)),
            isFinal = true
    )
}

fun DeclI.Instance.jvm(symTable: JvmSymTable) = run {
    val qualifiedName = "${instance.qualifiedString}${name ?: ""}\$Instance".qualifiedName
    val instanceType = JvmType.Class(instance)
    val (foralls, constraints, type) = scheme.jvm()
    if (constraints.isNotEmpty())
        TODO()
    val decls = impls.map {
        val (foralls, constraints, type) = it.scheme.jvm()
        JvmDecl.Method(
                it.name.unqualified,
                foralls,
                emptyList(),
                type,
                expr = it.expr?.jvm(symTable)
        )
    }
    JvmClass(
            qualifiedName,
            decls,
            interfaces = listOf(instanceType.copy(generics = instanceType.generics + type)),
            annotations = JvmAnnotation(
                    "quartz.lang.Instance".qualifiedName,
                    JvmAnnotation.Arg("value".name, instanceType.asmType).singletonList()
            ).singletonList(),
            foralls = foralls,
            isFinal = true
    )
}
