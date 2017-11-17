package io.quartz.parse

import io.github.aedans.parsek.dsl.*
import io.github.aedans.parsek.optional
import io.github.aedans.parsek.toSuccessOrExcept
import io.github.aedans.parsek.tokenizer.tokenize
import io.quartz.nil
import io.quartz.tree.Qualifier
import io.quartz.tree.ast.FileT
import io.quartz.tree.ast.ImportT
import io.quartz.tree.name
import io.quartz.tree.qualifiedName
import io.quartz.tree.unqualified
import java.io.File

fun File.parse() = run {
    try {
        val rest = tokenize(TokenType.tokens) + eofToken
        val (rest1, p) = packageP(rest).toSuccessOrExcept()
        val (rest2, imports) = list(importP)(rest1).toSuccessOrExcept()
        val (rest3, decls) = (list(name.declP) then skip(TokenType.EOF))(rest2).toSuccessOrExcept()
        if (rest3.any())
            throw Exception("Unexpected token ${rest3.first()}")
        FileT(p, imports, decls)
    } catch (e: Exception) {
        throw Exception("Exception parsing $name", e)
    }
}

val packageP: QuartzParser<Qualifier> get() = optional(skip(TokenType.PACKAGE) then parser { qualifierP }) map {
    it ?: nil
}

val importP: QuartzParser<ImportT> get() = parser { importStarP } or parser { importQualifiedP }

val importQualifiedP: QuartzParser<ImportT.Qualified> get() = skip(TokenType.IMPORT) then
        parser { qualifierP } then
        optional(skip(TokenType.AS) then TokenType.ID) map { (t1, t2) ->
    ImportT.Qualified(t1.qualifiedName, t2?.text?.name ?: t1.qualifiedName.unqualified)
}

val importStarP: QuartzParser<ImportT.Star> get() =
    skip(TokenType.IMPORT) then skip(TokenType.STAR) then parser { qualifierP } map { ImportT.Star(it.second) }

val qualifierP: QuartzParser<Qualifier> get() =
    list(TokenType.ID then skip(TokenType.DOT)) then TokenType.ID map { (t1, t2) ->
        (t1 + t2).map { it.text }
    }
