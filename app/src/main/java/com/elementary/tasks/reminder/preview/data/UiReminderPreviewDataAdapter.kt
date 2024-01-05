package com.elementary.tasks.reminder.preview.data

import android.media.RingtoneManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.adapter.UiAdapter
import com.elementary.tasks.core.data.adapter.UiReminderCommonAdapter
import com.elementary.tasks.core.data.adapter.UiReminderPlaceAdapter
import com.elementary.tasks.core.data.adapter.group.UiGroupListAdapter
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.data.ui.UiIcon
import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.data.ui.reminder.UiAppTarget
import com.elementary.tasks.core.data.ui.reminder.UiCallTarget
import com.elementary.tasks.core.data.ui.reminder.UiEmailTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderPlace
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.data.ui.reminder.UiSmsTarget
import com.elementary.tasks.core.os.ColorProvider
import com.elementary.tasks.core.os.UnitsConverter
import com.elementary.tasks.core.text.UiTextDecoration
import com.elementary.tasks.core.text.UiTextFormat
import com.elementary.tasks.core.text.UiTextStyle
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Sound
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.reminder.Icons
import com.elementary.tasks.reminder.preview.AttachmentToUiReminderPreviewAttachment
import java.io.File
import java.util.Locale

class UiReminderPreviewDataAdapter(
  private val prefs: Prefs,
  private val uiReminderPlaceAdapter: UiReminderPlaceAdapter,
  private val uiReminderCommonAdapter: UiReminderCommonAdapter,
  private val uiGroupListAdapter: UiGroupListAdapter,
  private val textProvider: TextProvider,
  private val colorProvider: ColorProvider,
  private val unitsConverter: UnitsConverter,
  private val attachmentToUiReminderPreviewAttachment: AttachmentToUiReminderPreviewAttachment
) : UiAdapter<Reminder, List<UiReminderPreviewData>> {

  override fun create(data: Reminder): List<UiReminderPreviewData> {
    val type = UiReminderType(data.type)
    return addStatus(data) +
      addDetails(data) +
      addTargetInfo(data) +
      addAttachments(data) +
      addSubTasks(data, type) +
      addMap(data, type) +
      addAds()
  }

  private fun addAttachments(data: Reminder): List<UiReminderPreviewData> {
    return data.attachmentFiles.takeIf { it.isNotEmpty() }?.let {
      attachmentToUiReminderPreviewAttachment(it)
    } ?: emptyList()
  }

  private fun addTargetInfo(
    data: Reminder
  ): List<UiReminderPreviewData> {
    val type = UiReminderType(data.type)
    if (!type.isLink() && !type.isCall() && !type.isApp() && !type.isEmail() && !type.isSms()) {
      return emptyList()
    }

    val target = uiReminderCommonAdapter.getTarget(data, type)

    return when {
      type.isCall() -> {
        val name = target?.let { it as? UiCallTarget }?.name
        listOfNotNull(
          UiReminderPreviewHeader(
            UiTextElement(
              text = textProvider.getText(R.string.make_call),
              textFormat = UiTextFormat(
                fontSize = unitsConverter.spToPx(18f),
                textStyle = UiTextStyle.BOLD,
                textColor = colorProvider.getColorOnBackground()
              )
            )
          ),
          name?.let {
            UiReminderPreviewElement(
              textElement = UiTextElement(
                text = it,
                textFormat = UiTextFormat(
                  fontSize = unitsConverter.spToPx(16f),
                  textStyle = UiTextStyle.BOLD,
                  textColor = colorProvider.getColorOnBackground()
                )
              ),
              icon = UiIcon(Icons.PERSON, colorProvider.getColorOnBackground())
            )
          },
          UiReminderPreviewElement(
            textElement = UiTextElement(
              text = data.target,
              textFormat = UiTextFormat(
                fontSize = unitsConverter.spToPx(16f),
                textStyle = UiTextStyle.BOLD,
                textColor = colorProvider.getColorOnBackground()
              )
            ),
            icon = UiIcon(Icons.MAKE_CALL, colorProvider.getColorOnBackground())
          )
        )
      }

      type.isSms() -> {
        val name = target?.let { it as? UiSmsTarget }?.name
        listOfNotNull(
          UiReminderPreviewHeader(
            UiTextElement(
              text = textProvider.getText(R.string.send_sms),
              textFormat = UiTextFormat(
                fontSize = unitsConverter.spToPx(18f),
                textStyle = UiTextStyle.BOLD,
                textColor = colorProvider.getColorOnBackground()
              )
            )
          ),
          name?.let {
            UiReminderPreviewElement(
              textElement = UiTextElement(
                text = it,
                textFormat = UiTextFormat(
                  fontSize = unitsConverter.spToPx(16f),
                  textStyle = UiTextStyle.BOLD,
                  textColor = colorProvider.getColorOnBackground()
                )
              ),
              icon = UiIcon(Icons.PERSON, colorProvider.getColorOnBackground())
            )
          },
          UiReminderPreviewElement(
            textElement = UiTextElement(
              text = data.target,
              textFormat = UiTextFormat(
                fontSize = unitsConverter.spToPx(16f),
                textStyle = UiTextStyle.BOLD,
                textColor = colorProvider.getColorOnBackground()
              )
            ),
            icon = UiIcon(Icons.SEND_SMS, colorProvider.getColorOnBackground())
          )
        )
      }

      type.isApp() -> {
        val name = target?.let { it as? UiAppTarget }?.name
        listOfNotNull(
          UiReminderPreviewHeader(
            UiTextElement(
              text = textProvider.getText(R.string.open_app),
              textFormat = UiTextFormat(
                fontSize = unitsConverter.spToPx(18f),
                textStyle = UiTextStyle.BOLD,
                textColor = colorProvider.getColorOnBackground()
              )
            )
          ),
          UiReminderPreviewElement(
            textElement = UiTextElement(
              text = name ?: data.target,
              textFormat = UiTextFormat(
                fontSize = unitsConverter.spToPx(16f),
                textStyle = UiTextStyle.BOLD,
                textColor = colorProvider.getColorOnBackground()
              )
            ),
            icon = UiIcon(Icons.OPEN_APP, colorProvider.getColorOnBackground())
          )
        )
      }

      type.isLink() -> {
        listOfNotNull(
          UiReminderPreviewHeader(
            UiTextElement(
              text = textProvider.getText(R.string.open_link),
              textFormat = UiTextFormat(
                fontSize = unitsConverter.spToPx(18f),
                textStyle = UiTextStyle.BOLD,
                textColor = colorProvider.getColorOnBackground()
              )
            )
          ),
          UiReminderPreviewElement(
            textElement = UiTextElement(
              text = data.target,
              textFormat = UiTextFormat(
                fontSize = unitsConverter.spToPx(16f),
                textStyle = UiTextStyle.BOLD,
                textDecoration = UiTextDecoration.UNDERLINE,
                textColor = colorProvider.getColorOnBackground()
              )
            ),
            icon = UiIcon(Icons.OPEN_LINK, colorProvider.getColorOnBackground())
          )
        )
      }

      type.isLink() -> {
        val name = target?.let { it as? UiEmailTarget }?.name
        listOfNotNull(
          UiReminderPreviewHeader(
            UiTextElement(
              text = textProvider.getText(R.string.e_mail),
              textFormat = UiTextFormat(
                fontSize = unitsConverter.spToPx(18f),
                textStyle = UiTextStyle.BOLD,
                textColor = colorProvider.getColorOnBackground()
              )
            )
          ),
          name?.let {
            UiReminderPreviewElement(
              textElement = UiTextElement(
                text = it,
                textFormat = UiTextFormat(
                  fontSize = unitsConverter.spToPx(16f),
                  textStyle = UiTextStyle.BOLD,
                  textColor = colorProvider.getColorOnBackground()
                )
              ),
              icon = UiIcon(Icons.PERSON, colorProvider.getColorOnBackground())
            )
          },
          UiReminderPreviewElement(
            textElement = UiTextElement(
              text = data.target,
              textFormat = UiTextFormat(
                fontSize = unitsConverter.spToPx(16f),
                textStyle = UiTextStyle.BOLD,
                textDecoration = UiTextDecoration.UNDERLINE,
                textColor = colorProvider.getColorOnBackground()
              )
            ),
            icon = UiIcon(Icons.SEND_EMAIL, colorProvider.getColorOnBackground())
          ),
          UiReminderPreviewElement(
            textElement = UiTextElement(
              text = data.subject,
              textFormat = UiTextFormat(
                fontSize = unitsConverter.spToPx(16f),
                textStyle = UiTextStyle.BOLD,
                textDecoration = UiTextDecoration.UNDERLINE,
                textColor = colorProvider.getColorOnBackground()
              )
            ),
            icon = UiIcon(Icons.EMAIL_SUBJECT, colorProvider.getColorOnBackground())
          )
        )
      }

      else -> emptyList()
    }
  }

  private fun addDetails(
    data: Reminder
  ): List<UiReminderPreviewData> {
    return listOfNotNull(
      UiReminderPreviewHeader(
        UiTextElement(
          text = textProvider.getText(R.string.details),
          textFormat = UiTextFormat(
            fontSize = unitsConverter.spToPx(18f),
            textStyle = UiTextStyle.BOLD,
            textColor = colorProvider.getColorOnBackground()
          )
        )
      ),
      data.summary.takeIf { it.isNotEmpty() }?.let {
        UiReminderPreviewElement(
          textElement = UiTextElement(
            text = it,
            textFormat = UiTextFormat(
              fontSize = unitsConverter.spToPx(16f),
              textStyle = UiTextStyle.BOLD
            )
          ),
          icon = UiIcon(Icons.SUMMARY, colorProvider.getColorOnBackground())
        )
      },
      data.description?.takeIf { it.isNotEmpty() }?.let {
        UiReminderPreviewElement(
          textElement = UiTextElement(
            text = it,
            textFormat = UiTextFormat(
              fontSize = unitsConverter.spToPx(14f),
              textStyle = UiTextStyle.NORMAL
            )
          ),
          icon = UiIcon(Icons.DESCRIPTION, colorProvider.getColorOnBackground())
        )
      }
    ) + addDueInfo(data) + addExtraInfo(data)
  }

  private fun addExtraInfo(data: Reminder): List<UiReminderPreviewData> {
    return listOfNotNull(
      getNormalTextElement(
        text = uiGroupListAdapter.convert(data.groupUuId, data.groupColor, data.groupTitle).title,
        icon = Icons.GROUP
      ),
      getNormalTextElement(
        uiReminderCommonAdapter.getWindowType(data.windowType),
        Icons.WINDOW_TYPE
      ),
      getMelodyName(data.melodyPath)?.let { getNormalTextElement(it, Icons.MELODY) },
      getNormalTextElement(uiReminderCommonAdapter.getPriorityTitle(data.priority), Icons.PRIORITY),
      getNormalTextElement(data.uuId, Icons.ID)
    )
  }

  private fun addDueInfo(
    data: Reminder
  ): List<UiReminderPreviewData> {
    val type = UiReminderType(data.type)
    val dueData = uiReminderCommonAdapter.getDue(data, type)
    return listOfNotNull(
      dueData.formattedDateTime?.let {
        UiReminderPreviewElement(
          textElement = UiTextElement(
            text = it,
            textFormat = UiTextFormat(
              fontSize = unitsConverter.spToPx(14f),
              textStyle = UiTextStyle.NORMAL
            )
          ),
          icon = UiIcon(Icons.DUE, colorProvider.getColorOnBackground())
        )
      },
      UiReminderPreviewElement(
        textElement = UiTextElement(
          text = dueData.repeat,
          textFormat = UiTextFormat(
            fontSize = unitsConverter.spToPx(14f),
            textStyle = UiTextStyle.NORMAL
          )
        ),
        icon = UiIcon(Icons.REPEAT, colorProvider.getColorOnBackground())
      ),
      UiReminderPreviewElement(
        textElement = UiTextElement(
          text = dueData.repeat,
          textFormat = UiTextFormat(
            fontSize = unitsConverter.spToPx(14f),
            textStyle = UiTextStyle.NORMAL
          )
        ),
        icon = UiIcon(Icons.DUE, colorProvider.getColorOnBackground())
      )
    )
  }

  private fun addStatus(data: Reminder): List<UiReminderPreviewData> {
    return listOfNotNull(
      createStatus(data)
    )
  }

  private fun createStatus(
    data: Reminder
  ): UiReminderPreviewStatus? {
    val status = uiReminderCommonAdapter.getReminderStatus(data.isActive, data.isRemoved)
    val text = if (status.active) {
      textProvider.getText(R.string.enabled4)
    } else {
      textProvider.getText(R.string.disabled)
    }
    return UiReminderPreviewStatus(
      id = data.uuId,
      status = status,
      statusText = UiTextElement(
        text = text,
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(20f),
          textStyle = UiTextStyle.BOLD,
          textColor = colorProvider.getColorOnBackground()
        )
      )
    ).takeIf { status.canToggle }
  }

  private fun addAds(): List<UiReminderPreviewData> {
    return listOfNotNull(
      UiReminderPreviewAds.takeIf { !Module.isPro }
    )
  }

  private fun addMap(data: Reminder, type: UiReminderType): List<UiReminderPreviewData> {
    if (!type.isGpsType()) return emptyList()
    val places = data.places.map { uiReminderPlaceAdapter.create(it) }
    if (places.isEmpty()) return emptyList()

    val headerTitle = if (places.size == 1) {
      if (type.isBase(UiReminderType.Base.LOCATION_IN)) {
        textProvider.getText(R.string.builder_arriving_destination)
      } else {
        textProvider.getText(R.string.builder_leaving_place)
      }
    } else {
      textProvider.getText(R.string.places)
    }
    val placeText = places.joinToString("\n") { it.toTitle() }
    return listOf(
      UiReminderPreviewHeader(
        UiTextElement(
          text = headerTitle,
          textFormat = UiTextFormat(
            fontSize = unitsConverter.spToPx(18f),
            textStyle = UiTextStyle.BOLD,
            textColor = colorProvider.getColorOnBackground()
          )
        )
      ),
      UiReminderPreviewMap(
        placesText = UiTextElement(
          text = placeText,
          textFormat = UiTextFormat(
            fontSize = unitsConverter.spToPx(14f),
            textStyle = UiTextStyle.NORMAL,
            textColor = colorProvider.getColorOnBackground()
          )
        ),
        places = places
      )
    )
  }

  private fun UiReminderPlace.toTitle(): CharSequence {
    return address.takeIf { it.isNotEmpty() } ?: String.format(
      Locale.getDefault(),
      "%.5f,%.5f",
      latitude,
      longitude
    )
  }

  private fun addSubTasks(data: Reminder, type: UiReminderType): List<UiReminderPreviewData> {
    if (!type.isSubTasks()) return emptyList()
    val subTasks = data.shoppings.filterNot { it.isDeleted }
      .sortedByDescending { !it.isChecked }
      .map { getSubTaskElement(it) }
    if (subTasks.isEmpty()) return emptyList()
    return listOf(
      UiReminderPreviewHeader(
        UiTextElement(
          text = textProvider.getText(R.string.builder_sub_tasks),
          textFormat = UiTextFormat(
            fontSize = unitsConverter.spToPx(18f),
            textStyle = UiTextStyle.BOLD,
            textColor = colorProvider.getColorOnBackground()
          )
        )
      )
    ) + subTasks
  }

  private fun getSubTaskElement(
    shopItem: ShopItem
  ): UiReminderPreviewSubTask {
    val textDecoration = when {
      shopItem.isChecked -> UiTextDecoration.STRIKE_THROUGH
      else -> UiTextDecoration.NONE
    }
    return UiReminderPreviewSubTask(
      id = shopItem.uuId,
      isChecked = shopItem.isChecked,
      textElement = UiTextElement(
        text = shopItem.summary,
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(14f),
          textColor = colorProvider.getColorOnBackground(),
          textStyle = UiTextStyle.NORMAL,
          textDecoration = textDecoration
        )
      )
    )
  }

  private fun getNormalTextElement(
    text: String,
    icon: Int?
  ): UiReminderPreviewElement {
    return UiReminderPreviewElement(
      icon = icon?.let { UiIcon(it, colorProvider.getColorOnBackground()) },
      textElement = UiTextElement(
        text = text,
        textFormat = UiTextFormat(
          fontSize = unitsConverter.spToPx(14f),
          textColor = colorProvider.getColorOnBackground(),
          textStyle = UiTextStyle.NORMAL,
          textDecoration = UiTextDecoration.NONE
        )
      )
    )
  }

  private fun getMelodyName(melodyPath: String): String? {
    var file: File? = null
    if (melodyPath.isNotEmpty()) {
      file = File(melodyPath)
    } else {
      val path = prefs.melodyFile
      if (path != "" && !Sound.isDefaultMelody(path)) {
        file = File(path)
      } else {
        val soundPath = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)?.path
        if (soundPath != null) {
          file = File(soundPath)
        }
      }
    }
    return file?.name
  }
}
