package tlint.inspection

import tlint.TLintBundle
import tlint.TLintProjectComponent
import tlint.cli.TLint
import tlint.cli.TLintRunner
import tlint.cli.LintResult
import com.intellij.lang.annotation.Annotation
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.profile.codeInspection.InspectionProjectProfileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import tlint.ActualFile2
import tlint.ThreadLocalTempActualFile
import tlint.ExternalLintAnnotationInput
import tlint.ExternalLintAnnotationResult
import tlint.InspectionUtil
import tlint.Delayer
import tlint.FileUtils

import java.io.File
import java.util.concurrent.TimeUnit

class TLintExternalAnnotator : ExternalAnnotator<ExternalLintAnnotationInput, ExternalLintAnnotationResult<LintResult>>() {

    private val delayer = Delayer(TimeUnit.SECONDS.toMillis(5L))

    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): ExternalLintAnnotationInput? {
        return collectInformation(file, editor)
    }

    override fun apply(file: PsiFile, annotationResult: ExternalLintAnnotationResult<LintResult>?, holder: AnnotationHolder) {
        if (annotationResult == null) {
            return
        }
        val inspectionProjectProfileManager = InspectionProjectProfileManager.getInstance(file.project)
        val severityRegistrar = inspectionProjectProfileManager.severityRegistrar
        val colorsScheme = annotationResult.input.colorsScheme

        val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return

        for (warn in annotationResult.result.tLint.file.errors) {
            val severity = HighlightSeverity.WARNING

            val forcedTextAttributes = InspectionUtil.getTextAttributes(colorsScheme, severityRegistrar, severity)

            createAnnotation(holder, document, warn, severity, forcedTextAttributes)
        }
    }

    override fun doAnnotate(collectedInfo: ExternalLintAnnotationInput?): ExternalLintAnnotationResult<LintResult>? {
        var actualCodeFile: ActualFile2? = null

        try {
            val file = collectedInfo!!.psiFile

            actualCodeFile = ActualFile2.getOrCreateActualFile(T_LINT_TEMP_FILE, file, collectedInfo.fileContent)

            if (actualCodeFile == null) {
                return null
            }

            val relativeFile: String?
            relativeFile = FileUtils.makeRelative(File(file.project.basePath!!), actualCodeFile.actualFile)

            LOG.info("project.basePath")
            LOG.info(file.project.basePath)
            LOG.info("actualCodeFile.actualFile.absolutePath")
            LOG.info(actualCodeFile.actualFile.absolutePath)
            LOG.info("relativeFile")
            LOG.info(relativeFile)

            val result = TLintRunner.lint(
                    TLintRunner.buildSettings(
                            file.project.basePath!!,
                            relativeFile!!,
                            getTlintExecutablePath(file.project.basePath!!)
                    )
            )

            actualCodeFile.deleteTemp()

            return ExternalLintAnnotationResult(collectedInfo, result)
        } catch (e: Exception) {
            LOG.error("Error running TLint inspection: ", e)

            showNotificationError("Error running TLint inspection: " + e.message)
        } finally {
            actualCodeFile?.deleteTemp()
        }

        return null
    }

    private fun showNotificationError(content: String) {
        if (delayer.should()) {
            TLintProjectComponent.showNotification(content, NotificationType.ERROR)
            delayer.done()
        }
    }

    companion object {
        private val LOG = Logger.getInstance(TLintBundle.LOG_ID)
        private const val MESSAGE_PREFIX = "TLint: "
        private val T_LINT_TEMP_FILE = Key.create<ThreadLocalTempActualFile>("T_LINT_TEMP_FILE")

        @Throws(Exception::class)
        private fun getTlintExecutablePath(cwd: String): String {
            val lintExecutable: File
            val osExtension = if (this.isWindows()) ".bat" else ""
            val localTlintExecutable = File("$cwd/vendor/bin/tlint$osExtension")
            val globalTlintExecutable = File(System.getProperty("user.home") + "/.composer/vendor/bin/tlint"+osExtension)
            val windowsGlobalTlintExecutable = File(System.getProperty("user.home") + "/AppData/Roaming/Composer/vendor/bin/tlint"+osExtension)

            lintExecutable = when {
                globalTlintExecutable.exists() -> globalTlintExecutable
                windowsGlobalTlintExecutable.exists() -> windowsGlobalTlintExecutable
                localTlintExecutable.exists() -> localTlintExecutable
                else -> {
                    throw Exception("No tlint executable found.")
                }
            }

            return lintExecutable.absolutePath
        }

        private fun isWindows(): Boolean {
            return  System.getProperty("os.name").startsWith("Windows")
        }

        private fun createAnnotation(
                holder: AnnotationHolder,
                document: Document,
                warn: TLint.Issue,
                severity: HighlightSeverity,
                forcedTextAttributes: TextAttributes?
        ): Annotation? {
            val line = warn.line - 1

            if (line < 0 || line >= document.lineCount) {
                return null
            }

            val lineEndOffset = document.getLineEndOffset(line)
            val lineStartOffset = document.getLineStartOffset(line)

            val range: TextRange

            range = TextRange(lineStartOffset, lineEndOffset)

            return InspectionUtil.createAnnotation(holder, severity, forcedTextAttributes, range, MESSAGE_PREFIX + warn.message!!.trim { it <= ' ' } + " (" + warn.source + ')'.toString())
        }

        private fun collectInformation(psiFile: PsiFile, editor: Editor): ExternalLintAnnotationInput? {
            val project = psiFile.project

            val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)

            val fileContent = document!!.text

            val colorsScheme = editor.colorsScheme

            return ExternalLintAnnotationInput(psiFile, fileContent, colorsScheme)
        }
    }
}
