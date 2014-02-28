package org.jetbrains.kotlin.gradle.buildtool

import org.gradle.api.Project
import org.gradle.api.Action
import org.gradle.api.Task


open class MyBuild {
    public fun doBuild(project: Project) {
        val task = project.task("doTask")
        task?.doFirst( object:Action<Task?> {
            override fun execute(t: Task?) {
                println("Hello World!")
            }
        })
    }
}