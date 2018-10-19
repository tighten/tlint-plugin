package tlint;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Process target file, either the real file or a temp file
 */
public abstract class ActualFile2 {
    ActualFile2(File file) {
        this.file = file;
    }

    private final File file;

    public File getActualFile() {
        return file;
    }

    public void deleteTemp() {
    }

    @Nullable
    public static ActualFile2 getOrCreateActualFile(@NotNull Key<ThreadLocalTempActualFile> key, @NotNull PsiFile psiFile, @Nullable String content) {
        // Original file
        VirtualFile virtualFile = psiFile.getVirtualFile();
        FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
        if (!fileDocumentManager.isFileModified(virtualFile)) {
            File file = new File(virtualFile.getPath());
            if (file.isFile()) {
                return new OriginalActualFile(file);
            }
        }

        // TEMP File
        ThreadLocalTempActualFile threadLocal = key.get(virtualFile);
        if (threadLocal == null) {
            threadLocal = virtualFile.putUserDataIfAbsent(key, new ThreadLocalTempActualFile(psiFile));
        }
        ThreadLocalTempActualFile.RelativeFile file = threadLocal.getOrCreateFile();
        if (file == null) {
            return null;
        }
        if (content == null) {
            Document document = fileDocumentManager.getDocument(virtualFile);
            if (document != null) {
                content = document.getText();
            }
        }
        if (content == null) {
            return null;
        }
        try {
            FileUtil.writeToFile(file.file, content);
            return new TempActualFile(new File(virtualFile.getPath()), file);
        } catch (IOException e) {
            TLintProjectComponent.showNotification(
                "Can not write to " + file.file.getAbsolutePath(),
                NotificationType.ERROR
            );
        }
        return null;
    }

    public static class OriginalActualFile extends ActualFile2 {
        OriginalActualFile(File file) {
            super(file);
        }
    }

    public static class TempActualFile extends ActualFile2 {
        private final ThreadLocalTempActualFile.RelativeFile tempFile;

        TempActualFile(File file, ThreadLocalTempActualFile.RelativeFile tempFile) {
            super(file);
            this.tempFile = tempFile;
        }

        @Override
        public File getActualFile() {
            return tempFile.file;
        }

        @Override
        public void deleteTemp() {
            File temp = tempFile.file;
            if (temp != null && temp.exists() && temp.isFile()) {
                boolean isDeleted = temp.delete();

                if (!isDeleted) {
                    TLintProjectComponent.showNotification(
                            "Failed to delete temp file",
                            NotificationType.ERROR
                    );
                }
            }
        }
    }
}
