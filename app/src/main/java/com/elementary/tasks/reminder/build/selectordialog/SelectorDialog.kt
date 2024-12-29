package com.elementary.tasks.reminder.build.selectordialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.core.utils.ui.onTabSelected
import com.elementary.tasks.core.utils.ui.onTextChanged
import com.elementary.tasks.databinding.BottomSheetBuilderSelectorBinding
import com.elementary.tasks.reminder.build.selectordialog.params.SelectorAdapter
import com.elementary.tasks.reminder.create.fragments.recur.preset.PresetAdapter
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.view.gone
import com.github.naz013.ui.common.view.visibleGone
import com.github.naz013.ui.common.view.visibleInvisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectorDialog : BottomSheetDialogFragment() {

  private val viewModel by viewModel<SelectorDialogViewModel>()

  private val selectorAdapter = SelectorAdapter { _, item ->
    dismiss()
    callback?.onBuilderItemAdd(item.builderItem)
    Logger.i(TAG, "Selected builder item: $item")
  }
  private val presetAdapter = PresetAdapter(
    canDelete = false,
    onItemClickListener = {
      dismiss()
      callback?.onPresetSelected(it)
      Logger.i(TAG, "Selected general preset: $it")
    },
    onItemDeleteListener = { }
  )
  private val recurPresetAdapter = PresetAdapter(
    canDelete = false,
    onItemClickListener = {
      dismiss()
      callback?.onPresetSelected(it)
      Logger.i(TAG, "Selected recur preset: $it")
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

    dialog?.setCancelable(true)
    dialog?.setCanceledOnTouchOutside(true)

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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.d(TAG, "Selector dialog created")
  }

  override fun onDestroy() {
    super.onDestroy()
    Logger.d(TAG, "Selector dialog destroyed")
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    Logger.d(TAG, "Selector dialog dismissed")
  }

  override fun onCancel(dialog: DialogInterface) {
    super.onCancel(dialog)
    Logger.d(TAG, "Selector dialog cancelled")
  }

  companion object {
    const val TAG = "SelectorDialog"
  }
}
