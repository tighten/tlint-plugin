package tlint

import com.intellij.codeInsight.daemon.impl.SeverityRegistrar
import com.intellij.lang.annotation.Annotation
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange

object InspectionUtil {

    private fun getColorsScheme(customScheme: EditorColorsScheme?): EditorColorsScheme {
        return customScheme ?: EditorColorsManager.getInstance().globalScheme
    }

    fun getTextAttributes(editorColorsScheme: EditorColorsScheme?, severityRegistrar: SeverityRegistrar, severity: HighlightSeverity): TextAttributes {
        val textAttributes = severityRegistrar.getTextAttributesBySeverity(severity)
        return if (textAttributes == null) {
            val colorsScheme = getColorsScheme(editorColorsScheme)
            val infoType = severityRegistrar.getHighlightInfoTypeBySeverity(severity)
            val key = infoType.attributesKey
            colorsScheme.getAttributes(key)
        } else {
            textAttributes
        }
    }

    fun createAnnotation(holder: AnnotationHolder, severity: HighlightSeverity, forcedTextAttributes: TextAttributes?, range: TextRange, message: String): Annotation? {
        val annotation: Annotation
        if (forcedTextAttributes == null) {
            annotation = if (severity == HighlightSeverity.ERROR) holder.createErrorAnnotation(range, message) else holder.createWarningAnnotation(range, message)
        } else {
            annotation = holder.createAnnotation(severity, range, message)
            annotation.enforcedTextAttributes = forcedTextAttributes
        }
        annotation.setNeedsUpdateOnTyping(false)
        return annotation
    }
}
