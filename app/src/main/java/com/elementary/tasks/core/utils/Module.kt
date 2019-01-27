package com.elementary.tasks.core.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.elementary.tasks.BuildConfig
import com.elementary.tasks.core.appWidgets.buttons.CombinedButtonsWidget
import com.elementary.tasks.core.appWidgets.buttons.CombinedWidgetConfigActivity
import com.elementary.tasks.core.appWidgets.buttons.VoiceWidgetDialog
import com.elementary.tasks.core.appWidgets.calendar.*
import com.elementary.tasks.core.appWidgets.events.EventEditService
import com.elementary.tasks.core.appWidgets.events.EventsService
import com.elementary.tasks.core.appWidgets.events.EventsWidget
import com.elementary.tasks.core.appWidgets.events.EventsWidgetConfigActivity
import com.elementary.tasks.core.appWidgets.googleTasks.TasksService
import com.elementary.tasks.core.appWidgets.googleTasks.TasksWidget
import com.elementary.tasks.core.appWidgets.googleTasks.TasksWidgetConfigActivity
import com.elementary.tasks.core.appWidgets.notes.NotesService
import com.elementary.tasks.core.appWidgets.notes.NotesWidget
import com.elementary.tasks.core.appWidgets.notes.NotesWidgetConfigActivity
import com.elementary.tasks.core.services.CallReceiver
import com.elementary.tasks.core.services.GeolocationService
import timber.log.Timber

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
object Module {

    val isPro: Boolean
        get() = BuildConfig.IS_PRO

    val isPie: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    val isOreoMr1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

    val isOreo: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    val isMarshmallow: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    val isNougat: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    val isNougat1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

    fun isChromeOs(context: Context): Boolean {
        return context.packageManager.hasSystemFeature("org.chromium.arc.device_management")
    }

    fun hasTelephony(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }

    fun hasLocation(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION) && SuperUtil.isGooglePlayServicesAvailable(context)
    }

    fun hasCamera(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    fun hasMicrophone(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun hasFingerprint(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }

    fun hasBluetooth(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }

    private fun supportWidgets(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)
    }

    fun checkComponents(context: Context) {
        val pm = context.packageManager
        Timber.d("checkComponents: $pm")

        if (pm == null) return

        if (Module.hasTelephony(context)) {
            setState(context, pm, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, CallReceiver::class.java)
        } else {
            setState(context, pm, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, CallReceiver::class.java)
        }

        if (Module.hasLocation(context)) {
            setState(context, pm, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, GeolocationService::class.java)
        } else {
            setState(context, pm, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, GeolocationService::class.java)
        }

        if (Module.supportWidgets(context)) {
            setState(context, pm, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    CombinedButtonsWidget::class.java,
                    CombinedWidgetConfigActivity::class.java,
                    EventsWidgetConfigActivity::class.java,
                    NotesWidgetConfigActivity::class.java,
                    TasksWidgetConfigActivity::class.java,
                    EventsWidget::class.java,
                    EventsService::class.java,
                    NotesWidget::class.java,
                    NotesService::class.java,
                    TasksWidget::class.java,
                    TasksService::class.java,
                    CalendarWidgetConfigActivity::class.java,
                    CalendarWidget::class.java,
                    CalendarWeekdayService::class.java,
                    CalendarMonthService::class.java,
                    VoiceWidgetDialog::class.java,
                    CalendarUpdateMinusService::class.java,
                    CalendarUpdateService::class.java,
                    EventEditService::class.java)
        } else {
            setState(context, pm, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    CombinedButtonsWidget::class.java,
                    CombinedWidgetConfigActivity::class.java,
                    EventsWidgetConfigActivity::class.java,
                    NotesWidgetConfigActivity::class.java,
                    TasksWidgetConfigActivity::class.java,
                    EventsWidget::class.java,
                    EventsService::class.java,
                    NotesWidget::class.java,
                    NotesService::class.java,
                    TasksWidget::class.java,
                    TasksService::class.java,
                    CalendarWidgetConfigActivity::class.java,
                    CalendarWidget::class.java,
                    CalendarWeekdayService::class.java,
                    CalendarMonthService::class.java,
                    VoiceWidgetDialog::class.java,
                    CalendarUpdateMinusService::class.java,
                    CalendarUpdateService::class.java,
                    EventEditService::class.java)
        }
    }

    private fun setState(context: Context, pm: PackageManager, state: Int, vararg components: Class<*>) {
        for (clazz in components) {
            val componentName = ComponentName(context, clazz)
            if (!isInSameState(pm, componentName, state)) {
                pm.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP)
            }
        }
    }

    private fun isInSameState(pm: PackageManager, componentName: ComponentName, state: Int): Boolean {
        val componentEnabledSetting = pm.getComponentEnabledSetting(componentName)
        Timber.d("isInSameState: component -> $componentName, current -> $componentEnabledSetting, need -> $state")
        return componentEnabledSetting == state
    }
}
