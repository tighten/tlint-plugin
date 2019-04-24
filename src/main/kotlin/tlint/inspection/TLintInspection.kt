package tlint.inspection

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.codeInspection.*
import com.intellij.codeInspection.ex.UnfairLocalInspectionTool
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import tlint.ExternalLintAnnotationInput
import tlint.ExternalLintAnnotationResult
import tlint.cli.LintResult

class TLintInspection : LocalInspectionTool(), UnfairLocalInspectionTool {

    override fun getDisplayName(): String {
        return "TLint"
    }

    override fun getShortName(): String {
        return INSPECTION_SHORT_NAME
    }


    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        val errorNotification = Notification("TLint plugin", "TLint plugin", "aaaaa", NotificationType.INFORMATION)
        Notifications.Bus.notify(errorNotification)
        return ExternalAnnotatorInspectionVisitor.checkFileWithExternalAnnotator<ExternalLintAnnotationInput, ExternalLintAnnotationResult<LintResult>>(file, manager, isOnTheFly, TLintExternalAnnotator())
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return ExternalAnnotatorInspectionVisitor(holder, TLintExternalAnnotator(), isOnTheFly)
    }

    override fun isSuppressedFor(element: PsiElement): Boolean {
        return false
    }

    companion object {
        const val INSPECTION_SHORT_NAME = "TLintInspection"
    }
}