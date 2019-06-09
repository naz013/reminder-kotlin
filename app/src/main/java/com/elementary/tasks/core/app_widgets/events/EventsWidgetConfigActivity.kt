package com.elementary.tasks.core.app_widgets.events

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivityWidgetCurrentTasksConfigBinding
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding

class EventsWidgetConfigActivity : BindingActivity<ActivityWidgetCurrentTasksConfigBinding>(R.layout.activity_widget_current_tasks_config) {

    private var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
    private var resultValue: Intent? = null
    private var textSize: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntent()

        binding.fabSave.setOnClickListener { showTextSizeDialog() }
        binding.bgColorSlider.setSelectorColorResource(if (isDarkMode) R.color.pureWhite else R.color.pureBlack)
        binding.bgColorSlider.setListener { position, _ ->
            binding.headerBg.setBackgroundResource(WidgetUtils.newWidgetBg(position))
            updateIcons(position)
        }

        binding.listItemBgColorSlider.setSelectorColorResource(if (isDarkMode) R.color.pureWhite else R.color.pureBlack)
        binding.listItemBgColorSlider.setListener { position, _ ->
            binding.listItemCard.setBackgroundResource(WidgetUtils.newWidgetBg(position))
            updateText(position)
        }

        updateText(0)
        updateIcons(0)

        showCurrentTheme()
    }

    private fun showCurrentTheme() {
        val sp = getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE)

        val headerBg = sp.getInt(WIDGET_HEADER_BG + widgetID, 0)
        binding.bgColorSlider.setSelection(headerBg)
        updateIcons(headerBg)

        val itemBg = sp.getInt(WIDGET_ITEM_BG + widgetID, 0)
        binding.listItemBgColorSlider.setSelection(itemBg)
        updateText(itemBg)
    }

    private fun updateText(code: Int) {
        if (WidgetUtils.isDarkBg(code)) {
            binding.statusIcon.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_twotone_alarm_24px,
                    ContextCompat.getColor(this, R.color.pureWhite)))
            binding.taskText.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            binding.taskDate.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            binding.taskNumber.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            binding.taskTime.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
            binding.leftTime.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
        } else {
            binding.statusIcon.setImageBitmap(ViewUtils.createIcon(this, R.drawable.ic_twotone_alarm_24px,
                    ContextCompat.getColor(this, R.color.pureBlack)))
            binding.taskText.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            binding.taskDate.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            binding.taskNumber.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            binding.taskTime.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
            binding.leftTime.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
        }
    }

    private fun updateIcons(code: Int) {
        if (WidgetUtils.isDarkBg(code)) {
            binding.btnSettings.setImageResource(R.drawable.ic_twotone_settings_white)
            binding.btnAddTask.setImageResource(R.drawable.ic_twotone_add_white)
            binding.btnVoice.setImageResource(R.drawable.ic_twotone_mic_white)
            binding.widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.pureWhite))
        } else {
            binding.btnSettings.setImageResource(R.drawable.ic_twotone_settings_24px)
            binding.btnAddTask.setImageResource(R.drawable.ic_twotone_add_24px)
            binding.btnVoice.setImageResource(R.drawable.ic_twotone_mic_24px)
            binding.widgetTitle.setTextColor(ContextCompat.getColor(this, R.color.pureBlack))
        }
    }

    private fun readIntent() {
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }
        resultValue = Intent()
        resultValue?.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
        setResult(RESULT_CANCELED, resultValue)
    }

    private fun showTextSizeDialog() {
        val builder = dialogues.getMaterialDialog(this)
        builder.setTitle(R.string.text_size)
        val b = DialogWithSeekAndTitleBinding.inflate(layoutInflater, null, false)
        b.seekBar.max = 13
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textSize = progress + 12
                b.titleView.text = textSize.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        b.seekBar.progress = 2
        textSize = 2 + 12
        b.titleView.text = textSize.toString()
        builder.setView(b.root)
        builder.setPositiveButton(R.string.ok) { dialogInterface, _ ->
            dialogInterface.dismiss()
            savePrefs()
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
        Dialogues.setFullWidthDialog(dialog, this)
    }

    private fun savePrefs() {
        val sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE)

        sp.edit()
                .putInt(WIDGET_HEADER_BG + widgetID, binding.bgColorSlider.selectedItem)
                .putInt(WIDGET_ITEM_BG + widgetID, binding.listItemBgColorSlider.selectedItem)
                .putFloat(WIDGET_TEXT_SIZE + widgetID, textSize.toFloat())
                .apply()

        val appWidgetManager = AppWidgetManager.getInstance(this)
        EventsWidget.updateWidget(this, appWidgetManager, sp, widgetID)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    companion object {
        const val WIDGET_PREF = "new_events_pref"
        const val WIDGET_HEADER_BG = "new_events_header_bg"
        const val WIDGET_ITEM_BG = "new_events_item_bg"
        const val WIDGET_TEXT_SIZE = "new_events_text_size"
    }
}
