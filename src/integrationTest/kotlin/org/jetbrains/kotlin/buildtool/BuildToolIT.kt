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

class BuildToolIT {

    var workingDir: File = File(".")

    Before fun setUp() {
        workingDir = Files.createTempDir()
        workingDir.mkdirs()
    }

    After fun tearDown() {
        deleteRecursively(workingDir)
    }

    Test fun testTrivialTask() {
        val project = Project("simpleProject")

        project.build("doTask") {
            assertSuccessful()
            assertContains(":doTask", "Hello World!")
        }
    }

    class Project(val projectName: String)

    class CompiledProject(val project: Project, val output: String, val resultCode: Int)

    fun Project.build(command: String, check: CompiledProject.() -> Unit) {
        copyRecursively(File("src/integrationTest/resources/$projectName"), workingDir)
        val projectDir = File(workingDir, projectName)
        val cmd = createCommand(command)
        val process = createProcess(cmd, projectDir)

        val (output, resultCode) = readOutput(process)
        CompiledProject(this, output, resultCode).check()
    }

    private fun CompiledProject.assertSuccessful(): CompiledProject {
        assertEquals(0, resultCode)
        return this
    }

    private fun CompiledProject.assertContains(vararg expected: String): CompiledProject {
        for (str in expected) {
            assertTrue(output.contains(str), "Should contain '$str', actual output: $output")
        }
        return this
    }

    private fun CompiledProject.assertReportExists(): CompiledProject {
        assertReportExists("")
        return this
    }
    private fun CompiledProject.assertReportExists(subProject: String): CompiledProject {
        assertTrue(File(File(File(workingDir, project.projectName), subProject), "build/reports/tests/demo.TestSource.html").exists(), "Test report does not exist. Were tests executed?")
        return this
    }

    private fun createCommand(taskName: String): List<String> {
        val pathToBuildToolPlugin = "-PpathToBuildToolPlugin=" + File("build/libs/kotlin-build-tool.jar").getAbsolutePath()

        return if (SystemInfo.isWindows)
            listOf("cmd", "/C", "gradlew.bat", taskName, pathToBuildToolPlugin, "--no-daemon", "--debug")
        else
            listOf("/bin/bash", "./gradlew", taskName, pathToBuildToolPlugin, "--no-daemon", "--debug")
    }

    private fun createProcess(cmd: List<String>, projectDir: File): Process {
        val builder = ProcessBuilder(cmd)
        builder.directory(projectDir)
        builder.redirectErrorStream(true)
        return builder.start()
    }

    private fun readOutput(process: Process): Pair<String, Int> {
        val s = Scanner(process.getInputStream()!!)
        val text = StringBuilder()
        while (s.hasNextLine()) {
            text append s.nextLine()
            text append "\n"
        }
        s.close()

        val result = process.waitFor()
        return text.toString() to result
    }

    fun copyRecursively(source: File, target: File) {
        assertTrue(target.isDirectory())
        val targetFile = File(target, source.getName())
        if (source.isDirectory()) {
            targetFile.mkdir()
            val array = source.listFiles()
            if (array != null) {
                for (child in array) {
                    copyRecursively(child, targetFile)
                }
            }
        } else {
            Files.copy(source, targetFile)
        }
    }

    fun deleteRecursively(f: File): Unit {
        if (f.isDirectory()) {
            val children = f.listFiles()
            if (children != null) {
                for (child in children) {
                    deleteRecursively(child)
                }
            }
            val shouldBeEmpty = f.listFiles()
            if (shouldBeEmpty != null) {
                assertTrue(shouldBeEmpty.isEmpty())
            } else {
                fail("Error listing directory content")
            }
        }
        f.delete()
    }
}