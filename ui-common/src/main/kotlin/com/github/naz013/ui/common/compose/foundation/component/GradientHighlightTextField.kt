package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppTheme

/**
 * A styled sub-range of a [GradientHighlightTextField]'s text - e.g. the portion of a live
 * speech-to-text transcript that was just recognized. [range] end is exclusive, clamped to the
 * current text length when applied.
 */
data class TextHighlight(
  val range: IntRange,
  val brush: Brush? = null,
  val bold: Boolean = false,
  val strikeThrough: Boolean = false,
)

/**
 * A single/multi-line text field that can paint [highlights] (gradient color, bold, strike-
 * through) over sub-ranges of its text without touching the underlying editable value. This is
 * the Compose replacement for `UiGradientEditText`, which achieved the same live speech-
 * transcript highlighting via Android `Spannable`/`Shader` spans on an `EditText`; here it's a
 * [VisualTransformation] building an [androidx.compose.ui.text.AnnotatedString] with
 * `SpanStyle(brush = ...)`.
 *
 * @param value Current field text.
 * @param onValueChange Invoked with the new plain text as the user types.
 * @param highlights Styled sub-ranges to paint over [value], e.g. from [TextInputController]-
 * style live speech recognition.
 */
@Composable
fun GradientHighlightTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  highlights: List<TextHighlight> = emptyList(),
  enabled: Boolean = true,
  textStyle: TextStyle = LocalTextStyle.current,
) {
  val contentColor = LocalContentColor.current
  val cursorColor = MaterialTheme.colorScheme.primary
  val resolvedColor = if (textStyle.color.isSpecified) textStyle.color else contentColor
  val mergedStyle = textStyle.copy(color = resolvedColor)
  val visualTransformation = remember(highlights) {
    VisualTransformation { text ->
      val annotated = buildAnnotatedString {
        append(text.text)
        highlights.forEach { highlight ->
          val start = highlight.range.first.coerceIn(0, text.length)
          val end = (highlight.range.last + 1).coerceIn(start, text.length)
          if (end > start) {
            addStyle(
              SpanStyle(
                brush = highlight.brush,
                fontWeight = if (highlight.bold) FontWeight.Bold else null,
                textDecoration = if (highlight.strikeThrough) TextDecoration.LineThrough else null,
              ),
              start,
              end,
            )
          }
        }
      }
      TransformedText(annotated, OffsetMapping.Identity)
    }
  }

  BasicTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    enabled = enabled,
    textStyle = mergedStyle,
    visualTransformation = visualTransformation,
    cursorBrush = SolidColor(cursorColor),
  )
}

@Preview(showBackground = true, name = "Gradient highlight text field")
@Composable
private fun PreviewGradientHighlightTextField() {
  AppTheme {
    val text = "Buy milk and eggs"
    GradientHighlightTextField(
      value = text,
      onValueChange = {},
      highlights = listOf(
        TextHighlight(
          range = 8..17,
          brush = Brush.linearGradient(
            listOf(
              MaterialTheme.colorScheme.primary,
              MaterialTheme.colorScheme.tertiary,
            ),
          ),
        ),
      ),
      modifier = Modifier.padding(8.dp),
    )
  }
}
