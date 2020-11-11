package com.elementary.tasks.core.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.AttachmentViewBinding
import com.elementary.tasks.core.utils.*
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

@KoinApiExtension
class AttachmentView : LinearLayout, KoinComponent {

  private val cacheUtil: CacheUtil by inject()

  private lateinit var binding: AttachmentViewBinding
  var onFileUpdateListener: ((path: String) -> Unit)? = null
  var onFileSelectListener: (() -> Unit)? = null
  var content: String = ""
    set(value) {
      field = value
      if (value != "") {
        binding.text.text = value
        binding.removeButton.show()
        onFileUpdateListener?.invoke(value)
      } else {
        noFile()
      }
    }

  constructor(context: Context) : super(context) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    init(context)
  }

  fun setUri(uri: Uri) {
    Timber.d("setUri: ${uri.path}")
    content = uri.toString()
    if (Permissions.checkPermission(context, Permissions.READ_EXTERNAL)) {
      UriUtil.obtainPath(cacheUtil, uri) { success, path ->
        Timber.d("setUri: $success, $path")
        content = if (success && path != null) {
          path
        } else {
          ""
        }
      }
    }
  }

  private fun noFile() {
    binding.removeButton.hide()
    binding.text.text = context.getString(R.string.not_selected)
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_attachment, this)
    orientation = VERTICAL
    binding = AttachmentViewBinding(this)

    binding.removeButton.setOnClickListener {
      content = ""
    }
    binding.text.setOnClickListener {
      addClick()
    }
    binding.hintIcon.setOnLongClickListener {
      Toast.makeText(context, context.getString(R.string.attachment), Toast.LENGTH_SHORT).show()
      return@setOnLongClickListener true
    }
    TooltipCompat.setTooltipText(binding.hintIcon, context.getString(R.string.attachment))
    content = ""
  }

  private fun addClick() {
    if (content == "") {
      onFileSelectListener?.invoke()
    }
  }
}
