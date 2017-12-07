package io.quartz.analyze.tree

import io.quartz.tree.util.Name

data class SchemeK(val foralls: Set<Name>, val constraints: List<ConstraintK>, val type: TypeK)
