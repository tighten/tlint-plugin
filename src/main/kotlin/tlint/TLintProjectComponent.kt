package tlint

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.components.ProjectComponent

class TLintProjectComponent : ProjectComponent {
    override fun getComponentName(): String {
        return "TLintProjectComponent"
    }

    companion object {
        private const val PLUGIN_NAME = "TLint plugin"

        fun showNotification(content: String, type: NotificationType) {
            val errorNotification = Notification(PLUGIN_NAME, PLUGIN_NAME, content, type)
            Notifications.Bus.notify(errorNotification)
        }
    }
}