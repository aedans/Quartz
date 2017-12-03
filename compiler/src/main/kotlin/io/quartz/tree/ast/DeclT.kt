package io.quartz.tree.ast

import io.quartz.tree.util.*

sealed class DeclT : Locatable {
    data class Trait(
            override val location: Location?,
            val name: Name,
            val constraints: List<ConstraintT>,
            val members: List<Member>
    ) : DeclT() {
        data class Member(
                val name: Name,
                val location: Location,
                val schemeT: SchemeT
        )
    }

    data class Value(
            override val location: Location?,
            val name: Name,
            val scheme: SchemeT?,
            val expr: ExprT
    ) : DeclT()

    data class Instance(
            override val location: Location?,
            val constraints: List<ConstraintT>,
            val type: TypeT,
            val instance: TypeT,
            val impls: List<Value>
    ) : DeclT()
}
