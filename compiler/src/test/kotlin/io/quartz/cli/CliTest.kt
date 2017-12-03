package io.quartz.cli

import com.xenomachina.argparser.*
import io.kotlintest.matchers.*
import io.kotlintest.specs.StringSpec
import io.quartz.nil
import io.quartz.target.*
import java.io.File

class CliTest : StringSpec() {
    init {
        "'--help' should throw ShowHelpException" {
            shouldThrow<ShowHelpException> {
                Options("--help").src
            }
        }

        "no options should throw a MissingRequiredPositionalArgumentException" {
            shouldThrow<MissingRequiredPositionalArgumentException> {
                Options().src
            }
        }

        "'a b c' should set the sources to [a, b, c]" {
            Options("a", "b", "c").src shouldEqual listOf("a", "b", "c").map { File(it) }
        }

        "'--sourcepath .;a;b;c' should set the sourcepath to [., a, b, c]" {
            Options("_", "--sourcepath", ".;a;b;c").sp shouldEqual listOf(".", "a", "b", "c").map { File(it) }
        }

        "'--binarypath .;a;b;c' should set the binarypath to [., a, b, c]" {
            Options("_", "--binarypath", ".;a;b;c").bp shouldEqual listOf(".", "a", "b", "c").map { File(it) }
        }

        "'--options .;a;b;c' should set the options to [., a, b, c]" {
            Options("_", "--options", ".;a;b;c").options shouldEqual listOf(".", "a", "b", "c")
        }

        targets.forEach { (name, target) ->
            "'--target $name' should set the target to $name" {
                Options("_", "--target", name).target shouldEqual target
            }
        }

        val default = Options("_")

        "the default target should be jvm" {
            default.target shouldEqual JvmTarget
        }

        "the default options should be nil" {
            default.options shouldEqual nil
        }

        "the default sourcepath should be ." {
            default.sp shouldEqual listOf(File("."))
        }

        "the default binarypath should be ." {
            default.bp shouldEqual listOf(File("."))
        }

        "the default out should be ." {
            default.out shouldEqual File(".")
        }
    }
}
