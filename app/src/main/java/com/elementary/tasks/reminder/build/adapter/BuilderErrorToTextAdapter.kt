package com.elementary.tasks.reminder.build.adapter

import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.logic.builderstate.BuilderError
import com.github.naz013.common.TextProvider

class BuilderErrorToTextAdapter(
  private val biTypeForUiAdapter: BiTypeForUiAdapter,
  private val textProvider: TextProvider
) {

  operator fun invoke(error: BuilderError): String {
    return when (error) {
      is BuilderError.RequiresBiType -> {
        collectionToText(error.value)
      }

      is BuilderError.InvalidState -> {
        textProvider.getText(R.string.builder_error_create_reminder)
      }

      is BuilderError.Unknown -> {
        textProvider.getText(R.string.builder_error_create_reminder)
      }
    }
  }

  private fun collectionToText(collection: BuilderError.BiTypeCollection): String {
    return when (collection) {
      is BuilderError.BiTypeCollection.Single -> {
        val typeName = biTypeForUiAdapter.getUiString(collection.value)
        textProvider.getString(R.string.please_add_x_to_create_reminder, typeName)
      }

      is BuilderError.BiTypeCollection.Multiple -> {
        textProvider.getString(
          R.string.please_add_the_following_items_x,
          multipleToText(collection)
        )
      }
    }
  }

  private fun multipleToText(multiple: BuilderError.BiTypeCollection.Multiple): String {
    return when (multiple) {
      is BuilderError.BiTypeCollection.Multiple.And -> {
        multiple.biTypes.joinToString("\nand ") { biTypeForUiAdapter.getUiString(it) }
      }
      is BuilderError.BiTypeCollection.Multiple.Or -> {
        multiple.biTypes.joinToString("\nor ") { biTypeForUiAdapter.getUiString(it) }
      }
    }
  }
}
