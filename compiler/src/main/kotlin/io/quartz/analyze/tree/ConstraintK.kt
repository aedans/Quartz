package io.quartz.analyze.tree

import io.quartz.tree.util.Name

data class ConstraintK(val type: TypeK, val name: Name) {
    companion object {
        operator fun invoke(type: TypeK?, name: Name) = ConstraintK(type ?: TypeK.any, name)
    }
}
