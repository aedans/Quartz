package io.quartz.parse

import io.github.aedans.parsek.dsl.*
import io.github.aedans.parsek.optional
import io.quartz.*
import io.quartz.tree.ast.DeclT
import io.quartz.tree.util.name

val String.declP: QuartzParser<DeclT> get() = parser { traitDeclP } or
        parser { valueDeclP } or
        parser { instanceDeclP }

val String.traitDeclP: QuartzParser<DeclT.Trait> get() = skip(TokenType.TRAIT) then
        TokenType.ID then
        list(parser { constraintP }) then
        skip(TokenType.O_BRACKET) then
        list(parser { memberP }) then
        skip(TokenType.C_BRACKET) map {
    val (t1, t2, t3) = it.tup()
    DeclT.Trait(t1.location(this), t1.text.name, t2, t3)
}

val String.memberP: QuartzParser<DeclT.Trait.Member> get() = skip(TokenType.DEF) then
        TokenType.ID then
        skip(TokenType.EXTENDS) then
        parser { schemeP } map { (t1, t2) ->
    DeclT.Trait.Member(t1.text.name, t1.location(this), t2)
}

val String.valueDeclP: QuartzParser<DeclT.Value> get() = skip(TokenType.DEF) then
        TokenType.ID then
        optional(skip(TokenType.EXTENDS) then parser { schemeP }) then
        skip(TokenType.EQ) then
        parser { exprP } map {
    val (t1, t2, t3) = it.tup()
    DeclT.Value(t1.location(this), t1.text.name, t2, t3)
}

val String.instanceDeclP: QuartzParser<DeclT.Instance> get() = skip(TokenType.INSTANCE) then
        optional(list(parser { constraintP }) then skip(TokenType.FAT_ARROW)) then
        parser { atomicTypeP } then
        parser { typeP } then
        skip(TokenType.O_BRACKET) then
        list(parser { valueDeclP }) then
        skip(TokenType.C_BRACKET) map {
    val (t1, t2, t3, t4) = it.tup()
    DeclT.Instance(t3.location, t1 ?: nil, t2, t3, t4)
}
