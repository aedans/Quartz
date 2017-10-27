package io.quartz.tree

/**
 * @author Aedan Smith
 */

interface Locatable {
    val location: Location
}

data class Location(val file: String, val line: Int, val position: Int) {
    override fun toString() = "$file:$line, $position"

    companion object {
        val unknown = Location("unknown", -1, -1)
    }
}
