package org.jetbrains.kotlin.buildtool

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.GradleException
import org.jetbrains.jet.cli.common.ExitCode
import java.io.File
import org.jetbrains.jet.cli.common.arguments.K2JVMCompilerArguments
import java.util.HashSet
import java.util.ArrayList
import org.apache.commons.lang.StringUtils
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.specs.Spec
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.jetbrains.jet.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.jet.cli.common.messages.CompilerMessageLocation
import org.jetbrains.jet.cli.common.messages.MessageCollector
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import com.google.common.io.Files
import org.apache.commons.io.FileUtils
import org.jetbrains.jet.cli.jvm.K2JVMCompiler
import java.net.URLClassLoader


open class KotlinBuildtoolPlugin: Plugin<Project> {

    override fun apply(project: Project) {

        val tempDir = Files.createTempDir()!!

        try {
            val logger = Logging.getLogger(getClass())
            val rootProject = project.getRootProject()
            val gradle = rootProject.getGradle()

            // init compiler
            val args = K2JVMCompilerArguments()
            // source
            val sources: List<String> = listOf("build.kt")
            args.src = sources.makeString(File.pathSeparator)

            val gradleUtils = GradleUtils(project.getBuildscript()!!)
            // classpath
            val gradleApi = project.getConfigurations()!!.detachedConfiguration(project.getDependencies()?.gradleApi())!!
            val gradleAPIpaths = gradleApi.resolve()!! map { it.getAbsolutePath() }
            val kotlinStdLibPaths = gradleUtils.resolveDependencies("org.jetbrains.kotlin:kotlin-stdlib:0.1-SNAPSHOT")!! map { it.getAbsolutePath() }
            args.classpath = (gradleAPIpaths + kotlinStdLibPaths) makeString File.pathSeparator

            // output
            args.outputDir = tempDir.getAbsolutePath()

            // annotations
            val annotationsFiles = gradleUtils.resolveDependencies("org.jetbrains.kotlin:kotlin-jdk-annotations:0.6+")
            args.annotations = annotationsFiles map { it.getAbsolutePath() } makeString File.pathSeparator

            // config
            args.noStdlib = true
            args.noJdkAnnotations = true

            // logging
            val messageCollector = GradleMessageCollector(logger)

            // compile build.kt
            val compiler = K2JVMCompiler()
            val exitCode = compiler.exec(messageCollector, args)
            when (exitCode) {
                ExitCode.COMPILATION_ERROR -> throw GradleException("Compilation error. See log for more details")
                ExitCode.INTERNAL_ERROR -> throw GradleException("Internal compiler error. See log for more details")
                else -> {}
            }

            val arrayOfURLs = array(tempDir.toURI().toURL())

            val builderLoader = URLClassLoader(arrayOfURLs, getClass().getClassLoader())

            // load class
            val cls = builderLoader.loadClass("org.jetbrains.kotlin.gradle.buildtool.MyBuild")!!
            val builder = cls.newInstance()

            // execute compiled code against RootProject and Gradle objects
            val entryPoint = cls.getDeclaredMethod("doBuild", javaClass<Project>())
            entryPoint.invoke(builder, project)
        } finally {
            FileUtils.deleteDirectory(tempDir)
        }
    }

}

class GradleMessageCollector(val logger : Logger): MessageCollector {
    public override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation) {
        val path = location.getPath()
        val hasLocation = path != null && location.getLine() > 0 && location.getColumn() > 0
        val text: String
        if (hasLocation) {
            val warningPrefix = if (severity == CompilerMessageSeverity.WARNING) "warning:" else ""
            val errorMarkerLine = "${" ".repeat(location.getColumn() - 1)}^"
            text = "$path:${location.getLine()}:$warningPrefix$message\n${errorMarkerLine}"
        }
        else {
            text = "${severity.name().toLowerCase()}:$message"
        }
        when (severity) {
            in CompilerMessageSeverity.VERBOSE -> logger.debug(text)
            in CompilerMessageSeverity.ERRORS -> logger.error(text)
            CompilerMessageSeverity.INFO -> logger.info(text)
            CompilerMessageSeverity.WARNING -> logger.warn(text)
            else -> throw IllegalArgumentException("Unknown CompilerMessageSeverity: $severity")
        }
    }
}

open class GradleUtils(val scriptHandler: ScriptHandler) {

    public fun resolveDependencies(vararg coordinates: String): Collection<File> {
        val dependencyHandler : DependencyHandler = scriptHandler.getDependencies()
        val configurationsContainer : ConfigurationContainer = scriptHandler.getConfigurations()

        val deps = coordinates.map { dependencyHandler.create(it) }
        val configuration = configurationsContainer.detachedConfiguration(*deps.copyToArray())

        return configuration.getResolvedConfiguration().getFiles(KSpec({ dep -> true }))!!
    }
}

open class KSpec<T: Any?>(val predicate: (T) -> Boolean): Spec<T> {
    public override fun isSatisfiedBy(p0: T?): Boolean {
        return p0 != null && predicate(p0)
    }
}
