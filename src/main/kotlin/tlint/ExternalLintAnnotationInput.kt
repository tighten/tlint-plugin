package tlint

import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.psi.PsiFile

class ExternalLintAnnotationInput(val psiFile: PsiFile, val fileContent: String, val colorsScheme: EditorColorsScheme)