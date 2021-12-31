package com.test

import java.io.File
import java.net.URL
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.templates.standard.SimpleScriptTemplate

fun main() {
    val mainScript = StringScriptSource("sharedVar += 1\nprintln(\"sharedVar == \$sharedVar\")", "main.kts")

    val userHome = System.getProperty("user.home")

    val middleScript = object : FileBasedScriptSource() {
        override val file = File("$userHome/IdeaProjects/script-import-test/diamondImportMiddle.kts")
        override val name: String = file.name
        override val text: String = file.readText()
        override val locationId: String = file.path
        override val externalLocation: URL = file.toURI().toURL()
    }

    val commonScript = object : FileBasedScriptSource() {
        override val file = File("$userHome/IdeaProjects/script-import-test/diamondImportCommon.kts")
        override val name: String = file.name
        override val text: String = file.readText()
        override val locationId: String = file.path
        override val externalLocation: URL = file.toURI().toURL()
    }

    val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate> {
        refineConfiguration {
            beforeCompiling { ctx ->
                when (ctx.script.name) {
                    "main.kts" -> ScriptCompilationConfiguration(ctx.compilationConfiguration) {
                        importScripts(middleScript, commonScript)
                    }
                    "diamondImportMiddle.kts" -> ScriptCompilationConfiguration(ctx.compilationConfiguration) {
                        importScripts(commonScript)
                    }
                    else -> ctx.compilationConfiguration
                }.asSuccess()
            }
        }
    }

    val evaluationConfiguration = ScriptEvaluationConfiguration {
        enableScriptsInstancesSharing()
    }

    BasicJvmScriptingHost().eval(mainScript, compilationConfiguration, evaluationConfiguration).throwOnFailure()
}

fun <T> ResultWithDiagnostics<T>.throwOnFailure(): ResultWithDiagnostics<T> = apply {
    if (this is ResultWithDiagnostics.Failure) {
        val firstExceptionFromReports = reports.find { it.exception != null }?.exception
        throw Exception(
            "Compilation/evaluation failed:\n  ${reports.joinToString("\n  ") { it.exception?.toString() ?: it.message }}",
            firstExceptionFromReports
        )
    }
}