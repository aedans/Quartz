package io.quartz.tree.ir

import io.quartz.tree.util.Name

data class SchemeI(val foralls: Set<Name>, val constraints: List<ConstraintI>, val type: TypeI)
