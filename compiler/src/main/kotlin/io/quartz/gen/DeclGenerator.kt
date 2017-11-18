package io.quartz.gen

import io.quartz.analyze.type.tVar
import io.quartz.gen.asm.*
import io.quartz.nil
import io.quartz.tree.ir.ConstraintI
import io.quartz.tree.ir.DeclI
import io.quartz.tree.ir.TypeI
import io.quartz.tree.ir.name
import io.quartz.tree.locatableName
import io.quartz.tree.name
import io.quartz.tree.qualify
import org.objectweb.asm.Opcodes

fun Iterable<DeclI>.generate(pg: ProgramGenerator) = forEach { it.generate(pg) }

fun DeclI.generate(pg: ProgramGenerator) = when (this) {
    is DeclI.Trait -> generate(pg)
    is DeclI.Value -> generate(pg)
    is DeclI.Instance -> TODO()
}

fun DeclI.Trait.generate(pg: ProgramGenerator) {
    val generics = constraints.map { it.name }.toSet().map { ConstraintI(it, TypeI.any) }
    pg.generateClass(ClassInfo(
            Opcodes.ACC_PUBLIC + Opcodes.ACC_INTERFACE + Opcodes.ACC_ABSTRACT,
            name.qualify(p).locatableName,
            classSignature(generics, listOf(TypeI.any)),
            TypeI.any.locatableName,
            emptyList()
    )) {
        constraints.filter { it.type != TypeI.any }.forEach {
            generateMethod(MethodInfo(
                    Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT,
                    method(it.type.apply { it.name.tVar }, "${it.name}$${it.type.name}".name, emptyList()),
                    methodSignature(nil, nil, it.type)
            )) { }
        }

        members.forEach {
            generateMethod(MethodInfo(
                    Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT,
                    method(it.scheme.type, it.name, nil),
                    methodSignature(nil, emptyList(), it.scheme.type)
            )) { }
        }
    }
}

fun DeclI.Value.generate(pg: ProgramGenerator) {
    pg.generateClass(ClassInfo(
            Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL,
            name.qualify(p).locatableName,
            classSignature(nil, listOf(TypeI.any)),
            TypeI.any.locatableName,
            emptyList()
    )) {
        generateMethod(MethodInfo(
                Opcodes.ACC_PUBLIC,
                method(scheme.type, name, emptyList()),
                methodSignature(scheme.constraints, emptyList(), scheme.type)
        )) {
            expr.run {
                push(this@generateMethod)
                box(scheme.type)
            }
        }
    }
}
