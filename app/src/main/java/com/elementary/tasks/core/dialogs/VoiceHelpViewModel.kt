package com.elementary.tasks.core.dialogs

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.utils.Logger
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoiceHelpViewModel(
    private val prefs: Prefs,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel(), DefaultLifecycleObserver {

    private val _urls = mutableLiveDataOf<Urls>()
    val urls = _urls.toLiveData()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        viewModelScope.launch {
            val urls = withContext(dispatcherProvider.default()) {
                val json = prefs.voiceHelpUrls
                Logger.d("voice help json $json")
                parseUrls(json)
            }
            Logger.d("voice help data $urls")
            urls?.also { _urls.postValue(it) }
        }
    }

    private fun parseUrls(json: String): Urls? {
        return Gson().fromJson(json, Urls::class.java)
    }

    data class Urls(
        @SerializedName("urls")
        val urls: List<UrlData> = emptyList()
    )

    data class UrlData(
        @SerializedName("lang")
        val lang: String?,
        @SerializedName("url")
        val url: String?
    )
}
