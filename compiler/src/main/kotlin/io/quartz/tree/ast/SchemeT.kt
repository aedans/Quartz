package io.quartz.tree.ast

import io.quartz.tree.util.Name

data class SchemeT(val foralls: Set<Name>, val constraints: List<ConstraintT>, val type: TypeT)
