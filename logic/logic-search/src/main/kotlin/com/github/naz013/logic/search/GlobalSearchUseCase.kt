package com.github.naz013.logic.search

import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.displayTitle
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.logging.Logger
import com.github.naz013.logic.routine.HabitConfig
import com.github.naz013.logic.routine.RoutineConfig
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.HabitRepository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.PlaceRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class GlobalSearchUseCase internal constructor(
  private val reminderRepository: ReminderV2Repository,
  private val noteRepository: NoteRepository,
  private val birthdayRepository: BirthdayRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val groupRepository: GroupV2Repository,
  private val placeRepository: PlaceRepository,
  private val routineRepository: RoutineRepository,
  private val habitRepository: HabitRepository,
  private val tagRepository: TagRepository,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val featureFlags: FeatureFlags,
  private val routineConfig: RoutineConfig,
  private val habitConfig: HabitConfig,
  private val dispatcherProvider: DispatcherProvider
) {

  suspend operator fun invoke(
    query: String,
    limitPerType: Int = DEFAULT_LIMIT_PER_TYPE
  ): List<SearchResult> = withContext(dispatcherProvider.default()) {
    val text = query.trim()
    if (text.length < MIN_QUERY_LENGTH) return@withContext emptyList()
    val normalized = text.lowercase()
    Logger.d(TAG, "Global search, limit per type: $limitPerType")

    val matchedTags = safeSearch("tags") {
      tagRepository.getAll().filter { it.name.lowercase().contains(normalized) }
    }

    val searches = buildList<suspend () -> List<SearchResult>> {
      add { searchReminders(normalized, matchedTags) }
      add { searchNotes(normalized, matchedTags) }
      add { searchBirthdays(normalized, matchedTags) }
      if (featureFlags.isEnabled(FeatureFlag.GOOGLE_TASKS)) {
        add { searchGoogleTasks(normalized, matchedTags) }
      }
      add { searchGroups(normalized) }
      add { searchPlaces(normalized) }
      if (routineConfig.isEnabled) {
        add { searchRoutines(normalized, matchedTags) }
      }
      if (habitConfig.isEnabled) {
        add { searchHabits(normalized) }
      }
    }

    coroutineScope {
      searches.map { search -> async { safeSearch("type") { search().take(limitPerType) } } }
        .awaitAll()
        .flatten()
    }
  }

  private suspend fun searchReminders(query: String, tags: List<Tag>): List<SearchResult> {
    val direct = reminderRepository.search(query).map {
      SearchResult(SearchResultType.REMINDER, it.uuId, it.summary, it.description)
    }
    val tagged = taggedResults(tags, TaggedItemType.REMINDER) { id ->
      reminderRepository.getById(id)?.let {
        SearchResult(SearchResultType.REMINDER, it.uuId, it.summary, it.description)
      }
    }
    return merge(direct, tagged)
  }

  private suspend fun searchNotes(query: String, tags: List<Tag>): List<SearchResult> {
    val direct = noteRepository.search(query).map { it.toResult() }
    val tagged = taggedResults(tags, TaggedItemType.NOTE) { id ->
      noteRepository.getById(id)?.note?.toResult()
    }
    return merge(direct, tagged)
  }

  private suspend fun searchBirthdays(query: String, tags: List<Tag>): List<SearchResult> {
    val direct = birthdayRepository.searchByName(query).map {
      SearchResult(SearchResultType.BIRTHDAY, it.uuId, it.name, it.date)
    }
    val tagged = taggedResults(tags, TaggedItemType.BIRTHDAY) { id ->
      birthdayRepository.getById(id)?.let {
        SearchResult(SearchResultType.BIRTHDAY, it.uuId, it.name, it.date)
      }
    }
    return merge(direct, tagged)
  }

  private suspend fun searchGoogleTasks(query: String, tags: List<Tag>): List<SearchResult> {
    val direct = googleTaskRepository.search(query).map {
      SearchResult(SearchResultType.GOOGLE_TASK, it.taskId, it.title, it.notes.ifBlank { null })
    }
    val tagged = taggedResults(tags, TaggedItemType.GOOGLE_TASK) { id ->
      googleTaskRepository.getById(id)?.let {
        SearchResult(SearchResultType.GOOGLE_TASK, it.taskId, it.title, it.notes.ifBlank { null })
      }
    }
    return merge(direct, tagged)
  }

  private suspend fun searchGroups(query: String): List<SearchResult> {
    return groupRepository.search(query).map {
      SearchResult(SearchResultType.GROUP, it.uuId, it.title)
    }
  }

  private suspend fun searchPlaces(query: String): List<SearchResult> {
    return placeRepository.searchByName(query).map {
      SearchResult(SearchResultType.PLACE, it.id, it.name, it.address.ifBlank { null })
    }
  }

  private suspend fun searchRoutines(query: String, tags: List<Tag>): List<SearchResult> {
    val direct = routineRepository.search(query).map { routine ->
      val matchedStep = routine.steps.firstOrNull { it.title.lowercase().contains(query) }
      SearchResult(
        SearchResultType.ROUTINE,
        routine.id,
        routine.title,
        matchedStep?.title ?: routine.description
      )
    }
    val tagged = taggedResults(tags, TaggedItemType.ROUTINE) { id ->
      routineRepository.getById(id)?.let {
        SearchResult(SearchResultType.ROUTINE, it.id, it.title, it.description)
      }
    }
    return merge(direct, tagged)
  }

  private suspend fun searchHabits(query: String): List<SearchResult> {
    return habitRepository.search(query).map {
      SearchResult(SearchResultType.HABIT, it.id, it.name, it.description)
    }
  }

  private suspend fun taggedResults(
    tags: List<Tag>,
    itemType: TaggedItemType,
    load: suspend (String) -> SearchResult?
  ): List<SearchResult> {
    return tags.flatMap { tag ->
      tagAssignmentRepository.getItemIdsForTag(tag.id, itemType).mapNotNull { id ->
        load(id)?.copy(matchedTag = tag.name)
      }
    }
  }

  private fun merge(direct: List<SearchResult>, tagged: List<SearchResult>): List<SearchResult> {
    val directIds = direct.map { it.id }.toSet()
    return direct + tagged.filter { it.id !in directIds }.distinctBy { it.id }
  }

  private fun Note.toResult(): SearchResult {
    return SearchResult(
      type = SearchResultType.NOTE,
      id = key,
      title = content.displayTitle(),
      subtitle = date.ifBlank { null }
    )
  }

  private suspend fun <T> safeSearch(label: String, block: suspend () -> List<T>): List<T> {
    return try {
      block()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Logger.e(TAG, "Search failed for $label", e)
      emptyList()
    }
  }

  companion object {
    const val DEFAULT_LIMIT_PER_TYPE = 5
    const val MIN_QUERY_LENGTH = 2
    private const val TAG = "GlobalSearchUseCase"
  }
}
