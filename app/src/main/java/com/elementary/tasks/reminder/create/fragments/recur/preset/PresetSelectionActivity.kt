package com.elementary.tasks.reminder.create.fragments.recur.preset

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.github.naz013.ui.common.activity.BindingActivity
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import com.github.naz013.ui.common.context.buildIntent
import com.github.naz013.common.intent.ActivityLauncherCreator
import com.github.naz013.common.intent.FragmentLauncherCreator
import com.github.naz013.common.intent.IntentPicker
import com.github.naz013.common.intent.LauncherCreator
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.ui.common.view.visibleGone
import com.elementary.tasks.databinding.ActivityRecurPresetListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@Deprecated("Use new builder screen")
class PresetSelectionActivity : BindingActivity<ActivityRecurPresetListBinding>() {

  private val viewModel by viewModel<PresetViewModel>()
  private val presetAdapter = PresetAdapter(
    onItemClickListener = { onPresetSelected(it) },
    onItemDeleteListener = { viewModel.deletePreset(it.id) }
  )

  override fun inflateBinding() = ActivityRecurPresetListBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initActionBar()

    binding.presetListView.layoutManager = LinearLayoutManager(this)
    binding.presetListView.adapter = presetAdapter

    lifecycle.addObserver(viewModel)
    viewModel.presets.nonNullObserve(this) {
      presetAdapter.submitList(it)
      updateListView(it.isEmpty())
    }
  }

  private fun updateListView(isEmpty: Boolean) {
    binding.emptyView.visibleGone(isEmpty)
    binding.presetListView.visibleGone(!isEmpty)
  }

  private fun onPresetSelected(item: UiPresetList) {
    setResult(
      RESULT_OK,
      Intent().apply {
        putExtra(IntentKeys.INTENT_ID, item.id)
      }
    )
    finish()
  }

  private fun initActionBar() {
    binding.toolbar.setNavigationOnClickListener {
      setResult(RESULT_CANCELED)
      finish()
    }
    binding.toolbar.title = getString(R.string.recur_presets)
  }
}

@Deprecated("Use new builder screen")
class PresetPicker private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>,
  private val resultCallback: (String) -> Unit
) : IntentPicker<Intent, ActivityResult>(
  ActivityResultContracts.StartActivityForResult(),
  launcherCreator
) {

  constructor(
    activity: ComponentActivity,
    resultCallback: (String) -> Unit
  ) : this(ActivityLauncherCreator(activity), resultCallback)

  constructor(
    fragment: Fragment,
    resultCallback: (String) -> Unit
  ) : this(FragmentLauncherCreator(fragment), resultCallback)

  fun pickPreset() {
    launch(getIntent())
  }

  override fun dispatchResult(result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      val appPackage = result.data?.getStringExtra(IntentKeys.INTENT_ID) ?: ""
      resultCallback.invoke(appPackage)
    }
  }

  private fun getIntent(): Intent {
    return getActivity().buildIntent(PresetSelectionActivity::class.java)
  }
}
