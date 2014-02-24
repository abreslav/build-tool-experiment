package org.jetbrains.kotlin.buildtool

import org.gradle.api.Project
import org.gradle.api.Plugin


open class KotlinBuildtoolPlugin: Plugin<Project> {

    override fun apply(p0: Project?) {
        val project = p0!!
        val rootProject = project.getRootProject()
        val gradle = rootProject.getGradle()

        // init compiler

          // compiler dependencies
          // gradle API


        // compile build.kt

        // execute compiled code against RootProject and Gradle objects
        // gradle runtime as the dependency


        throw UnsupportedOperationException()
    }

}
