package tlint.inspection;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import tlint.TLintBundle;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.UnfairLocalInspectionTool;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TLintInspection extends LocalInspectionTool implements UnfairLocalInspectionTool {
    public static final String INSPECTION_SHORT_NAME = "TLintInspection";

    private static final Logger LOG = Logger.getInstance(TLintBundle.LOG_ID);

    @NotNull
    public String getDisplayName() {
        return "TLint";
    }

    @NotNull
    public String getShortName() {
        return INSPECTION_SHORT_NAME;
    }


    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
        Notification errorNotification = new Notification("TLint plugin", "TLint plugin", "aaaaa", NotificationType.INFORMATION);
        Notifications.Bus.notify(errorNotification);
        return ExternalAnnotatorInspectionVisitor.checkFileWithExternalAnnotator(file, manager, isOnTheFly, new TLintExternalAnnotator());
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        // works
        return new ExternalAnnotatorInspectionVisitor(holder, new TLintExternalAnnotator(), isOnTheFly);
    }

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element) {
        return false;
    }

    @NotNull
    @Override
    public SuppressQuickFix[] getBatchSuppressActions(@Nullable PsiElement element) {
        return new SuppressQuickFix[0];
    }
}