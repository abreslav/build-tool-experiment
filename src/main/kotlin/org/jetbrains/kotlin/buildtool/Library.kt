package org.jetbrains.kotlin.buildtool

import org.gradle.api.Project

/**
 * Created by Nikita.Skvortsov
 * date: 21.03.2014.
 */

open class ProjectFacade(val p: Project) : Project by p {
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


