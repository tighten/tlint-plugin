package tlint;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ProjectComponent;
import org.jetbrains.annotations.NotNull;

public class TLintProjectComponent implements ProjectComponent {
    public String lintExecutable = System.getProperty("user.home") + "/.composer/vendor/bin/tlint";

    private static final String PLUGIN_NAME = "TLint plugin";

    @NotNull
    @Override
    public String getComponentName() {
        return "TLintProjectComponent";
    }

    public static void showNotification(String content, NotificationType type) {
        Notification errorNotification = new Notification(PLUGIN_NAME, PLUGIN_NAME, content, type);
        Notifications.Bus.notify(errorNotification);
    }
}