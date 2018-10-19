package tlint;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;

public final class FileUtils {
    public static String relativePath(Project project, VirtualFile absolutePath) {
        return FileUtil.getRelativePath(new File(project.getBasePath()), new File(absolutePath.getPath()));
    }

    public static String getExtensionWithDot(VirtualFile file){
        String ext = StringUtil.notNullize(file.getExtension());
        if (!ext.startsWith(".")) {
            ext = '.' + ext;
        }
        return ext;
    }

    public static String makeRelative(File project, File absolutePath) {
        return FileUtil.getRelativePath(project, absolutePath);
    }
}
