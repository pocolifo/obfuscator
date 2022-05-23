# The Pocolifo Java Obfuscator

The "just works," no bloat, stable, crazy fast obfuscator with zero configuration necessary, top tier code
mangling, and just about every feature you can think of.

## Project Structure

This is a repository contains several projects that relate to the Pocolifo Obfuscator.

### `com.pocolifo.obfuscator.engine` / Obfuscator
Contains the source code for the actual obfuscation engine.

### `com.pocolifo.obfuscator.cli` / CLI
Contains the source code for the command line interface for the obfuscation engine.

### `com.pocolifo.obfuscator.annotations` / Annotations
Contains annotations that allow you to configure the obfuscator inside your Java source code.

### `com.pocolifo.obfuscator.testproject` / Testing
A demo project that is used to test the obfuscation engine to see if it works.

### `com.pocolifo.obfuscator.gradleplugin` / Gradle Plugin Interface
A Gradle plugin to interface with the obfuscation engine.

### `com.pocolifo.obfuscator.webservice` / Web Service

A website (programmed in Go) to interface with the obfuscator engine.

## Requirements
### To run
- A Java 8 compatible JVM & JRE
- An interface for the obfuscation engine (CLI, Gradle plugin)
- A JAR to obfuscate

### To develop
- A Java 8 compatible JVM
- A JDK for Java 8