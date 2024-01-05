package com.elementary.tasks.reminder.create.fragments.recur

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.os.startActivity
import com.elementary.tasks.core.utils.datetime.recurrence.Day
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.core.utils.datetime.recurrence.FreqType
import com.elementary.tasks.core.utils.datetime.recurrence.RecurParamType
import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.isVisible
import com.elementary.tasks.core.utils.ui.onTextChanged
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.ClosableLegacyBuilderWarningView
import com.elementary.tasks.core.views.DateTimeView
import com.elementary.tasks.core.views.common.ValueSliderView
import com.elementary.tasks.databinding.DialogRecurDayAdvancedBinding
import com.elementary.tasks.databinding.DialogRecurDayBinding
import com.elementary.tasks.databinding.DialogRecurIntListBinding
import com.elementary.tasks.databinding.DialogRecurSingleIntBinding
import com.elementary.tasks.databinding.FragmentReminderRecurBinding
import com.elementary.tasks.reminder.create.fragments.RepeatableTypeFragment
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamBuilderAdapter
import com.elementary.tasks.reminder.create.fragments.recur.intdialog.IntListAdapter
import com.elementary.tasks.reminder.create.fragments.recur.preset.PresetPicker
import com.elementary.tasks.reminder.create.fragments.recur.preview.PreviewDataAdapter
import com.google.android.material.tabs.TabLayout
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDateTime
import timber.log.Timber

class RecurFragment : RepeatableTypeFragment<FragmentReminderRecurBinding>() {

  private val viewModel by viewModel<RecurBuilderViewModel>()
  private val analyticsEventSender by inject<AnalyticsEventSender>()

  private lateinit var presetPicker: PresetPicker

  private val previewAdapter = PreviewDataAdapter()
  private val builderAdapter = ParamBuilderAdapter(
    onItemClickListener = object : ParamBuilderAdapter.OnItemClickListener {
      override fun onItemClicked(position: Int, param: UiBuilderParam<*>) {
        showParamEditorDialog(param)
      }
    },
    onItemRemoveListener = object : ParamBuilderAdapter.OnItemRemoveListener {
      override fun onItemRemoved(position: Int, param: UiBuilderParam<*>) {
        viewModel.unSelectParam(param.param)
      }
    }
  )

  override fun getExplanationVisibilityType(): ReminderExplanationVisibility.Type {
    return ReminderExplanationVisibility.Type.BY_RECUR
  }

  override fun getExplanationView(): View {
    return binding.explanationView
  }

  override fun setCloseListenerToExplanationView(listener: View.OnClickListener) {
    binding.explanationView.setOnClickListener(listener)
  }

  override fun prepare(): Reminder? {
    val reminder = iFace.state.reminder
    var type = Reminder.BY_RECUR
    val isAction = binding.actionView.hasAction()
    if (TextUtils.isEmpty(reminder.summary) && !isAction) {
      binding.taskLayout.error = getString(R.string.task_summary_is_empty)
      binding.taskLayout.isErrorEnabled = true
      return null
    }
    var number = ""
    if (isAction) {
      number = binding.actionView.number
      if (TextUtils.isEmpty(number)) {
        iFace.showSnackbar(getString(R.string.you_dont_insert_number))
        binding.tabs.selectTab(binding.tabs.getTabAt(2), true)
        return null
      }
      type = if (binding.actionView.actionState == ActionView.ActionState.CALL) {
        Reminder.BY_RECUR_CALL
      } else {
        Reminder.BY_RECUR_SMS
      }
    }

    reminder.weekdays = listOf()
    reminder.target = number
    reminder.type = type
    reminder.after = 0L
    reminder.delay = 0
    reminder.eventCount = 0
    reminder.repeatInterval = 0

    val eventData = viewModel.getEventData()
    if (eventData == null) {
      iFace.showSnackbar(
        getString(R.string.recur_wrong_parameters_message)
      )
      binding.tabs.selectTab(binding.tabs.getTabAt(0), true)
      return null
    }

    val startTime = eventData.startDateTime
    Timber.d("EVENT_TIME ${dateTimeManager.logDateTime(startTime)}")

    reminder.recurDataObject = eventData.recurObject
    reminder.eventTime = dateTimeManager.getGmtFromDateTime(startTime)
    reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)
    Timber.d("EVENT_TIME %s", dateTimeManager.logDateTime(startTime))

    if (binding.savePresetCheck.isChecked) {
      viewModel.addPreset(
        recurObject = eventData.recurObject,
        name = binding.presetNameInput.trimmedText()
      )
    }

    analyticsEventSender.send(FeatureUsedEvent(Feature.RECUR_EVENT_CREATED))

    return reminder
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderRecurBinding.inflate(inflater, container, false)

  override fun getDynamicViews(): List<View> {
    return listOfNotNull(
      binding.ledView,
      binding.exportToCalendar,
      binding.exportToTasks,
      binding.tuneExtraView,
      binding.melodyView,
      binding.attachmentView,
      binding.groupView,
      binding.taskSummary,
      binding.beforeView,
      binding.loudnessView,
      binding.priorityView,
      binding.windowTypeView,
      binding.actionView,
      binding.dateView
    )
  }

  override fun getLegacyMessageView(): ClosableLegacyBuilderWarningView {
    return binding.legacyBuilderWarningView
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presetPicker = PresetPicker(this) {
      viewModel.onPresetSelected(it)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.tuneExtraView.hasAutoExtra = false

    binding.dateView.addOnDateChangeListener(
      object : DateTimeView.OnDateChangeListener {
        override fun onChanged(dateTime: LocalDateTime) {
          viewModel.onDateTimeChanged(dateTime)
        }
      }
    )
    viewModel.onDateTimeChanged(binding.dateView.selectedDateTime)

    binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: TabLayout.Tab?) {
        hideTabViews()
        when (tab?.position) {
          0 -> {
            binding.buildContentView.visible()
          }

          1 -> {
            binding.previewContentView.visible()
          }

          2 -> {
            binding.customizeContentView.visible()
          }
        }
      }

      override fun onTabReselected(tab: TabLayout.Tab?) {}
      override fun onTabUnselected(tab: TabLayout.Tab?) {}

      private fun hideTabViews() {
        if (binding.buildContentView.isVisible()) {
          binding.buildContentView.gone()
        }
        if (binding.previewContentView.isVisible()) {
          binding.previewContentView.gone()
        }
        if (binding.customizeContentView.isVisible()) {
          binding.customizeContentView.gone()
        }
      }
    })

    binding.ruleList.layoutManager = LinearLayoutManager(context)
    binding.ruleList.adapter = builderAdapter

    binding.addParamButton.setOnClickListener { showParamSelectorDialog() }
    binding.presetsButton.setOnClickListener { presetPicker.pickPreset() }
    binding.helpButton.setOnClickListener {
      startActivity(RecurHelpActivity::class.java)
    }
    binding.savePresetCheck.setOnCheckedChangeListener { buttonView, isChecked ->
      binding.presetNameLayout.visibleGone(isChecked)
    }

    binding.previewList.layoutManager = LinearLayoutManager(context)
    binding.previewList.adapter = previewAdapter
    binding.previewErrorTooltip.setOnClickListener {
      binding.previewErrorTooltip.gone()
    }

    viewModel.usedParams.observe(viewLifecycleOwner) {
      builderAdapter.submitList(it)
    }
    viewModel.previewData.observe(viewLifecycleOwner) {
      previewAdapter.submitList(it.items)
      if (it.scrollTo >= 0) {
        binding.previewList.smoothScrollToPosition(it.scrollTo)
      }
    }
    viewModel.dateTime.nonNullObserve(viewLifecycleOwner) {
      binding.dateView.selectedDateTime = it
    }
    viewModel.availableParams.observe(viewLifecycleOwner) { }
    viewModel.supportedFreq.observe(viewLifecycleOwner) { }
    viewModel.supportedDays.observe(viewLifecycleOwner) { }
    viewModel.previewError.observe(viewLifecycleOwner) { error ->
      binding.previewErrorTooltip.visibleGone(error != null)
      error?.also { binding.previewErrorTooltip.setText(it) }
    }

    editReminder()
  }

  private fun showParamSelectorDialog() {
    val availableParams = viewModel.availableParams.value ?: emptyList()

    val items = availableParams.map { it.text }.toTypedArray()

    dialogues.getNullableDialog(context)
      ?.setTitle(getString(R.string.recur_add_parameter))
      ?.setItems(items) { dialog, which ->
        dialog.dismiss()
        showParamEditorDialog(availableParams[which])
      }
      ?.create()
      ?.show()
  }

  @Suppress("UNCHECKED_CAST")
  private fun showParamEditorDialog(param: UiBuilderParam<*>) {
    when (param.param.recurParamType) {
      RecurParamType.COUNT -> showSingleIntPickerDialog(
        builderParam = param as UiBuilderParam<Int>,
        title = getString(R.string.recur_count),
        minValue = 0,
        maxValue = 500
      )

      RecurParamType.INTERVAL -> showSingleIntPickerDialog(
        builderParam = param as UiBuilderParam<Int>,
        title = getString(R.string.recur_interval),
        minValue = 0,
        maxValue = 366
      )

      RecurParamType.UNTIL -> showDatePickerDialog(param as UiBuilderParam<UtcDateTime>)
      RecurParamType.FREQ -> showFreqPickerDialog(param as UiBuilderParam<FreqType>)
      RecurParamType.BYDAY -> showDaysPickerDialog(param as UiBuilderParam<List<DayValue>>)
      RecurParamType.WEEKSTART -> showDayPickerDialog(param as UiBuilderParam<DayValue>)
      RecurParamType.BYMONTH -> showIntListPickerDialog(
        builderParam = param as UiBuilderParam<List<Int>>,
        title = getString(R.string.recur_month_s),
        minValue = 1,
        maxValue = 12
      )

      RecurParamType.BYMINUTE -> showIntListPickerDialog(
        builderParam = param as UiBuilderParam<List<Int>>,
        title = getString(R.string.recur_minute_s),
        minValue = 0,
        maxValue = 59
      )

      RecurParamType.BYHOUR -> showIntListPickerDialog(
        builderParam = param as UiBuilderParam<List<Int>>,
        title = getString(R.string.recur_hour_s),
        minValue = 0,
        maxValue = 23
      )

      RecurParamType.BYMONTHDAY -> showIntListPickerDialog(
        builderParam = param as UiBuilderParam<List<Int>>,
        title = getString(R.string.recur_day_s_of_month),
        minValue = -31,
        maxValue = 31,
        excludedValues = intArrayOf(0)
      )

      RecurParamType.BYWEEKNO -> showIntListPickerDialog(
        builderParam = param as UiBuilderParam<List<Int>>,
        title = getString(R.string.recur_week_number_s),
        minValue = -53,
        maxValue = 53,
        excludedValues = intArrayOf(0)
      )

      RecurParamType.BYYEARDAY -> showIntListPickerDialog(
        builderParam = param as UiBuilderParam<List<Int>>,
        title = getString(R.string.recur_day_s_of_year),
        minValue = -366,
        maxValue = 366,
        excludedValues = intArrayOf(0)
      )

      RecurParamType.BYSETPOS -> showIntListPickerDialog(
        builderParam = param as UiBuilderParam<List<Int>>,
        title = getString(R.string.recur_set_pos),
        minValue = -366,
        maxValue = 366,
        excludedValues = intArrayOf(0)
      )
    }
  }

  private fun showIntListPickerDialog(
    builderParam: UiBuilderParam<List<Int>>,
    title: String,
    minValue: Int,
    maxValue: Int,
    vararg excludedValues: Int
  ) {
    val numbers = viewModel.generateNumbers(
      minValue = minValue,
      maxValue = maxValue,
      excludedValues = excludedValues,
      selectedValues = builderParam.param.value
    )

    val adapter = IntListAdapter(numbers)
    val view = DialogRecurIntListBinding.inflate(layoutInflater)
    view.intList.layoutManager = GridLayoutManager(requireContext(), 6)
    view.intList.adapter = adapter

    view.clearSelectionButton.setOnClickListener { adapter.clearSelection() }
    view.selectAllButton.setOnClickListener { adapter.selectAll() }

    dialogues.getMaterialDialog(requireContext())
      .setView(view.root)
      .setTitle(title)
      .setPositiveButton(getString(R.string.recur_save)) { dialog, _ ->
        dialog.dismiss()
        val ints = adapter.getSelected().map { it.value }
        viewModel.selectOrUpdateParam(builderParam.param.copy(value = ints))
      }
      .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      .create()
      .show()
  }

  private fun showDaysPickerDialog(builderParam: UiBuilderParam<List<DayValue>>) {
    if (viewModel.showAdvancedDayDialog() || builderParam.param.value.any { it.hasPrefix }) {
      showAdvancedDaysPickerDialog(builderParam)
    } else {
      showSimplifiedDaysPickerDialog(builderParam)
    }
  }

  private fun showSimplifiedDaysPickerDialog(builderParam: UiBuilderParam<List<DayValue>>) {
    val view = DialogRecurDayBinding.inflate(layoutInflater)

    builderParam.param.value.map { it.value }.forEach {
      when {
        it.contains(Day.MO.value) -> {
          view.mondayCheck.isChecked = true
        }

        it.contains(Day.TU.value) -> {
          view.tuesdayCheck.isChecked = true
        }

        it.contains(Day.WE.value) -> {
          view.wednesdayCheck.isChecked = true
        }

        it.contains(Day.TH.value) -> {
          view.thursdayCheck.isChecked = true
        }

        it.contains(Day.FR.value) -> {
          view.fridayCheck.isChecked = true
        }

        it.contains(Day.SA.value) -> {
          view.saturdayCheck.isChecked = true
        }

        it.contains(Day.SU.value) -> {
          view.sundayCheck.isChecked = true
        }
      }
    }

    dialogues.getMaterialDialog(requireContext())
      .setView(view.root)
      .setTitle(getString(R.string.recur_day_s))
      .setPositiveButton(getString(R.string.recur_save)) { dialog, _ ->
        dialog.dismiss()
        viewModel.selectOrUpdateParam(
          builderParam.param.copy(
            value = dayValues(view)
          )
        )
      }
      .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      .setNeutralButton(getString(R.string.recur_advanced)) { dialog, _ ->
        dialog.dismiss()
        viewModel.setShowAdvancedDayDialog(true)
        val copyParam = builderParam.copy(
          param = builderParam.param.copy(
            value = dayValues(view)
          )
        )
        showAdvancedDaysPickerDialog(copyParam)
      }
      .create()
      .show()
  }

  private fun dayValues(view: DialogRecurDayBinding): MutableList<DayValue> {
    val days = mutableListOf<DayValue>()

    if (view.mondayCheck.isChecked) {
      days.add(DayValue(Day.MO))
    }
    if (view.tuesdayCheck.isChecked) {
      days.add(DayValue(Day.TU))
    }
    if (view.wednesdayCheck.isChecked) {
      days.add(DayValue(Day.WE))
    }
    if (view.thursdayCheck.isChecked) {
      days.add(DayValue(Day.TH))
    }
    if (view.fridayCheck.isChecked) {
      days.add(DayValue(Day.FR))
    }
    if (view.saturdayCheck.isChecked) {
      days.add(DayValue(Day.SA))
    }
    if (view.sundayCheck.isChecked) {
      days.add(DayValue(Day.SU))
    }
    return days
  }

  private fun showAdvancedDaysPickerDialog(builderParam: UiBuilderParam<List<DayValue>>) {
    val view = DialogRecurDayAdvancedBinding.inflate(layoutInflater)

    val initValue = builderParam.param.value.joinToString(",") { it.buildString() }
    view.daysInput.setText(initValue)

    val validator = ByDayValidator()

    val dialog = dialogues.getMaterialDialog(requireContext())
      .setView(view.root)
      .setTitle(getString(R.string.recur_day_s))
      .setPositiveButton(getString(R.string.recur_save)) { dialog, _ ->
        dialog.dismiss()
        viewModel.selectOrUpdateParam(
          builderParam.param.copy(
            value = validator.getValues()
          )
        )
      }
      .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      .setNeutralButton(getString(R.string.recur_simplified)) { dialog, _ ->
        dialog.dismiss()
        viewModel.setShowAdvancedDayDialog(false)
        val copyParam = builderParam.copy(
          param = builderParam.param.copy(
            value = validator.getValues()
          )
        )
        showSimplifiedDaysPickerDialog(copyParam)
      }
      .create()
    dialog.show()

    val buttonPositive: Button? = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
    val buttonNeutral: Button? = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)

    validator.setListener {
      if (!it) {
        view.daysLayout.error = getString(R.string.recur_by_day_wrong_format)
      } else {
        view.daysLayout.error = null
      }
      view.daysLayout.isErrorEnabled = !it

      buttonPositive?.isEnabled = it
      buttonNeutral?.isEnabled = it
    }
    validator.onTextChanged(initValue)

    view.daysInput.onTextChanged { validator.onTextChanged(it?.uppercase() ?: "") }
  }

  private fun showDayPickerDialog(builderParam: UiBuilderParam<DayValue>) {
    val availableParams = viewModel.supportedDays.value ?: emptyList()

    val items = availableParams.map { it.text }.toTypedArray()

    var index = availableParams.indexOfFirst { it.dayValue == builderParam.param.value }
    if (index == -1) {
      index = 0
    }

    dialogues.getMaterialDialog(requireContext())
      .setTitle(getString(R.string.recur_week_start))
      .setSingleChoiceItems(items, index) { _, which ->
        index = which
      }
      .setPositiveButton(getString(R.string.recur_save)) { dialog, _ ->
        dialog.dismiss()
        val newParam = builderParam.param.copy(
          value = availableParams[index].dayValue
        )
        viewModel.selectOrUpdateParam(newParam)
      }
      .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      .create()
      .show()
  }

  private fun showFreqPickerDialog(builderParam: UiBuilderParam<FreqType>) {
    val availableParams = viewModel.supportedFreq.value ?: emptyList()

    val items = availableParams.map { it.text }.toTypedArray()

    var index = availableParams.indexOfFirst { it.freqType == builderParam.param.value }
    if (index == -1) {
      index = 0
    }

    dialogues.getMaterialDialog(requireContext())
      .setTitle(getString(R.string.recur_frequency))
      .setSingleChoiceItems(items, index) { _, which ->
        index = which
      }
      .setPositiveButton(getString(R.string.recur_save)) { dialog, _ ->
        dialog.dismiss()
        val newParam = builderParam.param.copy(
          value = availableParams[index].freqType
        )
        viewModel.selectOrUpdateParam(newParam)
      }
      .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      .create()
      .show()
  }

  private fun showDatePickerDialog(builderParam: UiBuilderParam<UtcDateTime>) {
    builderParam.param.value.dateTime?.also { localDateTime ->
      dateTimePickerProvider.showDatePicker(requireContext(), localDateTime.toLocalDate()) {
        val newDateTime = LocalDateTime.of(it, localDateTime.toLocalTime())
        showTimePickerDialog(
          builderParam.copy(
            param = builderParam.param.copy(
              value = UtcDateTime(newDateTime)
            )
          )
        )
      }
    }
  }

  private fun showTimePickerDialog(builderParam: UiBuilderParam<UtcDateTime>) {
    builderParam.param.value.dateTime?.also { localDateTime ->
      dateTimePickerProvider.showTimePicker(requireContext(), localDateTime.toLocalTime()) {
        val newDateTime = LocalDateTime.of(localDateTime.toLocalDate(), it)
        viewModel.selectOrUpdateParam(
          builderParam.param.copy(
            value = UtcDateTime(newDateTime)
          )
        )
      }
    }
  }

  private fun showSingleIntPickerDialog(
    builderParam: UiBuilderParam<Int>,
    title: String,
    minValue: Int,
    maxValue: Int
  ) {
    val view = DialogRecurSingleIntBinding.inflate(layoutInflater)
    view.intValuePicker.valueFormatter = object : ValueSliderView.ValueFormatter {
      override fun apply(value: Float): String {
        return value.toInt().toString()
      }
    }
    view.intValuePicker.setRange(minValue.toFloat(), maxValue.toFloat(), 1f)
    view.intValuePicker.value = builderParam.param.value.toFloat()

    dialogues.getMaterialDialog(requireContext())
      .setView(view.root)
      .setTitle(title)
      .setPositiveButton(getString(R.string.recur_save)) { dialog, _ ->
        dialog.dismiss()
        val newParam = builderParam.param.copy(
          value = view.intValuePicker.value.toInt()
        )
        viewModel.selectOrUpdateParam(newParam)
      }
      .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
      }
      .create()
      .show()
  }

  override fun updateActions() {
    if (!isAdded) return
    if (binding.actionView.hasAction()) {
      if (binding.actionView.actionState == ActionView.ActionState.SMS) {
        binding.tuneExtraView.hasAutoExtra = false
      } else {
        binding.tuneExtraView.hasAutoExtra = true
        binding.tuneExtraView.hint = getString(R.string.enable_making_phone_calls_automatically)
      }
    } else {
      binding.tuneExtraView.hasAutoExtra = false
    }
  }

  private fun editReminder() {
    val reminder = iFace.state.reminder
    viewModel.onEdit(reminder)
  }
}
