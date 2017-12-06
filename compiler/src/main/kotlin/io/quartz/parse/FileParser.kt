package io.quartz.parse

import io.github.aedans.parsek.*
import io.github.aedans.parsek.dsl.*
import io.github.aedans.parsek.tokenizer.tokenize
import io.quartz.err.*
import io.quartz.nil
import io.quartz.tree.util.*
import kategory.right
import java.io.File

fun List<File>.parse() = map(File::parse).flat().map { it.flatten() }

fun File.parse() = run {
    try {
        val rest = tokenize(TokenType.tokens) + eofToken
        val (rest1, p) = packageP(rest).toSuccessOrExcept()
        val (rest2, imports) = list(importP)(rest1).toSuccessOrExcept()
        val (rest3, decls) = (list(name.declP) then skip(TokenType.EOF))(rest2).toSuccessOrExcept()
        if (rest3.any())
            throw Exception("Unexpected token ${rest3.first()}")
        else
            decls.map { Context(p, imports, it) }.right()
    } catch (e: Exception) {
        err { "Error parsing $name: ${e.message}" }
    }
}

val packageP: QuartzParser<Qualifier> get() = optional(skip(TokenType.PACKAGE) then parser { qualifierP }) map {
    it ?: nil
}

val importP: QuartzParser<Import> get() = parser { importStarP } or parser { importQualifiedP }

val importQualifiedP: QuartzParser<Import.Qualified> get() = skip(TokenType.IMPORT) then
        parser { qualifierP } then
        optional(skip(TokenType.AS) then TokenType.ID) map { (t1, t2) ->
    Import.Qualified(t1.qualifiedName, t2?.text?.name ?: t1.qualifiedName.unqualified)
}

val importStarP: QuartzParser<Import.Star> get() =
    skip(TokenType.IMPORT) then skip(TokenType.STAR) then parser { qualifierP } map { Import.Star(it.second) }

val qualifierP: QuartzParser<Qualifier> get() =
    list(TokenType.ID then skip(TokenType.DOT)) then TokenType.ID map { (t1, t2) ->
        (t1 + t2).map { it.text }
    }
