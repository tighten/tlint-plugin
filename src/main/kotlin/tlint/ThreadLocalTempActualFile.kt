package tlint

import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

import java.io.File
import java.io.IOException

/**
 * Lint target file thread local storage
 */
class ThreadLocalTempActualFile internal constructor(psiFile: PsiFile) : ThreadLocal<ThreadLocalTempActualFile.RelativeFile>() {
    private val baseName: String
    private val extension: String
    private val file: VirtualFile
    private val project: Project

    internal val orCreateFile: RelativeFile?
        get() {
            val path = super.get()
            if (path != null) {
                if (path.file.isFile) {
                    return path
                }
            }
            val file = createFile()
            if (file != null) {
                set(file)
                return file
            }
            return null
        }

    init {
        this.file = psiFile.virtualFile
        this.project = psiFile.project
        this.baseName = file.nameWithoutExtension
        this.extension = FileUtils.getExtensionWithDot(file)
    }

    private fun createFile(): RelativeFile? {
        // try to create a temp file in temp folder
        val dir = orCreateTempDir ?: return null
        val path = this.file.parent ?: return null;
        val tempParent = File(dir, FileUtils.relativePath(project, path)!!)
        var file = File(tempParent, this.file.name)
        FileUtil.createParentDirs(file)
        val relativeFile = RelativeFile(file)
        var created = false
        if (!file.exists()) {
            try {
                created = file.createNewFile()
            } catch (ignored: IOException) {
            }

        }
        if (!created) {
            try {
                file = FileUtil.createTempFile(dir, this.baseName, this.extension)
            } catch (e: IOException) {
                return null
            }

        }
        file.deleteOnExit()
        return relativeFile
    }

    class RelativeFile(val file: File)

    companion object {
        private const val TEMP_DIR_NAME = "intellij-tlint-temp"

        private val orCreateTempDir: File?
            get() {
                val tmpDir = File(FileUtil.getTempDirectory())
                val dir = File(tmpDir, TEMP_DIR_NAME)
                if (dir.isDirectory || dir.mkdirs()) {
                    return dir
                }
                try {
                    return FileUtil.createTempDirectory(tmpDir, TEMP_DIR_NAME, null)
                } catch (ignored: IOException) {
                    TLintProjectComponent.showNotification(
                            "Can't create '$TEMP_DIR_NAME' temporary directory.",
                            NotificationType.ERROR
                    )
                }

                return null
            }
    }
}
