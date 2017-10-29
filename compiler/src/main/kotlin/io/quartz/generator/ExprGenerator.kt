package io.quartz.generator

import io.quartz.generator.asm.MethodGenerator
import io.quartz.generator.asm.method
import io.quartz.generator.asm.type
import io.quartz.tree.*
import io.quartz.tree.ir.DeclI
import io.quartz.tree.ir.ExprI
import io.quartz.tree.ir.ExprI.Invoke.Dispatch.INTERFACE
import io.quartz.tree.ir.ExprI.Invoke.Dispatch.VIRTUAL
import io.quartz.tree.ir.VoidTypeI
import io.quartz.tree.ir.typeI
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter

fun ExprI.generate(mg: MethodGenerator) = when (this) {
    is ExprI.Block -> generate(mg)
    is ExprI.Set -> generate(mg)
    is ExprI.Bool,
    is ExprI.This,
    is ExprI.Arg,
    is ExprI.LocalField -> {

    }
    is ExprI.Invoke,
    is ExprI.InvokeStatic,
    is ExprI.If,
    is ExprI.AnonymousObject -> {
        push(mg)
        mg.ga.pop()
    }
}

fun ExprI.push(mg: MethodGenerator) = when (this) {
    is ExprI.Bool -> push(mg)
    is ExprI.This -> push(mg)
    is ExprI.Block -> push(mg)
    is ExprI.Invoke -> push(mg)
    is ExprI.InvokeStatic -> push(mg)
    is ExprI.If -> push(mg)
    is ExprI.Arg -> push(mg)
    is ExprI.LocalField -> push(mg)
    is ExprI.AnonymousObject -> push(mg)
    is ExprI.Set -> push(mg)
}

fun ExprI.Bool.push(mg: MethodGenerator) {
    mg.ga.push(boolean)
}

@Suppress("unused")
fun ExprI.This.push(mg: MethodGenerator) {
    mg.ga.loadThis()
}

fun ExprI.Block.generate(mg: MethodGenerator) {
    exprs.forEach { it.generate(mg) }
}

fun ExprI.Block.push(mg: MethodGenerator) {
    if (exprs == nil)
        throw Exception()

    exprs.dropLast(1).forEach { it.generate(mg) }
    exprs.last().push(mg)
}

fun ExprI.Invoke.push(mg: MethodGenerator) {
    expr.push(mg)
    args.forEach { it.first.push(mg) }
    (when (dispatch) {
        INTERFACE -> mg.ga::invokeInterface
        VIRTUAL -> mg.ga::invokeVirtual
    })(
            owner.type(),
            method(type, name, args.map { it.second })
    )
}

fun ExprI.InvokeStatic.push(mg: MethodGenerator) {
    args.forEach { it.first.push(mg) }
    mg.ga.invokeStatic(
            owner.type(),
            method(type, name, args.map { it.second })
    )
}

fun ExprI.If.push(mg: MethodGenerator) {
    val endLabel = mg.ga.newLabel()
    val falseLabel = mg.ga.newLabel()

    condition.push(mg)

    mg.ga.ifZCmp(GeneratorAdapter.EQ, falseLabel)

    expr1.push(mg)

    mg.ga.goTo(endLabel)

    mg.ga.mark(falseLabel)

    expr2.push(mg)

    mg.ga.mark(endLabel)
}

fun ExprI.Arg.push(mg: MethodGenerator) {
    mg.ga.loadArg(index)
}

fun ExprI.LocalField.push(mg: MethodGenerator) {
    mg.ga.loadThis()
    mg.ga.getField(
            mg.classGenerator.info.name.qualifiedName.typeI.type(),
            name.toString(),
            type.type()
    )
}

fun ExprI.AnonymousObject.push(mg: MethodGenerator) {
    val name = "${mg.classGenerator.info.name.name}$${mg.classGenerator.i++}".name
    val typeI = name.qualify(qualifier).typeI

    val block = closures.mapIndexed { i, it -> ExprI.Set(
            Location.unknown,
            typeI,
            it.first.name,
            it.second,
            ExprI.This(Location.unknown),
            ExprI.Arg(Location.unknown, i)
    ) }

    val constructor = DeclI.Class.Constructor(
            closures.map { it.second },
            ExprI.Block(Location.unknown, block)
    )

    val newFields = closures.map { (a, b) ->
        DeclI.Field(a.name, Location.unknown, b)
    }

    mg.visitClassGeneratorLater {
        visitProgramGeneratorLater {
            DeclI.Class(
                    name,
                    location,
                    qualifier,
                    constructor,
                    obj.copy(decls = newFields + obj.decls)
            ).generate(this)
        }

        visitInnerClass(typeI.locatableName.toString(), null, null, Opcodes.ACC_PUBLIC)
    }

    val type = Type.getType(typeI.descriptor)

    mg.ga.newInstance(type)
    mg.ga.dup()

    closures.forEach {
        it.first.push(mg)
    }

    mg.ga.invokeConstructor(
            type,
            method(VoidTypeI, "<init>".name, closures.map { it.second })
    )
}

fun ExprI.Set.push(mg: MethodGenerator) {
    generate(mg)
    mg.ga.getField(owner.type(), name.toString(), type.type())
}

fun ExprI.Set.generate(mg: MethodGenerator) {
    expr1.push(mg)
    expr2.push(mg)
    mg.ga.putField(owner.type(), name.toString(), type.type())
}
