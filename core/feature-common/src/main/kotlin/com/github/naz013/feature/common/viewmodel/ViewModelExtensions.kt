package com.github.naz013.feature.common.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.livedata.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

context(_:ViewModel)
fun <T> mutableLiveDataOf(): MutableLiveData<T> = MutableLiveData<T>()

context(_:ViewModel)
fun <T> mutableLiveEventOf(): MutableLiveData<Event<T>> = MutableLiveData<Event<T>>()

context(viewModel:ViewModel)
fun <T> Flow<T>.stateInWhileSubscribed(initialValue: T): StateFlow<T> {
  return stateIn(
    scope = viewModel.viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = initialValue,
  )
}
