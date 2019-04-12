package tlint

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

object FileUtils {
    fun relativePath(project: Project, absolutePath: VirtualFile): String? {
        return FileUtil.getRelativePath(File(project.basePath!!), File(absolutePath.path))
    }

    fun getExtensionWithDot(file: VirtualFile): String {
        var ext = StringUtil.notNullize(file.extension)
        if (!ext.startsWith(".")) {
            ext = ".$ext"
        }
        return ext
    }

    fun makeRelative(project: File, absolutePath: File): String? {
        return FileUtil.getRelativePath(project, absolutePath)
    }
}
