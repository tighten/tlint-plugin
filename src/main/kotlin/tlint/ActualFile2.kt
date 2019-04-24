package tlint

import com.intellij.notification.NotificationType
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiFile

import java.io.File
import java.io.IOException

/**
 * Process target file, either the real file or a temp file
 */
abstract class ActualFile2 internal constructor(open val actualFile: File) {

    open fun deleteTemp() {}

    class OriginalActualFile internal constructor(file: File) : ActualFile2(file)

    class TempActualFile internal constructor(file: File, private val tempFile: ThreadLocalTempActualFile.RelativeFile) : ActualFile2(file) {

        override val actualFile: File
            get() = tempFile.file

        override fun deleteTemp() {
            val temp = tempFile.file
            if (temp.exists() && temp.isFile) {
                val isDeleted = temp.delete()

                if (!isDeleted) {
                    TLintProjectComponent.showNotification(
                            "Failed to delete temp file",
                            NotificationType.ERROR
                    )
                }
            }
        }
    }

    companion object {

        fun getOrCreateActualFile(key: Key<ThreadLocalTempActualFile>, psiFile: PsiFile, content: String?): ActualFile2? {
            if (content == null) {
                return null
            }

            // Original file
            val virtualFile = psiFile.virtualFile
            val fileDocumentManager = FileDocumentManager.getInstance()
            if (!fileDocumentManager.isFileModified(virtualFile)) {
                val file = File(virtualFile.path)
                if (file.isFile) {
                    return OriginalActualFile(file)
                }
            }

            // TEMP File
            var threadLocal: ThreadLocalTempActualFile? = key.get(virtualFile)
            if (threadLocal == null) {
                threadLocal = virtualFile.putUserDataIfAbsent(key, ThreadLocalTempActualFile(psiFile))
            }
            val file = threadLocal.orCreateFile ?: return null

            try {
                FileUtil.writeToFile(file.file, content)
                return TempActualFile(File(virtualFile.path), file)
            } catch (e: IOException) {
                TLintProjectComponent.showNotification(
                        "Can not write to " + file.file.absolutePath,
                        NotificationType.ERROR
                )
            }

            return null
        }
    }
}
