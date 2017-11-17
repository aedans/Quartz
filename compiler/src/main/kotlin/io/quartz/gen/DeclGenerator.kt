package io.quartz.gen

import io.quartz.gen.asm.*
import io.quartz.nil
import io.quartz.tree.ir.DeclI
import io.quartz.tree.ir.TypeI
import io.quartz.tree.ir.VoidTypeI
import io.quartz.tree.locatableName
import io.quartz.tree.name
import io.quartz.tree.qualify
import org.funktionale.collections.prependTo
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

fun Iterable<DeclI>.generate(pg: ProgramGenerator) = forEach { it.generate(pg) }

fun DeclI.generate(pg: ProgramGenerator) = when (this) {
    is DeclI.Class -> generate(pg)
    is DeclI.Method -> throw Exception()
}

fun DeclI.Class.generate(pg: ProgramGenerator) {
    val access = Opcodes.ACC_PUBLIC +
            (if (constructor == null) Opcodes.ACC_INTERFACE + Opcodes.ACC_ABSTRACT else 0)
    pg.generateClass(ClassInfo(
            access,
            name.qualify(p).locatableName,
            classSignature(generics, TypeI.any.prependTo(superTypes)),
            TypeI.any.locatableName,
            superTypes.map { it.locatableName }
    )) {
        if (constructor != null) {
            generateConstructor(MethodInfo(
                    Opcodes.ACC_PUBLIC,
                    method(VoidTypeI, "<init>".name, constructor.args),
                    methodSignature(nil, constructor.args, VoidTypeI)
            )) {
                constructor.expr.generate(this)
            }
        }

        decls.forEach {
            it.generate(this)
        }
    }
}

fun DeclI.generate(cg: ClassGenerator) = when (this) {
    is DeclI.Class -> generate(cg)
    is DeclI.Method -> generate(cg)
}

fun DeclI.Class.generate(cg: ClassGenerator) {
    val name = "${cg.info.name}\$$name".name

    cg.visitProgramGeneratorLater {
        copy(name = name).generate(this)
    }

    cg.cw.visitInnerClass(
            name.toString(),
            cg.info.name.toString(),
            this.name.toString(),
            Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC
    )
}

fun DeclI.Method.generate(cg: ClassGenerator) {
    val access = Opcodes.ACC_PUBLIC +
            (if (expr == null) Opcodes.ACC_ABSTRACT else 0)
    cg.generateMethod(MethodInfo(
            access,
            method(scheme.ret, name, scheme.args),
            methodSignature(scheme.generics, scheme.args, scheme.ret)
    )) {
        expr?.run {
            push(this@generateMethod)
            when (scheme.ret) {
                TypeI.bool -> ga.box(Type.BOOLEAN_TYPE)
                TypeI.byte -> ga.box(Type.BYTE_TYPE)
                TypeI.char -> ga.box(Type.CHAR_TYPE)
                TypeI.short -> ga.box(Type.SHORT_TYPE)
                TypeI.int -> ga.box(Type.INT_TYPE)
                TypeI.long -> ga.box(Type.LONG_TYPE)
                TypeI.float -> ga.box(Type.FLOAT_TYPE)
                TypeI.double -> ga.box(Type.DOUBLE_TYPE)
            }
            ga.returnValue()
        }
    }
}
