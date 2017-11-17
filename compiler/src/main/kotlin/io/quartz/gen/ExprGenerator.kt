package io.quartz.gen

import io.quartz.gen.asm.*
import io.quartz.nil
import io.quartz.singletonList
import io.quartz.tree.ir.*
import io.quartz.tree.ir.ExprI.Invoke.Dispatch.INTERFACE
import io.quartz.tree.ir.ExprI.Invoke.Dispatch.VIRTUAL
import io.quartz.tree.locatableName
import io.quartz.tree.name
import io.quartz.tree.qualifiedName
import io.quartz.tree.qualify
import org.funktionale.collections.prependTo
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter

fun ExprI.generate(mg: MethodGenerator) = when (this) {
    is ExprI.Block -> generate(mg)
    is ExprI.Bool,
    is ExprI.Arg,
    is ExprI.Lambda,
    is ExprI.LocalField -> {

    }
    is ExprI.Invoke,
    is ExprI.InvokeStatic,
    is ExprI.If -> {
        push(mg)
        mg.ga.pop()
    }
}

fun ExprI.push(mg: MethodGenerator) = when (this) {
    is ExprI.Bool -> push(mg)
    is ExprI.Block -> push(mg)
    is ExprI.Invoke -> push(mg)
    is ExprI.InvokeStatic -> push(mg)
    is ExprI.If -> push(mg)
    is ExprI.Arg -> push(mg)
    is ExprI.LocalField -> push(mg)
    is ExprI.Lambda -> push(mg)
}

fun ExprI.Bool.push(mg: MethodGenerator) {
    mg.ga.push(boolean)
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
    args.forEach { it.a.push(mg) }
    (when (dispatch) {
        INTERFACE -> mg.ga::invokeInterface
        VIRTUAL -> mg.ga::invokeVirtual
    })(
            owner.type(),
            method(returnType, name, args.map { it.b })
    )
}

fun ExprI.InvokeStatic.push(mg: MethodGenerator) {
    args.forEach { it.a.push(mg) }
    mg.ga.invokeStatic(
            owner.type(),
            method(type, name, args.map { it.b })
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

fun ExprI.Lambda.push(mg: MethodGenerator) {
    val name = "${mg.classGenerator.info.name.string}$${mg.classGenerator.i++}".name
    val typeI = name.qualify(p).typeI
    val superTypes = TypeI.function(argType, returnType).singletonList()

    mg.visitClassGeneratorLater {
        visitProgramGeneratorLater {
            generateClass(ClassInfo(
                    Opcodes.ACC_PUBLIC,
                    name.qualify(p).locatableName,
                    classSignature(generics, TypeI.any.prependTo(superTypes)),
                    TypeI.any.locatableName,
                    superTypes.map { it.locatableName }
            )) {
                val args = closures.map { it.b.type }
                generateConstructor(MethodInfo(
                        Opcodes.ACC_PUBLIC,
                        method(VoidTypeI, "<init>".name, args),
                        methodSignature(nil, args, VoidTypeI)
                )) {
                    closures.forEachIndexed { i, it ->
                        ga.loadThis()
                        ga.loadArg(i)
                        ga.putField(typeI.type(), it.b.name.toString(), it.b.type.type())
                    }
                }

                closures.forEach {
                    cw.visitField(
                            Opcodes.ACC_PUBLIC,
                            it.b.name.toString(),
                            it.b.type.descriptor,
                            it.b.type.signature,
                            null
                    ).visitEnd()
                }

                val invokeScheme = DeclI.Method.Scheme(nil, listOf(argType), returnType)
                val invokeDecl = DeclI.Method("invoke".name, location, p, invokeScheme, expr)
                invokeDecl.generate(this)
            }
        }

        cw.visitInnerClass(typeI.locatableName.toString(), null, null, Opcodes.ACC_PUBLIC)
    }

    val type = Type.getType(typeI.descriptor)

    mg.ga.newInstance(type)
    mg.ga.dup()

    closures.forEach {
        it.a.push(mg)
    }

    mg.ga.invokeConstructor(
            type,
            method(VoidTypeI, "<init>".name, closures.map { it.b.type })
    )
}
