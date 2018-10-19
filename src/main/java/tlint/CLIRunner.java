package tlint;

import com.google.common.base.Charsets;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import java.io.File;

public final class CLIRunner {
    /**
     * @param cwd working directory
     * @param node node interpreter path
     * @param exe node executable to run
     * @return command line to execute
     */
    @NotNull
    public static GeneralCommandLine createCommandLine(@NotNull String cwd, String filePath, String tlintPath) {
        GeneralCommandLine commandLine = new GeneralCommandLine();

        if (!new File(cwd).exists()) {
            throw new IllegalArgumentException("cwd doesn't exist");
        }
        commandLine.setWorkDirectory(cwd);

        if (!new File(cwd + "/" + filePath).exists()) {
            throw new IllegalArgumentException("filePath doesn't exist");
        }

        if (!new File(tlintPath).exists()) {
            throw new IllegalArgumentException("tlint path doesn't exist");
        }
        commandLine.setExePath(tlintPath);

        return commandLine;
    }

    @NotNull
    public static ProcessOutput execute(@NotNull GeneralCommandLine commandLine, int timeoutInMilliseconds) throws ExecutionException {
        Process process = commandLine.createProcess();
        OSProcessHandler processHandler = new ColoredProcessHandler(process, commandLine.getCommandLineString(), Charsets.UTF_8);
        final ProcessOutput output = new ProcessOutput();

        processHandler.addProcessListener(new ProcessAdapter() {
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                if (outputType.equals(ProcessOutputTypes.STDERR)) {
                    output.appendStderr(event.getText());
                } else if (!outputType.equals(ProcessOutputTypes.SYSTEM)) {
                    output.appendStdout(event.getText());
                }
            }
        });

        processHandler.startNotify();

        if (processHandler.waitFor(timeoutInMilliseconds)) {
            output.setExitCode(process.exitValue());
        } else {
            processHandler.destroyProcess();
            output.setTimeout();
        }

        if (output.isTimeout()) {
            throw new ExecutionException("Command '" + commandLine.getCommandLineString() + "' is timed out.");
        }

        return output;
    }
}