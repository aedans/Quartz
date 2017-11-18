package io.quartz.tree.ast

import io.quartz.tree.Locatable
import io.quartz.tree.Location
import io.quartz.tree.Name

/** Sealed class representing all AST declarations */
sealed class DeclT : Locatable {
    data class Trait(
            override val location: Location,
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
            override val location: Location,
            val name: Name,
            val schemeT: SchemeT?,
            val expr: ExprT
    ) : DeclT()

    data class Instance(
            override val location: Location,
            val constraints: List<ConstraintT>,
            val type: TypeT,
            val instance: TypeT,
            val impls: List<Value>
    ) : DeclT()
}
