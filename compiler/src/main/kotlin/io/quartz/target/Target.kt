package io.quartz.target

import io.quartz.env.Env
import io.quartz.gen.Generator
import java.io.File

interface Target {
    fun env(bp: List<File>): Env
    fun generator(out: File): Generator
}
