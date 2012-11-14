package org.jetbrains.jps.kotlin

import org.jetbrains.jps.Module
import org.jetbrains.jps.ModuleBuilder
import org.jetbrains.jps.ModuleBuildState
import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.Project
import groovy.io.FileType

/**
 * @author max
 */
class KotlinModuleBuilder implements ModuleBuilder {
    def processModule(ModuleBuildState state, ModuleChunk moduleChunk, Project project) {
        // TODO: examples do not compile %)
        if ("examples" == moduleChunk.getName()) return
        if ("example-vfs" == moduleChunk.getName()) return

        List<File> kotlinFiles = []
        state.sourceRoots.each {
            scanKotlinFiles(new File(it), kotlinFiles)
        }

        def ant = project.binding.ant

        if (!kotlinFiles.empty) {
            String kotlinHome = project.getPropertyIfDefined("kotlinHome")

            if (kotlinHome == null) {
                kotlinHome = detectKotlinHome(state.classpath)
            }

            if (kotlinHome == null) {
                ant.fail("Failed to detect Kotlin SDK for " + getDescription(moduleChunk) + ". Please either specify the SDK or set \"kotlinHome\" system property.")
            }

            if (!new File(kotlinHome, "lib/kotlin-compiler.jar").exists()) {
                ant.fail("\"$kotlinHome\" is not a valid Kotlin SDK. Can't find \"lib/kotlin-compiler.jar\" there.")
            }

            boolean debug = Boolean.parseBoolean(project.getPropertyIfDefined("kotlin.debug"))

            if (debug) {
                println("Following Kotlin SDK was detected: $kotlinHome")
            }

            ant.mkdir(dir: state.targetFolder)

            StringBuilder builder = new StringBuilder()
            builder.append("import kotlin.modules.*\n")
            builder.append("fun project() {\n")
            builder.append("module(\"${moduleChunk.name}\") {\n")

            state.sourceRoots.each {
                builder.append("sources += \"${path(it)}\"\n")
                builder.append("classpath += \"${path(it)}\"\n") // To find java files in same compilation scope
            }

            state.classpath.each {
                if (new File(it).exists()) {
                    builder.append("classpath += \"${path(it)}\"\n")
                    if (debug) {
                        println("[cp] " + path(it))
                    }
                }
            }

            project.libraries.each {
                it.value.annotationRoots.each {
                    builder.append("annotationsPath += \"${path(it)}\"\n")
                }
            }

            project.globalLibraries.each {
                it.value.annotationRoots.each {
                    builder.append("annotationsPath += \"${path(it)}\"\n")
                }
            }

            builder.append("}\n")
            builder.append("}\n")

            def moduleFile = new File(state.targetFolder, "module.kts")
            moduleFile.text = builder.toString()

            project.builder.buildInfoPrinter.printCompilationStart(project, "kotlinc")

            def jarName = "${state.targetFolder}/kt.jar"
            ant.java(classname: "org.jetbrains.jet.cli.jvm.K2JVMCompiler", fork: "true") {
                jvmarg(line: "-ea -Xmx300m -XX:MaxPermSize=200m" + (debug ? " -Dkotlin.print.cmd.args=true" : ""))

                arg(value: "-module")
                arg(value: "${moduleFile.absolutePath}")

                arg(value: "-jar")
                arg(value: jarName)

                arg(value: "-noStdlib")

                if (debug) {
                    arg(value: "-verbose")
                }

                classpath() {
                    fileset(dir: "$kotlinHome/lib") {
                        include(name: "*.jar")
                    }
                }
            }

            project.builder.buildInfoPrinter.printCompilationFinish(project, "kotlinc")

            moduleFile.delete();

            state.classpath << jarName
            ant.unjar(src: jarName, dest: state.targetFolder)
        }
    }

    String path(String raw) {
        return raw.replace('\\', '/')
    }

    def scanKotlinFiles(File file, List<File> answer) {
        file.eachFileRecurse(FileType.FILES) {
            if (it.getName().endsWith(".kt")) {
                answer.add(it)
            }
        }
    }

    String detectKotlinHome(List<String> classpath) {
        for (String path : classpath) {
            File file = new File(path)
            if (file.getName().equals("kotlin-compiler.jar")) {
                File libDir = file.getParentFile()
                if (libDir != null) {
                    File kotlinHome = libDir.getParentFile()
                    if (kotlinHome != null) {
                        return kotlinHome.getAbsolutePath()
                    }
                }
            }
        }
        return null
    }

    String getDescription(ModuleChunk chunk) {
        def modules = chunk.modules

        StringBuilder b = new StringBuilder()

        b << "module"
        if (modules.size() > 1) b << "s"
        b << " \""

        elements.eachWithIndex { Module it, int index ->
            if (index > 0) b << "\", \""
            b << it.name
        }

        b << "\""

        b.toString()
    }
}
