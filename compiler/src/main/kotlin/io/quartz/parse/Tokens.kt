package io.quartz.parse

import io.github.aedans.parsek.dsl.parser
import io.github.aedans.parsek.tokenizer.Token
import io.github.aedans.parsek.tokenizer.TokenInfo
import io.github.aedans.parsek.tokenizer.TokenParser
import io.github.aedans.parsek.tokenizer.tokenParser

enum class TokenType(val regexp: String) : TokenParser<TokenType> {
    WS("\\s+"),
    COMMENT("//.+"),
    FAT_ARROW("=>"),
    F_SLASH("/"),
    ARROW("->"),
    EXTENDS("::"),
    EQ("="),
    O_PAREN("\\("),
    C_PAREN("\\)"),
    O_BRACE("\\["),
    C_BRACE("\\]"),
    O_BRACKET("\\{"),
    C_BRACKET("\\}"),
    BACKSLASH("\\\\"),
    DOT("\\."),
    STAR("\\*"),
    INTERFACE("interface\\b"),
    INSTANCE("instance\\b"),
    PACKAGE("package\\b"),
    IMPORT("import\\b"),
    FALSE("false\\b"),
    THEN("then\\b"),
    ELSE("else\\b"),
    TRUE("true\\b"),
    DEF("def\\b"),
    IS("is\\b"),
    IF("if\\b"),
    AS("as\\b"),
    ID("[_a-zA-Z][_a-zA-Z0-9]*"),
    EOF("\u0000");

    private val parser = parser { tokenParser(this, ignore = listOf(WS, COMMENT)) }
    override fun invoke(p1: Sequence<Token<TokenType>>) = parser(p1)

    companion object {
        val tokens = TokenType.values().map { TokenInfo(it, it.regexp.toPattern()) }
    }
}

val eofToken = Token("\u0000", TokenType.EOF, -1, -1)
