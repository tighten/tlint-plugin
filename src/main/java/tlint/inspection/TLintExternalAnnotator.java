package tlint.inspection;

import tlint.TLintBundle;
import tlint.TLintProjectComponent;
import tlint.cli.TLint;
import tlint.cli.TLintRunner;
import tlint.cli.LintResult;
import com.intellij.codeInsight.daemon.impl.SeverityRegistrar;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import tlint.ActualFile2;
import tlint.ThreadLocalTempActualFile;
import tlint.ExternalLintAnnotationInput;
import tlint.ExternalLintAnnotationResult;
import tlint.InspectionUtil;
import tlint.Delayer;
import tlint.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class TLintExternalAnnotator extends ExternalAnnotator<ExternalLintAnnotationInput, ExternalLintAnnotationResult<LintResult>> {

    private static final Logger LOG = Logger.getInstance(TLintBundle.LOG_ID);
    private static final String MESSAGE_PREFIX = "TLint: ";
    private static final Key<ThreadLocalTempActualFile> T_LINT_TEMP_FILE = Key.create("T_LINT_TEMP_FILE");

    @Nullable
    @Override
    public ExternalLintAnnotationInput collectInformation(@NotNull PsiFile file) {
        return collectInformation(file, null);
    }

    @Nullable
    @Override
    public ExternalLintAnnotationInput collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        return collectInformation(file, editor);
    }

    @Override
    public void apply(@NotNull PsiFile file, ExternalLintAnnotationResult<LintResult> annotationResult, @NotNull AnnotationHolder holder) {
        if (annotationResult == null) {
            return;
        }
        InspectionProjectProfileManager inspectionProjectProfileManager = InspectionProjectProfileManager.getInstance(file.getProject());
        SeverityRegistrar severityRegistrar = inspectionProjectProfileManager.getSeverityRegistrar();
        EditorColorsScheme colorsScheme = annotationResult.input.colorsScheme;

        Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        if (document == null) {
            return;
        }

        for (TLint.Issue warn : annotationResult.result.tLint.file.errors) {
            HighlightSeverity severity = HighlightSeverity.WARNING;

            TextAttributes forcedTextAttributes = InspectionUtil.getTextAttributes(colorsScheme, severityRegistrar, severity);

            createAnnotation(holder, document, warn, severity, forcedTextAttributes);
        }
    }

    @Nullable
    private static Annotation createAnnotation(
            @NotNull AnnotationHolder holder,
            @NotNull Document document,
            @NotNull TLint.Issue warn,
            @NotNull HighlightSeverity severity,
            @Nullable TextAttributes forcedTextAttributes
    ) {
        int line = warn.line - 1;

        if (line < 0 || line >= document.getLineCount()) {
            return null;
        }

        int lineEndOffset = document.getLineEndOffset(line);
        int lineStartOffset = document.getLineStartOffset(line);

        TextRange range;

        range = new TextRange(lineStartOffset, lineEndOffset);

        return InspectionUtil.createAnnotation(holder, severity, forcedTextAttributes, range, MESSAGE_PREFIX + warn.message.trim() + " (" + warn.source + ')');
    }

    @Nullable
    private static ExternalLintAnnotationInput collectInformation(@NotNull PsiFile psiFile, @Nullable Editor editor) {
        Project project = psiFile.getProject();

        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);

        String fileContent = document.getText();

        EditorColorsScheme colorsScheme = editor != null ? editor.getColorsScheme() : null;

        return new ExternalLintAnnotationInput(psiFile, fileContent, colorsScheme);
    }

    @Nullable
    @Override
    public ExternalLintAnnotationResult<LintResult> doAnnotate(ExternalLintAnnotationInput collectedInfo) {
        ActualFile2 actualCodeFile = null;

        try {
            PsiFile file = collectedInfo.psiFile;
            TLintProjectComponent component = file.getProject().getComponent(TLintProjectComponent.class);

            actualCodeFile = ActualFile2.getOrCreateActualFile(T_LINT_TEMP_FILE, file, collectedInfo.fileContent);

            String relativeFile;
            relativeFile = FileUtils.makeRelative(new File(file.getProject().getBasePath()), actualCodeFile.getActualFile());

            LintResult result = TLintRunner.lint(TLintRunner.buildSettings(file.getProject().getBasePath(), relativeFile, component.lintExecutable));

            actualCodeFile.deleteTemp();

            return new ExternalLintAnnotationResult<>(collectedInfo, result);
        } catch (Exception e) {
            LOG.error("Error running TLint inspection: ", e);

            showNotificationError("Error running TLint inspection: " + e.getMessage());
        } finally {
            if (actualCodeFile != null) {
                actualCodeFile.deleteTemp();
            }
        }

        return null;
    }

    private final Delayer delayer = new Delayer(TimeUnit.SECONDS.toMillis(5L));

    private void showNotificationError(String content) {
        if (delayer.should()) {
            TLintProjectComponent.showNotification(content, NotificationType.ERROR);
            delayer.done();
        }
    }
}