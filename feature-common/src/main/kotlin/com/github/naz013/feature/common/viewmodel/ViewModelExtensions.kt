package com.github.naz013.feature.common.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

fun <T> ViewModel.mutableLiveDataOf(): MutableLiveData<T> = MutableLiveData<T>()
