package com.elementary.tasks.settings.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.cloud.converters.NoteToOldNoteConverter
import com.elementary.tasks.core.os.datapicker.UriPicker
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.databinding.FragmentSettingsObjectExportBinding
import com.elementary.tasks.databinding.ListItemTestsObjectBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.elementary.tasks.notes.SharedNote
import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.logging.Logger
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.ui.common.fragment.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ObjectExportTestFragment : BaseSettingsFragment<FragmentSettingsObjectExportBinding>() {

  private val reminderRepository by inject<ReminderRepository>()
  private val noteRepository by inject<NoteRepository>()
  private val birthdayRepository by inject<BirthdayRepository>()
  private val placeRepository by inject<PlaceRepository>()
  private val reminderGroupRepository by inject<ReminderGroupRepository>()
  private val memoryUtil by inject<MemoryUtil>()
  private val noteToOldNoteConverter by inject<NoteToOldNoteConverter>()

  private val uriPicker = UriPicker(this)

  private val itemAdapter = ItemAdapter { askForFile(it) }

  private var objectType = ObjectType.Reminder

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsObjectExportBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.objectListView.layoutManager = LinearLayoutManager(requireContext())
    binding.objectListView.adapter = itemAdapter

    binding.objectTypeSelector.adapter = ArrayAdapter(
      requireContext(),
      android.R.layout.simple_spinner_dropdown_item,
      getTypes()
    )
    binding.objectTypeSelector.onItemSelectedListener =
      object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
          objectType = ObjectType.entries[position]
          loadList()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
      }

    loadList()
  }

  private fun getTypes(): List<String> {
    return ObjectType.entries.map { it.name }
  }

  private fun loadList() {
    lifecycleScope.launch(Dispatchers.IO) {
      val items = loadItems()

      withUIContext {
        itemAdapter.submitList(items)
      }
    }
  }

  private suspend fun loadItems(): List<Item> {
    return when (objectType) {
      ObjectType.Reminder -> loadReminders()
      ObjectType.Note -> loadNotes()
      ObjectType.Birthday -> loadBirthdays()
      ObjectType.Place -> loadPlaces()
      ObjectType.Group -> loadGroups()
    }
  }

  private suspend fun loadNotes(): List<Item> {
    return noteRepository.getAll().map {
      Item(it.getKey(), it.getSummary() + "\nID: " + it.getKey())
    }
  }

  private suspend fun loadReminders(): List<Item> {
    return reminderRepository.getAll().map {
      Item(it.uuId, it.summary + "\nID: " + it.uuId)
    }
  }

  private suspend fun loadGroups(): List<Item> {
    return reminderGroupRepository.getAll().map {
      Item(it.groupUuId, it.groupTitle + "\nID: " + it.groupUuId)
    }
  }

  private suspend fun loadBirthdays(): List<Item> {
    return birthdayRepository.getAll().map {
      Item(it.uuId, it.name + "\nID: " + it.uuId)
    }
  }

  private suspend fun loadPlaces(): List<Item> {
    return placeRepository.getAll().map {
      Item(it.id, it.name + "\nID: " + it.id)
    }
  }

  private fun askForFile(item: Item) {
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "*/*"
      putExtra(Intent.EXTRA_TITLE, item.title + getFileExt())
    }
    uriPicker.launchIntent(intent) { saveObject(item, it) }
  }

  private fun saveObject(item: Item, uri: Uri?) {
    if (uri == null) {
      Logger.d("OETest", "Uri is NULL")
      return
    }
    lifecycleScope.launch(Dispatchers.IO) {
      val obj = getObject(item)

      if (obj == null) {
        Logger.d("OETest", "Object is NULL, for id = ${item.id}")
        return@launch
      }

      val outputStream = requireContext().contentResolver.openOutputStream(uri)
      if (outputStream == null) {
        Logger.d("OETest", "OutputStream is NULL")
        return@launch
      }

      if (objectType == ObjectType.Note) {
        val oldNote = noteToOldNoteConverter.toSharedNote(obj as NoteWithImages)
        if (oldNote == null) {
          Logger.d("OETest", "OldNote is NULL")
          return@launch
        }

        memoryUtil.toStream(oldNote, outputStream)
      } else {
        memoryUtil.toStream(obj, outputStream)
      }

      withUIContext {
        toast("Object is saved")
      }
    }
  }

  private suspend fun getObject(item: Item): Any? {
    return when (objectType) {
      ObjectType.Reminder -> reminderRepository.getById(item.id)
      ObjectType.Note -> noteRepository.getById(item.id)
      ObjectType.Birthday -> birthdayRepository.getById(item.id)
      ObjectType.Place -> placeRepository.getById(item.id)
      ObjectType.Group -> reminderGroupRepository.getById(item.id)
    }
  }

  private fun getFileExt(): String {
    return when (objectType) {
      ObjectType.Reminder -> FileConfig.FILE_NAME_REMINDER
      ObjectType.Note -> SharedNote.FILE_EXTENSION
      ObjectType.Birthday -> FileConfig.FILE_NAME_BIRTHDAY
      ObjectType.Place -> FileConfig.FILE_NAME_PLACE
      ObjectType.Group -> FileConfig.FILE_NAME_GROUP
    }
  }

  override fun getTitle(): String = "Save object to File"

  private class ItemAdapter(
    private val onItemClicked: (Item) -> Unit
  ) : ListAdapter<Item, ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return ViewHolder(
        parent = parent,
        onClick = { onItemClicked(getItem(it)) }
      )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(getItem(position))
    }
  }

  private class ViewHolder(
    parent: ViewGroup,
    private val onClick: (Int) -> Unit,
    private val binding: ListItemTestsObjectBinding = ListItemTestsObjectBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    )
  ) : RecyclerView.ViewHolder(binding.root) {

    init {
      binding.titleView.setOnClickListener {
        onClick(bindingAdapterPosition)
      }
    }

    fun bind(item: Item) {
      binding.titleView.text = item.title
    }
  }

  private class DiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
      return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
      return oldItem.title == newItem.title
    }
  }

  private data class Item(
    val id: String,
    val title: String
  )

  private enum class ObjectType {
    Reminder,
    Birthday,
    Note,
    Place,
    Group
  }
}
