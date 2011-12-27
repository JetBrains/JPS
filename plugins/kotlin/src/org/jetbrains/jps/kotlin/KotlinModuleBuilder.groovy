package org.jetbrains.jps.kotlin

import org.jetbrains.jps.ModuleBuilder
import org.jetbrains.jps.ModuleBuildState
import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.Project
import groovy.io.FileType
import org.jetbrains.jps.builders.BuildUtil

/**
 * @author max
 */
class KotlinModuleBuilder implements ModuleBuilder {
    def processModule(ModuleBuildState state, ModuleChunk moduleChunk, Project project) {
        List<File> kotlinFiles = []
        state.sourceRoots.each {
            scanKotlinFiles(new File(it), kotlinFiles)
        }

        if (!kotlinFiles.empty) {
            String kotlinHome = project.kotlinHome

            if (kotlinHome == null) {
                ant.fail("kotlinHome is not defined")
            }

            if (!new File(kotlinHome, "lib/kotlin-compiler.jar").exists()) {
                ant.fail("'$kotlinHome' is not a valid Kotlin compiler. Can't find lib/kotlin-compiler.jar there")
            }
            
            String stubsRoot = createTempDir(state, project)

            StringBuilder builder = new StringBuilder()
            builder.append("import kotlin.modules.*\n")
            builder.append("val modules = module(\"${moduleChunk.name}\") {\n")
            
            kotlinFiles.each {
                builder.append("source files \"${it.absolutePath}\"\n")
            }
            
            state.classpath.each {
                builder.append("classpath entry \"${it}\"")
            }
            
            builder.append("jar name \"$stubsRoot/kt.jar\"")
            
            builder.append("}")

            def moduleFile = new File(stubsRoot, "module.kts")
            moduleFile.text = builder.toString()
            
            ant.java(classname: "org.jetbrains.jet.cli.KotlinCompiler", fork: "true") {
                jvmarg(line: "-ea -Xmx300m -XX:MaxPermSize=200m")

                arg(value: "-module")
                arg(value: "${moduleFile.absolutePath}")
                
                classpath() {
                    fileset(dir: "$kotlinHome/lib") {
                        include(name: "*.jar")
                    }
                }
            }
            
            state.classpath << "$stubsRoot/kt.jar"
            ant.unjar(src: "$stubsRoot/kt.jar", dest: state.targetFolder)
        }
    }

    private String createTempDir(ModuleBuildState state, Project project) {
        String targetFolder = project.targetFolder
        
        File sutbsDir = new File(targetFolder != null ? targetFolder : ".", "___temp___")
        def stubsRoot = sutbsDir.getAbsolutePath()

        BuildUtil.deleteDir(project, stubsRoot)
        ant.mkdir(dir: sutbsDir)

        state.tempRootsToDelete << stubsRoot
        return stubsRoot
    }

    def scanKotlinFiles(File file, List<File> answer) {
        file.eachFileRecurse(FileType.FILES) {
            if (it.getName().endsWith(".kt")) {
                answer.add(it)
            }
        }
    }
}
