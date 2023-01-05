package com.elementary.tasks.core.apps

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.launch

class SelectApplicationViewModel(
  dispatcherProvider: DispatcherProvider,
  private val packageManagerWrapper: PackageManagerWrapper
) : BaseProgressViewModel(dispatcherProvider) {

  private val _applications = mutableLiveDataOf<List<UiApplicationList>>()
  val applications = _applications.toLiveData()

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    loadApps()
  }

  private fun loadApps() {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val list = mutableListOf<UiApplicationList>()
      val packages = packageManagerWrapper.getInstalledApplications()
      for (packageInfo in packages) {
        val name = packageInfo.loadLabel(packageManagerWrapper.packageManager).toString()
        val packageName = packageInfo.packageName
        val drawable = packageInfo.loadIcon(packageManagerWrapper.packageManager)
        val data = UiApplicationList(name, packageName, drawable)
        val pos = getPosition(name, list)
        if (pos == -1) {
          list.add(data)
        } else {
          list.add(getPosition(name, list), data)
        }
      }
      withUIContext {
        postInProgress(false)
        _applications.postValue(list)
      }
    }
  }

  private fun getPosition(name: String, mList: MutableList<UiApplicationList>): Int {
    if (mList.size == 0) {
      return 0
    }
    var position = -1
    for (data in mList) {
      val comp = name.compareTo(data.name!!)
      if (comp <= 0) {
        position = mList.indexOf(data)
        break
      }
    }
    return position
  }
}
