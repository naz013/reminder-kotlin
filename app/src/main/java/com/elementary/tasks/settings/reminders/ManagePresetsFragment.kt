package com.elementary.tasks.settings.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.android.visibleGone
import com.elementary.tasks.databinding.FragmentManagePresetsBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.elementary.tasks.reminder.build.preset.ManagePresetsViewModel
import com.elementary.tasks.reminder.create.fragments.recur.preset.PresetAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class ManagePresetsFragment : BaseSettingsFragment<FragmentManagePresetsBinding>() {

  private val viewModel by viewModel<ManagePresetsViewModel>()
  private val presetAdapter = PresetAdapter(
    onItemClickListener = { },
    onItemDeleteListener = { viewModel.deletePreset(it.id) }
  )

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentManagePresetsBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initList()
    initViewModel()
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.presets.nonNullObserve(this) {
      presetAdapter.submitList(it)
      updateListView(it.isEmpty())
    }
  }

  override fun getTitle(): String = getString(R.string.recur_presets)

  private fun initList() {
    binding.presetListView.layoutManager = LinearLayoutManager(requireContext())
    binding.presetListView.adapter = presetAdapter
  }

  private fun updateListView(isEmpty: Boolean) {
    binding.emptyView.visibleGone(isEmpty)
    binding.presetListView.visibleGone(!isEmpty)
  }

  companion object {

    fun newInstance(): ManagePresetsFragment {
      return ManagePresetsFragment()
    }
  }
}
