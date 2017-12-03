package io.quartz.target

import io.quartz.env.jvm.JvmEnv
import io.quartz.gen.jvm.JvmGenerator
import java.io.File

object JvmTarget : Target {
    override fun env(bp: List<File>) = JvmEnv(bp)
    override fun generator(out: File) = JvmGenerator(out)
}
