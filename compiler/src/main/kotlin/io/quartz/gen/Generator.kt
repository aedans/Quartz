package io.quartz.gen

import io.quartz.tree.ir.DeclI
import io.quartz.tree.util.Context

interface Generator {
    fun generate(declContext: Context<DeclI>)
}
