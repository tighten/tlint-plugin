package tlint.cli

import com.google.common.base.Strings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.diagnostic.Logger
import tlint.CLIRunner
import java.util.concurrent.TimeUnit

object TLintRunner {
    private val LOG = Logger.getInstance(TLintRunner::class.java)

    private val TIME_OUT = TimeUnit.SECONDS.toMillis(120L).toInt()

    class TLintSettings {
        internal var cwd: String = ""
        internal var filePath: String = ""
        internal var tlintPath: String = ""
    }

    fun buildSettings(cwd: String, filePath: String, tlintPath: String): TLintSettings {
        val settings = TLintSettings()
        settings.cwd = cwd
        settings.filePath = filePath
        settings.tlintPath = tlintPath
        return settings
    }

    fun lint(settings: TLintSettings): LintResult {
        val result = LintResult()

        try {
            val commandLine = createCommandLineLint(settings)
            val out = CLIRunner.execute(commandLine, TIME_OUT)
            result.errorOutput = out.stderr
            try {
                if (Strings.isNullOrEmpty(out.stdout)) {
                    LOG.debug("TLint Empty Output")
                } else {
                    result.tLint = TLint.read(out.stdout)
                }
            } catch (e: Exception) {
                LOG.error(e)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            result.errorOutput = e.toString()
            Notifications.Bus.notify(Notification("TLint plugin", "TLint plugin", result.errorOutput!!, NotificationType.INFORMATION))
        }

        return result
    }

    private fun createCommandLine(settings: TLintSettings): GeneralCommandLine {
        return CLIRunner.createCommandLine(settings.cwd, settings.filePath, settings.tlintPath)
    }

    private fun createCommandLineLint(settings: TLintSettings): GeneralCommandLine {
        if (settings.cwd == "" && settings.filePath == "" && settings.tlintPath == "") {
            throw Exception("Invalid Settings")
        }

        val commandLine = createCommandLine(settings)
        commandLine.addParameter("lint")
        commandLine.addParameter(settings.filePath)
        commandLine.addParameter("--json")
        return commandLine
    }
}