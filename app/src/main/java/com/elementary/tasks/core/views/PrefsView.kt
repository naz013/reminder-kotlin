package com.elementary.tasks.core.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.views.PrefsViewBinding
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.transparent
import com.elementary.tasks.core.utils.ui.visible
import timber.log.Timber

class PrefsView : RelativeLayout {

  private lateinit var binding: PrefsViewBinding

  var isChecked: Boolean = false
    set(checked) {
      field = checked
      if (viewType == CHECK) {
        binding.prefsCheck.isChecked = checked
      } else if (viewType == SWITCH) {
        binding.prefsSwitch.isChecked = checked
      }
      refreshDetailText()
      for (listener in mOnCheckedListeners) {
        listener.onCheckedChange(checked)
      }
    }
  private var isForPro: Boolean = false
  private var isTest: Boolean = false
  private var mDependentValue: Boolean? = null
  private var viewType = CHECK
  private var mOnText: String? = null
  private var mOffText: String? = null
  private var mSecondaryText: String? = null
  private val mDependencyViews = ArrayList<PrefsView>()
  private val mReverseDependencyViews = ArrayList<PrefsView>()
  private val mOnCheckedListeners = ArrayList<OnCheckedListener>()

  private val isCheckable: Boolean
    get() = viewType == CHECK || viewType == SWITCH

  constructor(context: Context) : super(context) {
    init(context, null)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init(context, attrs)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
    context,
    attrs,
    defStyle
  ) {
    init(context, attrs)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    View.inflate(context, R.layout.view_prefs, this)
    binding = PrefsViewBinding(this)
    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

    binding.progressView.gone()

    if (attrs != null) {
      val a = context.theme.obtainStyledAttributes(
        /* set = */ attrs,
        /* attrs = */ R.styleable.PrefsView,
        /* defStyleAttr = */ 0,
        /* defStyleRes = */ 0
      )
      var titleText = ""
      var valueText = ""
      var divTop = false
      var divBottom = false
      var res = 0
      var iconId = 0
      try {
        titleText = a.getString(R.styleable.PrefsView_prefs_primary_text) ?: ""
        mSecondaryText = a.getString(R.styleable.PrefsView_prefs_secondary_text)
        mOnText = a.getString(R.styleable.PrefsView_prefs_secondary_text_on)
        mOffText = a.getString(R.styleable.PrefsView_prefs_secondary_text_off)
        valueText = a.getString(R.styleable.PrefsView_prefs_value_text) ?: ""
        divTop = a.getBoolean(R.styleable.PrefsView_prefs_divider_top, false)
        divBottom = a.getBoolean(R.styleable.PrefsView_prefs_divider_bottom, false)
        isForPro = a.getBoolean(R.styleable.PrefsView_prefs_pro, false)
        isTest = a.getBoolean(R.styleable.PrefsView_prefs_isTest, false)
        viewType = a.getInt(R.styleable.PrefsView_prefs_type, CHECK)
        res = a.getInt(R.styleable.PrefsView_prefs_view_resource, 0)
        iconId = a.getResourceId(R.styleable.PrefsView_prefs_icon, 0)
        val primaryColor = a.getColor(R.styleable.PrefsView_prefs_primary_text_color, -1)
        if (primaryColor != -1) {
          binding.prefsPrimaryText.setTextColor(primaryColor)
        }
      } catch (e: Exception) {
        Timber.d("init: ${e.message}")
      } finally {
        a.recycle()
      }
      if (iconId != 0 && SHOW_ICON) {
        binding.iconView.visible()
        binding.iconView.setImageResource(iconId)
        binding.iconView.imageTintList = binding.prefsPrimaryText.textColors
      } else {
        binding.iconView.transparent()
      }
      setTitleText(titleText)
      setDividerTop(divTop)
      setDividerBottom(divBottom)
      setView()
      setValueText(valueText)
      setViewResource(res)
    }
    setDetailText(mSecondaryText)
    isChecked = isChecked
    setVisible()
  }

  fun setLoading(isLoading: Boolean) {
    if (isLoading) {
      binding.progressView.visible()
    } else {
      binding.progressView.gone()
    }
  }

  fun setCustomViewClickListener(onClickListener: OnClickListener) {
    binding.prefsView.setOnClickListener(onClickListener)
  }

  fun setOnCheckedListener(listener: OnCheckedListener) {
    this.mOnCheckedListeners.add(listener)
  }

  fun setForPro(forPro: Boolean) {
    isForPro = forPro
    setVisible()
  }

  fun setDependentView(view: PrefsView) {
    mDependencyViews.add(view)
    view.setOnCheckedListener(object : OnCheckedListener {
      override fun onCheckedChange(checked: Boolean) {
        checkDependency()
      }
    })
    checkDependency()
  }

  fun setDependentValue(b: Boolean) {
    mDependentValue = b
    checkDependency()
    checkReverseDependency()
    checkDependentValue()
  }

  private fun checkDependency() {
    var enable = true
    for (prefsView in mDependencyViews) {
      if (!prefsView.isChecked) {
        enable = false
        break
      }
    }
    isEnabled = enable
    checkDependentValue()
  }

  private fun checkDependentValue() {
    val dep = mDependentValue ?: return
    if (isEnabled) {
      isEnabled = dep
    }
  }

  fun setReverseDependentView(view: PrefsView) {
    mReverseDependencyViews.add(view)
    view.setOnCheckedListener(object : OnCheckedListener {
      override fun onCheckedChange(checked: Boolean) {
        checkReverseDependency()
      }
    })
    checkReverseDependency()
  }

  private fun checkReverseDependency() {
    var enable = true
    for (prefsView in mReverseDependencyViews) {
      if (prefsView.isChecked) {
        enable = false
        break
      }
    }
    isEnabled = enable
    checkDependentValue()
  }

  private fun setVisible() {
    visibility = if (isTest) {
      if (BuildConfig.DEBUG) {
        View.VISIBLE
      } else {
        View.GONE
      }
    } else if (isForPro) {
      if (Module.isPro) {
        View.VISIBLE
      } else {
        View.GONE
      }
    } else {
      View.VISIBLE
    }
  }

  private fun setView() {
    hideAll()
    when (viewType) {
      CHECK -> binding.prefsCheck.visible()
      SWITCH -> binding.prefsSwitch.visible()
      TEXT -> binding.prefsValue.visible()
      VIEW -> binding.prefsView.visible()
    }
  }

  private fun hideAll() {
    binding.prefsCheck.gone()
    binding.prefsSwitch.gone()
    binding.prefsValue.gone()
    binding.prefsView.gone()
  }

  private fun setTitleText(text: String) {
    binding.prefsPrimaryText.text = text
  }

  fun setDetailText(text: String?) {
    if (isCheckable && hasOnOff()) {
      if (isChecked) {
        binding.prefsSecondaryText.text = mOnText
      } else {
        binding.prefsSecondaryText.text = mOffText
      }
      binding.prefsSecondaryText.visible()
    } else {
      if (TextUtils.isEmpty(text)) {
        binding.prefsSecondaryText.gone()
      } else {
        binding.prefsSecondaryText.text = text
        binding.prefsSecondaryText.visible()
      }
    }
  }

  fun setValue(value: Int) {
    binding.prefsValue.text = value.toString()
  }

  fun setValueText(text: String) {
    binding.prefsValue.text = text
  }

  fun setViewColor(@ColorInt color: Int) {
    if (color != 0) {
      binding.prefsView.setBackgroundColor(color)
    }
  }

  fun setViewResource(@DrawableRes resource: Int) {
    if (resource != 0) {
      binding.prefsView.setImageResource(resource)
    }
  }

  fun setViewTintColor(@ColorInt color: Int) {
    if (color != 0) {
      binding.prefsView.setColorFilter(color)
    }
  }

  fun setViewDrawable(drawable: Drawable?) {
    binding.prefsView.setImageDrawable(drawable)
  }

  fun setViewBitmap(bitmap: Bitmap?) {
    binding.prefsView.setImageBitmap(bitmap)
  }

  private fun refreshDetailText() {
    if (isCheckable && hasOnOff()) {
      setDetailText(mSecondaryText)
    }
  }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    binding.prefsSwitch.isEnabled = enabled
    binding.prefsCheck.isEnabled = enabled
    binding.prefsView.isEnabled = enabled
    binding.prefsValue.isEnabled = enabled
    binding.prefsSecondaryText.isEnabled = enabled
    binding.prefsPrimaryText.isEnabled = enabled
    binding.iconView.isEnabled = enabled
  }

  private fun setDividerTop(divider: Boolean) {
    if (divider) {
      binding.dividerTop.visible()
    } else {
      binding.dividerTop.gone()
    }
  }

  private fun setDividerBottom(divider: Boolean) {
    if (divider) {
      binding.dividerBottom.visible()
    } else {
      binding.dividerBottom.gone()
    }
  }

  private fun hasOnOff(): Boolean {
    return mOffText != null && mOnText != null
  }

  interface OnCheckedListener {
    fun onCheckedChange(checked: Boolean)
  }

  companion object {

    private const val CHECK = 0
    private const val SWITCH = 1
    private const val VIEW = 2
    private const val TEXT = 3
    private const val SHOW_ICON: Boolean = true
  }
}
