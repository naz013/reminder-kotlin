package com.github.naz013.feature.common.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.naz013.feature.common.livedata.Event

fun <T> ViewModel.mutableLiveDataOf(): MutableLiveData<T> = MutableLiveData<T>()

fun <T> ViewModel.mutableLiveEventOf(): MutableLiveData<Event<T>> =
  MutableLiveData<Event<T>>()
