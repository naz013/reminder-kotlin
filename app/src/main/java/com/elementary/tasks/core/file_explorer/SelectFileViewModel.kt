package com.elementary.tasks.core.file_explorer

import android.os.Environment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import java.io.File
import java.io.FilenameFilter
import java.util.*

class SelectFileViewModel : ViewModel(), LifecycleObserver {

    var files: MutableLiveData<List<FileItem>> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData()

    private var path = File(Environment.getExternalStorageDirectory().toString() + "")
    private val directoryIcon: Int = R.drawable.ic_twotone_folder_24px
    private val undoIcon: Int = R.drawable.ic_twotone_subdirectory_arrow_left_24px
    var isRoot: Boolean = true
    val str = ArrayList<String>()
    var filType: String = ""

    fun loadFileList(path: File = this.path, firstLevel: Boolean = this.isRoot) {
        isRoot = firstLevel
        isLoading.postValue(true)
        launchDefault {
            try {
                path.mkdirs()
            } catch (e: SecurityException) {
            }

            val list = if (path.exists()) {
                createFilteredFileList(path)
            } else {
                listOf()
            }
            withUIContext {
                files.postValue(list)
                isLoading.postValue(false)
            }
        }
    }

    fun moveUp(s: String, firstLevel: Boolean) {
        path = File(path.toString().substring(0, path.toString().lastIndexOf(s)))
        loadFileList(path, firstLevel)
    }

    private fun createFilteredFileList(path: File): List<FileItem> {
        this.path = path
        val filter = FilenameFilter { dir, filename ->
            val sel = File(dir, filename)
            (sel.isFile || sel.isDirectory) && !sel.isHidden
        }

        val list = try {
            Arrays.asList(*path.list(filter))
        } catch (e: NullPointerException) {
            arrayListOf<String>()
        }

        val dataList = list.asSequence().map { File(path, it) }
                .sortedWith(Comparator { o1, o2 ->
                    if (o1 == null) return@Comparator -1
                    if (o2 == null) return@Comparator 1
                    when {
                        o1.isDirectory == o2.isDirectory -> o1.name.toLowerCase().compareTo(o2.name.toLowerCase())
                        o1.isDirectory -> -1
                        o1.isDirectory -> 1
                        else -> 0
                    }
                })
                .map { FileItem(it.name, if (it.isDirectory) directoryIcon else 0, it.toString()) }
                .toMutableList()

        if (!isRoot) {
            dataList.add(0, FileItem("", undoIcon, "", true))
        }
        return dataList
    }
}