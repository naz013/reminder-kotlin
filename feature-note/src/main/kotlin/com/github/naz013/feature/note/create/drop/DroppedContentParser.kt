package com.github.naz013.feature.note.create.drop

import android.content.ClipData
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.github.naz013.logging.Logger
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

/**
 * Parses the items contained in a [ClipData] from a drag-and-drop operation and routes each item
 * to the appropriate content handler based on its MIME type.
 *
 * Supported content:
 * - Inline text (e.g. text dragged from a browser selection).
 * - Image URIs (`image*`) — forwarded as-is to the image pipeline.
 * - Text file URIs (`text*`, e.g. `.txt`, `.md`, `.csv`) — content read as UTF-8 string.
 * - PDF file URIs (`applicationpdf`) — text extracted via Apache PDFBox.
 * - Unsupported URIs — counted and reported so the UI can show an appropriate error.
 *
 * @property context application context used for [android.content.ContentResolver] access.
 */
class DroppedContentParser(
  private val context: Context,
) {
  /**
   * Aggregated result of parsing all items in a [ClipData].
   *
   * @property textContent ordered list of text strings extracted from inline text, text files,
   *   and PDF documents.
   * @property imageUris list of URIs that should be decoded as images.
   * @property unsupportedCount number of dropped items that could not be handled.
   */
  data class ParseResult(
    val textContent: List<String>,
    val imageUris: List<Uri>,
    val unsupportedCount: Int,
  )

  /**
   * Classifies and processes all items in [clipData].
   *
   * Each item is inspected for inline text first. If a URI is present it is classified by MIME
   * type and the content is extracted accordingly. Items with no text and no URI are silently
   * skipped.
   *
   * @param clipData the drop payload received from [android.view.DragEvent.ACTION_DROP].
   * @return a [ParseResult] with all extracted content grouped by type.
   */
  fun parse(clipData: ClipData): ParseResult {
    val textContent = mutableListOf<String>()
    val imageUris = mutableListOf<Uri>()
    var unsupportedCount = 0

    for (i in 0 until clipData.itemCount) {
      val item = clipData.getItemAt(i)
      val inlineText = item.text?.toString()
      val uri = item.uri

      when {
        !inlineText.isNullOrEmpty() && uri == null -> {
          // Pure inline text drag (e.g. selected text from another app).
          Logger.d(TAG, "Item $i: inline text (${inlineText.length} chars)")
          textContent.add(inlineText)
        }

        uri != null -> {
          val mimeType = resolveMimeType(uri)
          Logger.d(TAG, "Item $i: URI=$uri mimeType=$mimeType")
          when {
            mimeType.startsWith("image/") -> imageUris.add(uri)

            mimeType.startsWith("text/") -> {
              val text = readTextFromUri(uri)
              if (text != null) {
                textContent.add(text)
              } else {
                unsupportedCount++
              }
            }

            mimeType == MIME_PDF -> {
              val text = extractTextFromPdf(uri)
              if (text != null) {
                textContent.add(text)
              } else {
                unsupportedCount++
              }
            }

            else -> {
              Logger.d(TAG, "Item $i: unsupported MIME=$mimeType")
              unsupportedCount++
            }
          }
        }

        else -> {
          // Item has neither inline text nor a URI — nothing to do.
          Logger.d(TAG, "Item $i: empty clip item, skipping")
        }
      }
    }

    return ParseResult(
      textContent = textContent,
      imageUris = imageUris,
      unsupportedCount = unsupportedCount,
    )
  }

  /**
   * Resolves the MIME type of [uri] by querying the [android.content.ContentResolver] first.
   * Falls back to inferring the type from the file extension when the resolver returns `null` or
   * the generic `application/octet-stream`.
   *
   * @param uri the URI to inspect.
   * @return the resolved MIME type string, or an empty string when detection fails.
   */
  private fun resolveMimeType(uri: Uri): String {
    val resolvedType = context.contentResolver.getType(uri)
    if (!resolvedType.isNullOrEmpty() && resolvedType != MIME_OCTET_STREAM) {
      return resolvedType
    }
    return inferMimeFromExtension(uri) ?: resolvedType ?: ""
  }

  /**
   * Infers a MIME type from the file extension present in [uri]'s path using [MimeTypeMap].
   *
   * @param uri the URI whose path is inspected.
   * @return the inferred MIME type, or `null` when no extension is detected.
   */
  private fun inferMimeFromExtension(uri: Uri): String? {
    val extension =
      MimeTypeMap
        .getFileExtensionFromUrl(uri.toString())
        ?.lowercase()
        ?.takeIf { it.isNotEmpty() }
        ?: return null
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
  }

  /**
   * Opens [uri] via the [android.content.ContentResolver] and reads its content as a UTF-8 string.
   *
   * @param uri the URI of the text file to read.
   * @return the trimmed file content, or `null` if reading fails or the file is empty.
   */
  private fun readTextFromUri(uri: Uri): String? =
    try {
      context.contentResolver.openInputStream(uri)?.use { inputStream ->
        inputStream
          .bufferedReader(Charsets.UTF_8)
          .readText()
          .trim()
          .takeIf { it.isNotEmpty() }
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to read text file from URI: $uri", e)
      null
    }

  /**
   * Extracts all text content from a PDF document at [uri] using Apache PDFBox.
   *
   * @param uri the URI of the PDF file.
   * @return the extracted and trimmed text, or `null` if extraction fails or the PDF is empty.
   */
  private fun extractTextFromPdf(uri: Uri): String? =
    try {
      context.contentResolver.openInputStream(uri)?.use { inputStream ->
        val document = PDDocument.load(inputStream)
        val text = PDFTextStripper().getText(document).trim()
        document.close()
        text.takeIf { it.isNotEmpty() }
      }
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to extract text from PDF URI: $uri", e)
      null
    }

  companion object {
    private const val TAG = "DroppedContentParser"
    private const val MIME_PDF = "application/pdf"
    private const val MIME_OCTET_STREAM = "application/octet-stream"
  }
}
