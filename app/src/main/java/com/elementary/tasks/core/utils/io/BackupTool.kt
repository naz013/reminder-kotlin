package com.elementary.tasks.core.utils.io

import android.content.Context
import android.net.Uri
import androidx.annotation.Keep
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.cloud.completables.ReminderCompletable
import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.note.OldNote
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.ref.WeakReference

class BackupTool(
  private val reminderRepository: ReminderRepository,
  private val reminderCompletable: ReminderCompletable,
  private val context: Context,
  private val dateTimeManager: DateTimeManager,
  private val noteRepository: NoteRepository,
  private val noteToOldNoteConverter: NoteToOldNoteConverter,
  private val memoryUtil: MemoryUtil,
  private val birthdayRepository: BirthdayRepository,
  private val placeRepository: PlaceRepository,
  private val reminderGroupRepository: ReminderGroupRepository
) {

  fun importAll(
    uri: Uri?,
    replace: Boolean = false,
    callback: (Boolean) -> Unit
  ) {
    if (uri == null) {
      callback.invoke(false)
      return
    }
    var stream: InputStream? = null
    runCatching {
      stream = context.contentResolver.openInputStream(uri)
    }
    if (stream == null) {
      callback.invoke(false)
      return
    }
    launchIo {
      try {
        val allData = Gson().fromJson(InputStreamReader(stream), AllData::class.java)
        if (allData != null) {
          var hasAnyData = false
          val defGroup = if (allData.groups.isNotEmpty()) {
            Logger.d("importAll: has groups ${allData.groups.size}")
            hasAnyData = true
            allData.groups.map {
              it.copy(isDefaultGroup = false)
            }
            if (replace) {
              reminderGroupRepository.deleteAll()
              allData.groups[0] = allData.groups[0].copy(isDefaultGroup = true)
            }
            allData.groups.forEach { reminderGroupRepository.save(it) }
            reminderGroupRepository.defaultGroup()
          } else {
            reminderGroupRepository.defaultGroup()
          }

          if (allData.reminders.isNotEmpty()) {
            Logger.d("importAll: has reminders ${allData.reminders.size}")
            hasAnyData = true
            val allGroups = reminderGroupRepository.getAll()
            if (replace) {
              reminderRepository.deleteAll()
            }
            allData.reminders.forEach {
              if (!hasGroup(it.groupUuId, allGroups) && defGroup != null) {
                it.groupUuId = defGroup.groupUuId
                it.groupColor = defGroup.groupColor
                it.groupTitle = defGroup.groupTitle
              }
              reminderRepository.save(it)
              reminderCompletable.action(it)
            }
          }

          if (allData.birthdays.isNotEmpty()) {
            Logger.d("importAll: has birthdays ${allData.birthdays.size}")
            hasAnyData = true
            if (replace) {
              birthdayRepository.deleteAll()
            }
            allData.birthdays.forEach { birthdayRepository.save(it) }
          }

          if (allData.places.isNotEmpty()) {
            Logger.d("importAll: has places ${allData.places.size}")
            hasAnyData = true
            if (replace) {
              placeRepository.deleteAll()
            }
            allData.places.forEach { placeRepository.save(it) }
          }

          if (allData.notes.isNotEmpty()) {
            Logger.d("importAll: has notes ${allData.notes.size}")
            hasAnyData = true
            if (replace) {
              noteRepository.deleteAllImages()
              noteRepository.deleteAllNotes()
            }
            allData.notes.mapNotNull { noteToOldNoteConverter.toNote(it) }
              .filter { it.note != null }
              .forEach {
                it.note?.also { note: Note ->
                  it.images.forEach { image ->
                    noteRepository.save(image)
                  }
                  noteRepository.save(note)
                }
              }
          }
          withUIContext { callback.invoke(hasAnyData) }
        } else {
          withUIContext { callback.invoke(false) }
        }
      } catch (e: Exception) {
        withUIContext { callback.invoke(false) }
      }
    }
  }

  private fun hasGroup(uuId: String, list: List<ReminderGroup>): Boolean {
    for (g in list) {
      if (uuId == g.groupUuId) {
        return true
      }
    }
    return false
  }

  suspend fun exportAll(): File? {
    val allData = AllData(
      reminders = reminderRepository.getAll(),
      groups = reminderGroupRepository.getAll().toMutableList(),
      notes = noteRepository.getAll().mapNotNull { noteToOldNoteConverter.toOldNote(it) },
      places = placeRepository.getAll(),
      birthdays = birthdayRepository.getAll()
    )
    return createAllDataFile(allData)
  }

  private fun createAllDataFile(item: AllData): File? {
    val jsonData = WeakReference(Gson().toJson(item))
    val file: File
    val dir = context.externalCacheDir ?: context.cacheDir
    return if (dir != null) {
      val exportFileName = dateTimeManager.getNowGmtDateTime() + FileConfig.FILE_NAME_FULL_BACKUP
      file = File(dir, exportFileName)
      try {
        memoryUtil.writeFileNoEncryption(file, jsonData.get())
        jsonData.clear()
        file
      } catch (e: Exception) {
        e.printStackTrace()
        null
      }
    } else {
      null
    }
  }

  fun reminderToFile(item: Reminder): File? {
    return anyToFile(item, item.uuId + FileConfig.FILE_NAME_REMINDER)
  }

  fun noteToFile(item: NoteWithImages?): File? {
    val note = item?.note ?: return null
    return anyToFile(item, note.key + FileConfig.FILE_NAME_NOTE)
  }

  fun placeToFile(item: Place): File? {
    return anyToFile(item, item.id + FileConfig.FILE_NAME_PLACE)
  }

  private fun anyToFile(any: Any, fileName: String): File? {
    val cacheDir = context.externalCacheDir ?: context.cacheDir
    val file = File(cacheDir, fileName)
    if (!file.createNewFile()) {
      try {
        file.delete()
        file.createNewFile()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    return try {
      val outputStream = FileOutputStream(file)
      return if (memoryUtil.toStream(any, outputStream)) {
        outputStream.flush()
        outputStream.close()
        file
      } else {
        outputStream.flush()
        outputStream.close()
        null
      }
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  @Keep
  data class AllData(
    @SerializedName("reminders")
    var reminders: List<Reminder> = listOf(),
    @SerializedName("groups")
    var groups: MutableList<ReminderGroup> = mutableListOf(),
    @SerializedName("notes")
    var notes: List<OldNote> = listOf(),
    @SerializedName("places")
    var places: List<Place> = listOf(),
    @SerializedName("birthdays")
    var birthdays: List<Birthday> = listOf()
  )
}
