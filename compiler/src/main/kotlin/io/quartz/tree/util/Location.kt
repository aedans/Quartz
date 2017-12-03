package io.quartz.tree.util

data class Location(val uri: String, val line: Int, val position: Int) {
    override fun toString() = "$uri:$line:$position"
}
