package org.jetbrains.kotlin.gradle

import com.google.common.io.Files
import com.intellij.openapi.util.SystemInfo
import java.io.File
import java.util.Arrays
import java.util.Scanner
import org.junit.Before
import org.junit.After
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.fail
import org.jetbrains.kotlin.gradle.BaseGradleIT
import org.jetbrains.kotlin.gradle.BaseGradleIT.Project

class BuildToolIT : BaseGradleIT(resourcesRoot = "src/integrationTest/resources") {

    Test fun testTrivialTask() {
        val project = Project("simpleProject", "1.11")

        project.build("doTask") {
            assertSuccessful()
            assertContains(":doTask", "Hello World!")
        }
    }

    Test fun testJavaBuild() {
        val project = Project("javaProject", "1.11")

        project.build("build", "-PextractInfo") {
            println(output)
            //assertSuccessful()
            //assertContains(":compileJava", ":jar", ":build")
        }
    }
}