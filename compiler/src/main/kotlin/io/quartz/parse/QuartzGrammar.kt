package io.quartz.parse

import io.github.aedans.parsek.Parser
import io.github.aedans.parsek.tokenizer.Token

typealias QuartzParser<T> = Parser<Token<TokenType>, T>
