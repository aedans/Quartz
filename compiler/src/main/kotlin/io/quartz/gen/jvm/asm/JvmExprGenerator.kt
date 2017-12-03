package io.quartz.gen.jvm.asm

import io.quartz.*
import io.quartz.gen.jvm.sym.JvmMemLoc
import io.quartz.gen.jvm.tree.*
import io.quartz.gen.jvm.util.*
import io.quartz.tree.util.name
import org.objectweb.asm.commons.*

fun JvmExpr.generate(mg: MethodGenerator): Unit = when (this) {
    is JvmExpr.Var -> generate(mg)
    is JvmExpr.Invoke -> generate(mg)
    is JvmExpr.If -> generate(mg)
    is JvmExpr.Lambda -> generate(mg)
}

fun JvmExpr.Var.generate(mg: MethodGenerator) {
    when (memLoc) {
        is JvmMemLoc.Arg -> mg.ga.loadArg(memLoc.index)
        is JvmMemLoc.Global -> mg.ga.invokeStatic(
                JvmType.Class(memLoc.name.varClassName).asmType,
                method(type, varGetterName, nil)
        )
        is JvmMemLoc.LocalField -> {
            mg.ga.loadThis()
            mg.ga.getField(
                    JvmType.Class(mg.cg.name).asmType,
                    memLoc.name.string,
                    type.asmType
            )
        }
    }
}

fun JvmExpr.Invoke.generate(mg: MethodGenerator) {
    expr1.generate(mg)
    expr2.generate(mg)
    mg.ga.invokeInterface(
            JvmType.function.asmType,
            Method.getMethod("${arrow.t1.string} invoke (${arrow.t2.string})")
    )
}

fun JvmExpr.If.generate(mg: MethodGenerator) {
    val endLabel = mg.ga.newLabel()
    val falseLabel = mg.ga.newLabel()

    condition.generate(mg)

    mg.ga.ifZCmp(GeneratorAdapter.EQ, falseLabel)

    expr1.generate(mg)

    mg.ga.goTo(endLabel)

    mg.ga.mark(falseLabel)

    expr2.generate(mg)

    mg.ga.mark(endLabel)
}

fun JvmExpr.Lambda.generate(mg: MethodGenerator) {
    val name = mg.cg.name.run { copy(string = "$string$${mg.cg.i++}") }
    val type = JvmType.Class(name).asmType
    val invoke = JvmDecl.Method(
            "invoke".name,
            emptyList(),
            argType.singletonList(),
            returnType,
            expr
    )
    val fields = closures.map { (name, _, type) ->
        JvmDecl.Field(name, type)
    }
    val clazz = JvmClass(
            name,
            invoke.singletonList() + fields,
            generics = generics,
            interfaces = listOf(JvmType.function(argType, returnType)),
            isFinal = true
    )
    mg.cg.jg.generate(clazz)

    mg.ga.newInstance(type)
    mg.ga.dup()

    mg.ga.invokeConstructor(
            type,
            method(JvmType.Void, "<init>".name, emptyList())
    )

    closures.forEach { (name, expr, t) ->
        mg.ga.dup()
        expr.generate(mg)
        mg.ga.putField(type, name.string, t.asmType)
    }
}
