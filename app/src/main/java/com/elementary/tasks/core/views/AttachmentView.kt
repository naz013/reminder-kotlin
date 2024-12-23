package com.elementary.tasks.core.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresPermission
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.io.CacheUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.ViewAttachmentBinding
import com.github.naz013.common.Permissions
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.visible
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AttachmentView : LinearLayout, KoinComponent {

  private val cacheUtil by inject<CacheUtil>()

  private lateinit var binding: ViewAttachmentBinding
  var onFileUpdateListener: ((path: String) -> Unit)? = null
  var onFileSelectListener: (() -> Unit)? = null
  var content: String = ""
    set(value) {
      field = value
      if (value != "") {
        binding.text.text = value
        binding.removeButton.visible()
        binding.selectButton.gone()
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

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init(context)
  }

  fun setUri(uri: Uri) {
    Logger.d("setUri: ${uri.path}")
    content = uri.toString()
    if (Permissions.checkPermission(context, Permissions.READ_EXTERNAL)) {
      obtainPath(cacheUtil, uri) { success, path ->
        Logger.d("setUri: $success, $path")
        content = if (success && path != null) {
          path
        } else {
          ""
        }
      }
    }
  }

  private fun noFile() {
    binding.removeButton.gone()
    binding.selectButton.visible()
    binding.text.text = context.getString(R.string.not_selected)
  }

  private fun init(context: Context) {
    View.inflate(context, R.layout.view_attachment, this)
    orientation = VERTICAL
    binding = ViewAttachmentBinding.bind(this)

    binding.removeButton.setOnClickListener {
      content = ""
    }
    binding.selectButton.setOnClickListener { addClick() }
    content = ""
  }

  private fun addClick() {
    if (content == "") {
      onFileSelectListener?.invoke()
    }
  }

  @RequiresPermission(Permissions.READ_EXTERNAL)
  private fun obtainPath(cacheUtil: CacheUtil, uri: Uri, onReady: (Boolean, String?) -> Unit) {
    launchDefault {
      try {
        val path = cacheUtil.cacheFile(uri)
        if (path == null) {
          withUIContext { onReady.invoke(false, null) }
        } else {
          withUIContext { onReady.invoke(true, path) }
        }
      } catch (e: Throwable) {
        withUIContext { onReady.invoke(false, null) }
      }
    }
  }
}
