package io.quartz.tree.ir

import io.quartz.tree.Locatable
import io.quartz.tree.Location
import io.quartz.tree.Name
import io.quartz.tree.Qualifier

/**
 * @author Aedan Smith
 */

sealed class ExprI : Locatable {
    data class Bool(
            override val location: Location,
            val boolean: Boolean
    ) : ExprI()

    data class Block(
            override val location: Location,
            val exprs: List<ExprI>
    ) : ExprI()

    data class Arg(
            override val location: Location,
            val index: Int
    ) : ExprI()

    data class LocalField(
            override val location: Location,
            val name: Name,
            val type: TypeI
    ) : ExprI()

    data class Invoke(
            override val location: Location,
            val expr: ExprI,
            val owner: TypeI,
            val type: TypeI,
            val name: Name,
            val args: List<Pair<ExprI, TypeI>>,
            val dispatch: Dispatch
    ) : ExprI() {
        enum class Dispatch {
            INTERFACE,
            VIRTUAL
        }
    }

    data class InvokeStatic(
            override val location: Location,
            val owner: TypeI,
            val type: TypeI,
            val name: Name,
            val args: List<Pair<ExprI, TypeI>>
    ) : ExprI()

    data class If(
            override val location: Location,
            val condition: ExprI,
            val expr1: ExprI,
            val expr2: ExprI
    ) : ExprI()

    data class AnonymousObject(
            override val location: Location,
            val qualifier: Qualifier,
            val obj: DeclI.Class.Object,
            val closures: List<Pair<ExprI.LocalField, TypeI>>
    ) : ExprI()

    data class Set(
            override val location: Location,
            val owner: TypeI,
            val name: Name,
            val type: TypeI,
            val expr1: ExprI,
            val expr2: ExprI
    ) : ExprI()

    data class This(override val location: Location) : ExprI()
}
