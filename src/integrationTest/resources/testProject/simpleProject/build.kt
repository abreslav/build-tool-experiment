import org.jetbrains.kotlin.buildtool.ProjectFacade
import org.gradle.api.Action
import org.gradle.api.Task

fun ProjectFacade.configure() {
    val task = task("doTask")
    task?.doFirst( object:Action<Task?> {
        override fun execute(t: Task?) {
            println("Hello World!")
        }
    })
}