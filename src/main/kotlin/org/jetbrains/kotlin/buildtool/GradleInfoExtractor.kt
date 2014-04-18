package org.jetbrains.kotlin.buildtool

import org.gradle.api.Project
import java.io.File
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.Action
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.Plugin

/**
 * Created by Nikita.Skvortsov
 * date: 18.04.2014.
 */

class ExtractorProject(val p: Project): Project by p {

    val exCont = LoggingExtensionContainer(p.getExtensions()!!, { p.getLogger()?.info(it) })

    override fun getExtensions(): ExtensionContainer = exCont


    override fun getPlugins() : PluginContainer =
            LoggingPluginContainer(p.getPlugins()!!, File("project_plugins.log"));
}


class LoggingExtensionContainer(val ex: ExtensionContainer, val log: (String) -> Unit): ExtensionContainer by ex {

    val ext : ExtraPropertiesExtension = LoggingExtPropertiesContainer(ex.getExtraProperties()!!, log)

    override fun add(p0: String?, p1: Class<out Any?>?, vararg p2: Any?) {
        log("[EX].add: name=$p0 class=$p1 args=$p2")
        ex.add(p0, p1, p2)
    }

    override fun <T> create(p0: String?, p1: Class<T>?, vararg p2: Any?): T? {
        log("[EX].create: name=$p0 class=$p1 args=$p2")
        return ex.create(p0, p1, p2)
    }

    override fun <T> configure(p0: Class<T>?, p1: Action<in T>?) {
        log("[EX].configure: name=$p0 action=$p1")
        return ex.configure(p0, p1)
    }

    override fun getExtraProperties(): ExtraPropertiesExtension? {
        return ext
    }

    override fun add(p0: String?, p1: Any?) {
        log("[EX].add: name=$p0 object=$p1")
        ex.add(p0, p1)
    }

}

class LoggingExtPropertiesContainer(val ext: ExtraPropertiesExtension, val log: (String) -> Unit): ExtraPropertiesExtension by ext {

    override fun has(p0: String?): Boolean {
        log("[EXT].has: $p0")
        return ext.has(p0)
    }

    override fun get(p0: String?): Any? {
        log("[EXT].get: $p0")
        return ext[p0]
    }

    override fun set(p0: String?, p1: Any?) {
        log("[EXT].set: $p0 - $p1")
        ext[p0] = p1
    }

    override fun getProperties(): MutableMap<String, Any>? {
        log("[EXT].getProperties")
        return ext.getProperties();
    }

}

class LoggingPluginContainer(val pc: PluginContainer, val report: File): PluginContainer by pc {

    override fun <T : Plugin<out Any?>?> getAt(p0: Class<T>?): T? {
        println("[PC].getAt: $p0")
        return pc.getAt(p0)
    }
    override fun apply(p0: String?): Plugin<out Any?>? {
        println("[PC].apply: $p0")
        return pc.apply(p0)
    }

    override fun <T : Plugin<out Any?>?> apply(p0: Class<T>?): T? {
        println("[PC].apply: $p0")
        return pc.apply(p0)
    }

    override fun hasPlugin(p0: String?): Boolean {
        println("[PC].hasPlugin: $p0")
        return pc.hasPlugin(p0)
    }

    override fun hasPlugin(p0: Class<out Plugin<out Any?>?>?): Boolean {
        println("[PC].hasPlugin: $p0")
        return pc.hasPlugin(p0)
    }

    override fun findPlugin(p0: String?): Plugin<out Any?>? {
        println("[PC].findPlugin: $p0")
        return pc.findPlugin(p0)
    }

    override fun <T : Plugin<out Any?>?> findPlugin(p0: Class<T>?): T? {
        println("[PC].findPlugin: $p0")
        return pc.findPlugin(p0)
    }

    override fun getPlugin(p0: String?): Plugin<out Any?>? {
        println("[PC].getPlugin: $p0")
        return pc.getPlugin(p0)
    }

    override fun <T : Plugin<out Any?>?> getPlugin(p0: Class<T>?): T? {
        println("[PC].getPlugin: $p0")
        return pc.getPlugin(p0)
    }

    override fun getAt(p0: String?): Plugin<out Any?>? {
        println("[PC].getAt: $p0")
        return pc.getAt(p0)
    }
}