package tlint.cli;

import com.google.common.base.Strings;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import tlint.CLIRunner;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.TimeUnit;

public final class TLintRunner {
    private static final Logger LOG = Logger.getInstance(TLintRunner.class);

    private static final int TIME_OUT = (int) TimeUnit.SECONDS.toMillis(120L);

    public static class TLintSettings {
        String cwd;
        String filePath;
        String tlintPath;
    }

    public static TLintSettings buildSettings(@NotNull String cwd, @NotNull String filePath, @NotNull String tlintPath) {
        TLintSettings settings = new TLintSettings();
        settings.cwd = cwd;
        settings.filePath = filePath;
        settings.tlintPath = tlintPath;
        return settings;
    }

    public static LintResult lint(@NotNull TLintSettings settings) {
        LintResult result = new LintResult();

        try {
            GeneralCommandLine commandLine = createCommandLineLint(settings);
            ProcessOutput out = CLIRunner.execute(commandLine, TIME_OUT);
            result.errorOutput = out.getStderr();
            try {
                if (Strings.isNullOrEmpty(out.getStdout())) {
                    LOG.debug("TLint Empty Output");
                } else {
                    result.tLint = TLint.read(out.getStdout());
                }
            } catch (Exception e) {
                LOG.error(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.errorOutput = e.toString();
            Notifications.Bus.notify(new Notification("TLint plugin", "TLint plugin", result.errorOutput, NotificationType.INFORMATION));
        }
        return result;
    }

    @NotNull
    private static GeneralCommandLine createCommandLine(@NotNull TLintSettings settings) {
        return CLIRunner.createCommandLine(settings.cwd, settings.filePath, settings.tlintPath);
    }

    @NotNull
    private static GeneralCommandLine createCommandLineLint(@NotNull TLintSettings settings) {
        GeneralCommandLine commandLine = createCommandLine(settings);
        commandLine.addParameter("lint");
        commandLine.addParameter(settings.filePath);
        commandLine.addParameter("--json");
        return commandLine;
    }
}