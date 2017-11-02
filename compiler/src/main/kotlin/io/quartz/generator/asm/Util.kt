package io.quartz.generator.asm

import io.quartz.tree.Name
import io.quartz.tree.ir.GenericI
import io.quartz.tree.ir.TypeI
import io.quartz.tree.ir.signature
import io.quartz.nil
import org.objectweb.asm.commons.Method

fun method(returnType: TypeI, name: Name, args: List<TypeI>) = Method.getMethod(
        "${returnType.qualifiedName} $name ${args.joinToString(prefix = "(", postfix = ")") { it.qualifiedName.toString() }}"
)!!

fun TypeI.type() = org.objectweb.asm.Type.getType(descriptor)!!

fun methodSignature(
        generics: List<GenericI>,
        args: List<TypeI>,
        returnType: TypeI
) = when (generics) {
    nil -> ""
    else -> generics.joinToString(prefix = "<", postfix = ">", separator = "") { "${it.name}:${it.type.signature}" }
} + args.joinToString(prefix = "(", postfix = ")", separator = "") { it.signature } +
        returnType.signature

fun classSignature(
        generics: List<GenericI>,
        superTypes: List<TypeI>
) = when (generics) {
    nil -> ""
    else -> generics.joinToString(prefix = "<", postfix = ">", separator = "") { "${it.name}:${it.type.signature}" }
} + superTypes.joinToString(separator = "", prefix = "", postfix = "") { it.signature }
