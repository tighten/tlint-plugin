package tlint;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Lint target file thread local storage
 */
public class ThreadLocalTempActualFile extends ThreadLocal<ThreadLocalTempActualFile.RelativeFile> {
    private final String baseName;
    private final String extension;
    private final VirtualFile file;
    private final Project project;
    private static final String TEMP_DIR_NAME = "intellij-tlint-temp";

    ThreadLocalTempActualFile(@NotNull PsiFile psiFile) {
        this.file = psiFile.getVirtualFile();
        this.project = psiFile.getProject();
        this.baseName = file.getNameWithoutExtension();
        this.extension = FileUtils.getExtensionWithDot(file);
    }

    @Nullable
    RelativeFile getOrCreateFile() {
        RelativeFile path = super.get();
        if (path != null) {
            if (path.file.isFile()) {
                return path;
            }
        }
        RelativeFile file = createFile();
        if (file != null) {
            set(file);
            return file;
        }
        return null;
    }

    @Nullable
    private static File getOrCreateTempDir() {
        File tmpDir = new File(FileUtil.getTempDirectory());
        File dir = new File(tmpDir, TEMP_DIR_NAME);
        if (dir.isDirectory() || dir.mkdirs()) {
            return dir;
        }
        try {
            return FileUtil.createTempDirectory(tmpDir, TEMP_DIR_NAME, null);
        } catch (IOException ignored) {
            TLintProjectComponent.showNotification(
                    "Can't create '" + TEMP_DIR_NAME + "' temporary directory.",
                    NotificationType.ERROR
            );
        }
        return null;
    }

    @Nullable
    private RelativeFile createFile() {
        // try to create a temp file in temp folder
        File dir = getOrCreateTempDir();
        if (dir == null) {
            return null;
        }
        File tempParent = new File(dir, FileUtils.relativePath(project, this.file.getParent()));
        File file = new File(tempParent, this.file.getName());
        FileUtil.createParentDirs(file);
        RelativeFile relativeFile = new RelativeFile(dir, file);
        boolean created = false;
        if (!file.exists()) {
            try {
                created = file.createNewFile();
            } catch (IOException ignored) {
            }
        }
        if (!created) {
            try {
                file = FileUtil.createTempFile(dir, this.baseName, this.extension);
            } catch (IOException e) {
                return null;
            }
        }
        file.deleteOnExit();
        return relativeFile;
    }

    static class RelativeFile {
        final File root;
        final File file;

        RelativeFile(File root, File file) {
            this.root = root;
            this.file = file;
        }
    }
}
