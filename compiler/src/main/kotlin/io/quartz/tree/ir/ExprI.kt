package io.quartz.tree.ir

import io.quartz.tree.Locatable
import io.quartz.tree.Location
import io.quartz.tree.Name
import io.quartz.tree.QualifiedName
import io.quartz.tree.ast.Package
import kategory.Tuple2

/** Sealed class representing all IR expressions */
sealed class ExprI : Locatable {
    data class Block(
            override val location: Location,
            val exprs: List<ExprI>
    ) : ExprI()

    data class Id(
            override val location: Location,
            val name: QualifiedName,
            val loc: Loc,
            val type: TypeI
    ) : ExprI() {
        /** ADT representing where an identifier is located */
        sealed class Loc {
            data class Arg(val index: Int) : Loc()
            data class Global(val name: QualifiedName) : Loc()
            data class Field(val name: Name) : Loc()
        }
    }

    data class Invoke(
            override val location: Location,
            val expr: ExprI,
            val owner: TypeI,
            val returnType: TypeI,
            val name: Name,
            val args: List<Tuple2<ExprI, TypeI>>,
            val dispatch: Dispatch
    ) : ExprI() {
        enum class Dispatch {
            INTERFACE,
            VIRTUAL
        }
    }

    data class If(
            override val location: Location,
            val condition: ExprI,
            val expr1: ExprI,
            val expr2: ExprI
    ) : ExprI()

    data class Lambda(
            override val location: Location,
            val p: Package,
            val constraints: List<ConstraintI>,
            val argType: TypeI,
            val returnType: TypeI,
            val expr: ExprI,
            val closures: List<Tuple2<ExprI, Id>>
    ) : ExprI()
}
