package tlint

import com.google.common.base.Charsets
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.openapi.util.Key
import java.io.File

object CLIRunner {
    fun createCommandLine(cwd: String, filePath: String, tlintPath: String): GeneralCommandLine {
        val commandLine = GeneralCommandLine()

        if (!File(cwd).exists()) {
            throw IllegalArgumentException("cwd doesn't exist")
        }
        commandLine.setWorkDirectory(cwd)

        if (!File("$cwd/$filePath").exists()) {
            throw IllegalArgumentException("filePath doesn't exist")
        }

        if (!File(tlintPath).exists()) {
            throw IllegalArgumentException("tlint path doesn't exist")
        }
        commandLine.exePath = tlintPath

        return commandLine
    }

    @Throws(ExecutionException::class)
    fun execute(commandLine: GeneralCommandLine, timeoutInMilliseconds: Int): ProcessOutput {
        val process = commandLine.createProcess()
        val processHandler = ColoredProcessHandler(process, commandLine.commandLineString, Charsets.UTF_8)
        val output = ProcessOutput()

        processHandler.addProcessListener(object : ProcessAdapter() {
            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                if (outputType == ProcessOutputTypes.STDERR) {
                    output.appendStderr(event.text)
                } else if (outputType != ProcessOutputTypes.SYSTEM) {
                    output.appendStdout(event.text)
                }
            }
        })

        processHandler.startNotify()

        if (processHandler.waitFor(timeoutInMilliseconds.toLong())) {
            output.exitCode = process.exitValue()
        } else {
            processHandler.destroyProcess()
            output.setTimeout()
        }

        if (output.isTimeout) {
            throw ExecutionException("Command '" + commandLine.commandLineString + "' is timed out.")
        }

        return output
    }
}