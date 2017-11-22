package io.quartz.gen.asm

import io.quartz.nil
import io.quartz.tree.Name
import io.quartz.tree.ir.ConstraintI
import io.quartz.tree.ir.TypeI
import io.quartz.tree.ir.signature
import org.objectweb.asm.commons.Method

fun method(returnType: TypeI, name: Name, args: List<TypeI>) = Method.getMethod(
        "${returnType.qualifiedName} $name ${args.joinToString(prefix = "(", postfix = ")") { it.qualifiedName.toString() }}"
)!!

fun TypeI.type() = org.objectweb.asm.Type.getType(descriptor)!!

fun methodSignature(
        constraints: List<ConstraintI>,
        args: List<TypeI>,
        returnType: TypeI
) = when (constraints) {
    nil -> ""
    else -> constraints.joinToString(prefix = "<", postfix = ">", separator = "") { "${it.name}:${it.type.signature}" }
} + args.joinToString(prefix = "(", postfix = ")", separator = "") { it.signature } +
        returnType.signature

fun classSignature(
        constraints: List<ConstraintI>,
        superTypes: List<TypeI>
) = when (constraints) {
    nil -> ""
    else -> constraints.joinToString(prefix = "<", postfix = ">", separator = "") { "${it.name}:${it.type.signature}" }
} + superTypes.joinToString(separator = "", prefix = "", postfix = "") { it.signature }
