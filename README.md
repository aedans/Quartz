Quartz
======

A functional programming language for the JVM.

Compiler
-------

Quartz is currently in very early alpha. Breaking changes are being made daily, multiple features 
are being added weekly, and there is no documentation to speak of. The only working code samples 
are in the unit tests (./compiler/src/test/kotlin).

The compiler is written in purely functional [Kotlin](https://kotlinlang.org/) using
[Kategory](https://github.com/kategory/kategory), and the standard library is written in Java.
As the language matures, the compiler and the standard library will be rewritten in Quartz, 
until they are entirely self-hosting. 

Building
--------

Quartz uses Gradle as its build system. Cloning this repository and running `./gradlew build`
will build the compiler, and `./gradlew test` will run the unit tests.

Running
-------

// TODO Add REPL
