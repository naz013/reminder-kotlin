package com.elementary.tasks.core.utils

import timber.log.Timber

object Logger {

    private const val TAG = "ReminderApp"

    fun d(message: String) {
        Timber.tag(TAG).d(message)
    }

    fun w(message: String, throwable: Throwable?) {
        Timber.tag(TAG).w(throwable, message)
    }
}