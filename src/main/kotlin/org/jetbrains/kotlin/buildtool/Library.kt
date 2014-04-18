package org.jetbrains.kotlin.buildtool

import org.gradle.api.Project
import org.gradle.util.Configurable
import groovy.lang.Closure
import org.gradle.api.plugins.ExtensionAware

/**
 * Created by Nikita.Skvortsov
 * date: 21.03.2014.
 */

open class ProjectFacade(val p: Project): Project by p {
    val apply = Applicator(p)
}

open class Applicator(val p: Project) {
    fun plugin(name: String) {
        p.apply(hashMapOf("plugin" to name))
    }

    fun from(url: String) {
        p.apply(hashMapOf("from" to url))
    }
}

/*

fun <T> Configurable<T>.configure(f: T.() -> Unit): T {
    return this.configure(object : Closure<T>(f, this) {
        override fun call(vararg args: Any?): T? {
            if (args.size > 0) {
                val arg = args[0] as T
                arg.f()
                return arg
            }
            return null
        }
    })!!
}
*/

