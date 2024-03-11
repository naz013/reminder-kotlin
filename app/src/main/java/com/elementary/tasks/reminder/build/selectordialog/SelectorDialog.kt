package com.elementary.tasks.reminder.build.selectordialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.onTabSelected
import com.elementary.tasks.core.utils.ui.onTextChanged
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.core.utils.ui.visibleInvisible
import com.elementary.tasks.databinding.BottomSheetBuilderSelectorBinding
import com.elementary.tasks.reminder.build.selectordialog.params.SelectorAdapter
import com.elementary.tasks.reminder.create.fragments.recur.preset.PresetAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectorDialog : BottomSheetDialogFragment() {

  private val viewModel by viewModel<SelectorDialogViewModel>()

  private val selectorAdapter = SelectorAdapter { _, item ->
    callback?.onBuilderItemAdd(item.builderItem)
    dismiss()
  }
  private val presetAdapter = PresetAdapter(
    canDelete = false,
    onItemClickListener = {
      callback?.onPresetSelected(it)
      dismiss()
    },
    onItemDeleteListener = { }
  )
  private val recurPresetAdapter = PresetAdapter(
    canDelete = false,
    onItemClickListener = {
      callback?.onPresetSelected(it)
      dismiss()
    },
    onItemDeleteListener = { }
  )

  private lateinit var binding: BottomSheetBuilderSelectorBinding
  private var callback: SelectorDialogCallback? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    runCatching {
      callback = context as? SelectorDialogCallback
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = BottomSheetBuilderSelectorBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.dialogCloseButton.setOnClickListener { dismiss() }

    binding.searchInput.onTextChanged {
      binding.searchClearButton.visibleInvisible(!it.isNullOrEmpty())
      viewModel.onQueryChanged(it ?: "")
    }

    binding.searchClearButton.setOnClickListener {
      binding.searchInput.setText("")
      viewModel.onQueryChanged("")
    }

    binding.tabLayout.onTabSelected { viewModel.onTabSelected(it.position) }
    binding.tabLayout.gone()

    binding.itemsListView.layoutManager = LinearLayoutManager(requireContext())
    binding.itemsListView.adapter = selectorAdapter

    viewModel.tabs.nonNullObserve(viewLifecycleOwner) { tabs ->
      binding.tabLayout.removeAllTabs()
      tabs.forEachIndexed { index, selectorTab ->
        val tab = binding.tabLayout.newTab().apply {
          this.text = getString(selectorTab.titleRes)
        }
        binding.tabLayout.addTab(tab, index == 0)
      }
      binding.tabLayout.visibleGone(tabs.size > 1)
    }
    viewModel.builderItems.nonNullObserve(viewLifecycleOwner) { selectorAdapter.submitList(it) }
    viewModel.showTab.nonNullObserve(viewLifecycleOwner) { onTabSelected(it) }
    viewModel.presetItems.nonNullObserve(viewLifecycleOwner) { presetAdapter.submitList(it) }
    viewModel.recurPresetItems.nonNullObserve(viewLifecycleOwner) {
      recurPresetAdapter.submitList(it)
    }
    viewModel.loadTabs()
  }

  private fun onTabSelected(tab: SelectorTab) {
    binding.searchInput.setHint(tab.searchHintRes)
    binding.searchInput.setText("")
    when (tab) {
      SelectorTab.BUILDER -> {
        binding.itemsListView.adapter = selectorAdapter
      }

      SelectorTab.PRESETS -> {
        binding.itemsListView.adapter = presetAdapter
      }

      SelectorTab.RECUR_PRESETS -> {
        binding.itemsListView.adapter = recurPresetAdapter
      }
    }
  }

  companion object {
    const val TAG = "SelectorDialog"
  }
}
