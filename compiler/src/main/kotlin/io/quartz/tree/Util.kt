package io.quartz.tree

/**
 * @author Aedan Smith
 */

val nil = emptyList<Nothing>()

val <A, B> Pair<A, B>.swapped get() = second to first
