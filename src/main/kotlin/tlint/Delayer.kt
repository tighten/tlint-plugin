package tlint

import java.util.Date

class Delayer(private val timeOut: Long) {
    private var lastNotification: Date? = null

    fun done() {
        lastNotification = Date()
    }

    fun should(): Boolean {
        if (lastNotification == null) {
            return true
        }
        val now = Date()
        val delta = now.time - lastNotification!!.time
        return delta > timeOut
    }
}