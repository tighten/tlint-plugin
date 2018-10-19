package tlint;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.SeverityRegistrar;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InspectionUtil {
    private InspectionUtil() {
    }

    @NotNull
    private static EditorColorsScheme getColorsScheme(@Nullable EditorColorsScheme customScheme) {
        return customScheme == null ? EditorColorsManager.getInstance().getGlobalScheme() : customScheme;
    }

    @NotNull
    public static TextAttributes getTextAttributes(@Nullable EditorColorsScheme editorColorsScheme, @NotNull SeverityRegistrar severityRegistrar, @NotNull HighlightSeverity severity) {
        TextAttributes textAttributes = severityRegistrar.getTextAttributesBySeverity(severity);
        if (textAttributes == null) {
            EditorColorsScheme colorsScheme = getColorsScheme(editorColorsScheme);
            HighlightInfoType.HighlightInfoTypeImpl infoType = severityRegistrar.getHighlightInfoTypeBySeverity(severity);
            TextAttributesKey key = infoType.getAttributesKey();
            return colorsScheme.getAttributes(key);
        } else {
            return textAttributes;
        }
    }

    @Nullable
    public static Annotation createAnnotation(@NotNull AnnotationHolder holder, @NotNull HighlightSeverity severity, @Nullable TextAttributes forcedTextAttributes, @NotNull TextRange range, @NotNull String message) {
        Annotation annotation;
        if (forcedTextAttributes == null) {
            annotation = severity.equals(HighlightSeverity.ERROR) ? holder.createErrorAnnotation(range, message) : holder.createWarningAnnotation(range, message);
        } else {
            annotation = holder.createAnnotation(severity, range, message);
            annotation.setEnforcedTextAttributes(forcedTextAttributes);
        }
        annotation.setNeedsUpdateOnTyping(false);
        return annotation;
    }
}
