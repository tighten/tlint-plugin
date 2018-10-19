package tlint;

import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.psi.PsiFile;

public class ExternalLintAnnotationInput {
    public final String fileContent;
    public final EditorColorsScheme colorsScheme;
    public final PsiFile psiFile;

    public ExternalLintAnnotationInput(PsiFile psiFile, String fileContent, EditorColorsScheme colorsScheme) {
        this.psiFile = psiFile;
        this.fileContent = fileContent;
        this.colorsScheme = colorsScheme;
    }
}