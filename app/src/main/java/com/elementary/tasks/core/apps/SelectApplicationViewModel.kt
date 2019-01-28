package com.elementary.tasks.core.apps

import android.content.pm.PackageManager
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.Job

class SelectApplicationViewModel : ViewModel(), LifecycleObserver {

    var applications: MutableLiveData<List<ApplicationItem>> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var packageManager: PackageManager? = null
    private var job: Job? = null

    fun loadApps() {
        val pm = packageManager ?: return
        if (job != null || !applications.value.isNullOrEmpty()) return
        isLoading.postValue(true)
        job = launchDefault {
            val list: MutableList<ApplicationItem> = mutableListOf()
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (packageInfo in packages) {
                val name = packageInfo.loadLabel(pm).toString()
                val packageName = packageInfo.packageName
                val drawable = packageInfo.loadIcon(pm)
                val data = ApplicationItem(name, packageName, drawable)
                val pos = getPosition(name, list)
                if (pos == -1) {
                    list.add(data)
                } else {
                    list.add(getPosition(name, list), data)
                }
            }
            withUIContext {
                isLoading.postValue(false)
                applications.postValue(list)
            }
            job = null
        }
    }

    private fun getPosition(name: String, mList: MutableList<ApplicationItem>): Int {
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