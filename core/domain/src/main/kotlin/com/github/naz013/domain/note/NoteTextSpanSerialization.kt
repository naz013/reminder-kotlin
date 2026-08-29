package com.github.naz013.domain.note

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

private const val ATTRIBUTE_TYPE_FIELD = "type"

/** Gson can't deserialize a sealed interface polymorphically on its own, since [NoteTextSpan]
 * embeds [NoteSpanAttribute] nested inside the `spans` list persisted as one JSON blob (unlike
 * e.g. `RecurrenceRule`, which is split into separate `type`/`payload` columns instead). This
 * adapter adds a `type` discriminator per attribute and dispatches on it. */
private object NoteSpanAttributeAdapter :
  JsonSerializer<NoteSpanAttribute>,
  JsonDeserializer<NoteSpanAttribute> {

  override fun serialize(
    src: NoteSpanAttribute,
    typeOfSrc: Type,
    context: JsonSerializationContext,
  ): JsonElement {
    val json = when (src) {
      is NoteSpanAttribute.FontFamily,
      is NoteSpanAttribute.FontSize,
      is NoteSpanAttribute.SolidColor,
      is NoteSpanAttribute.GradientColor,
      -> context.serialize(src, src.javaClass).asJsonObject
      else -> JsonObject()
    }
    json.addProperty(ATTRIBUTE_TYPE_FIELD, src.javaClass.simpleName)
    return json
  }

  override fun deserialize(
    json: JsonElement,
    typeOfT: Type,
    context: JsonDeserializationContext,
  ): NoteSpanAttribute {
    val jsonObject = json.asJsonObject
    return when (val type = jsonObject.get(ATTRIBUTE_TYPE_FIELD)?.asString) {
      "Bold" -> NoteSpanAttribute.Bold
      "Italic" -> NoteSpanAttribute.Italic
      "Underline" -> NoteSpanAttribute.Underline
      "Strikethrough" -> NoteSpanAttribute.Strikethrough
      "Heading1" -> NoteSpanAttribute.Heading1
      "Heading2" -> NoteSpanAttribute.Heading2
      "Heading3" -> NoteSpanAttribute.Heading3
      "BulletItem" -> NoteSpanAttribute.BulletItem
      "FontFamily" -> context.deserialize(jsonObject, NoteSpanAttribute.FontFamily::class.java)
      "FontSize" -> context.deserialize(jsonObject, NoteSpanAttribute.FontSize::class.java)
      "SolidColor" -> context.deserialize(jsonObject, NoteSpanAttribute.SolidColor::class.java)
      "GradientColor" -> context.deserialize(jsonObject, NoteSpanAttribute.GradientColor::class.java)
      else -> error("Unknown NoteSpanAttribute type: $type")
    }
  }
}

private val noteSpanGson: Gson by lazy {
  GsonBuilder()
    .registerTypeAdapter(NoteSpanAttribute::class.java, NoteSpanAttributeAdapter)
    .create()
}

private val noteTextSpanListType: Type = object : TypeToken<List<NoteTextSpan>>() {}.type

fun List<NoteTextSpan>.toJson(): String = noteSpanGson.toJson(this, noteTextSpanListType)

fun String.toNoteTextSpans(): List<NoteTextSpan> {
  if (isBlank()) return emptyList()
  return noteSpanGson.fromJson(this, noteTextSpanListType) ?: emptyList()
}
