package com.github.naz013.files

import android.util.Base64
import android.util.Base64InputStream
import android.util.Base64OutputStream
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TagAssignment
import com.github.naz013.domain.note.OldNote
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.reminder.migration.toGroupV2
import com.github.naz013.domain.reminder.migration.toReminderV2
import com.github.naz013.domain.reminder.v2.CalendarExportSettings
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.LocationSettings
import com.github.naz013.domain.reminder.v2.LockScreenVisibility
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.ReminderAction
import com.github.naz013.domain.reminder.v2.ReminderNotificationCategory
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ShopItemV2
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.reminder.v2.TaskExportSettings
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineExecutionRecord
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.domain.workflow.ScheduleRecurrence
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.files.model.CalendarExportSettingsJson
import com.github.naz013.files.model.GroupV2Json
import com.github.naz013.files.model.LocationSettingsJson
import com.github.naz013.files.model.NoteV3Json
import com.github.naz013.files.model.NotificationSettingsOverrideJson
import com.github.naz013.files.model.ReminderV2Json
import com.github.naz013.files.model.RoutineExecutionJson
import com.github.naz013.files.model.RoutineJson
import com.github.naz013.files.model.RoutineStepJson
import com.github.naz013.files.model.SharedNote
import com.github.naz013.files.model.ShopItemV2Json
import com.github.naz013.files.model.TagJson
import com.github.naz013.files.model.TaskExportSettingsJson
import com.github.naz013.files.model.WorkflowRuleJson
import com.github.naz013.files.model.WorkflowTemplateJson
import com.github.naz013.logging.Logger
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

internal class DataConverterImpl : DataConverter {

  override suspend fun toOutputStream(any: Any, outputStream: OutputStream) {
    try {
      val output64 = Base64OutputStream(outputStream, Base64.DEFAULT)
      val bufferedWriter = BufferedWriter(OutputStreamWriter(output64, StandardCharsets.UTF_8))
      val writer = JsonWriter(bufferedWriter)
      val data = any.toJson()
      val type = when (data) {
        is ReminderV2Json -> object : TypeToken<ReminderV2Json>() {}.type
        is Place -> object : TypeToken<Place>() {}.type
        is Birthday -> object : TypeToken<Birthday>() {}.type
        is GroupV2Json -> object : TypeToken<GroupV2Json>() {}.type
        is RecurPreset -> object : TypeToken<RecurPreset>() {}.type
        is NoteV3Json -> object : TypeToken<NoteV3Json>() {}.type
        is SharedNote -> object : TypeToken<SharedNote>() {}.type
        is Reminder -> object : TypeToken<Reminder>() {}.type
        is ReminderGroup -> object : TypeToken<ReminderGroup>() {}.type
        is OldNote -> object : TypeToken<OldNote>() {}.type
        is TagJson -> object : TypeToken<TagJson>() {}.type
        is TagAssignment -> object : TypeToken<TagAssignment>() {}.type
        is RoutineJson -> object : TypeToken<RoutineJson>() {}.type
        is RoutineExecutionJson -> object : TypeToken<RoutineExecutionJson>() {}.type
        is WorkflowRuleJson -> object : TypeToken<WorkflowRuleJson>() {}.type
        is WorkflowTemplateJson -> object : TypeToken<WorkflowTemplateJson>() {}.type
        else -> null
      } ?: run {
        throw IllegalArgumentException("Unsupported type: ${any::class.java}")
      }
      Gson().toJson(data, type, writer)
      writer.close()
      output64.close()
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to write to OutputStream: $e")
      throw e
    } finally {
      outputStream.close()
    }
  }

  override suspend fun toInputStream(any: Any): InputStream {
    val outputStream = CopyByteArrayStream()
    try {
      toOutputStream(any, outputStream)
      return outputStream.toInputStream()
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to create InputStream: $e")
      throw e
    }
  }

  override suspend fun toData(stream: InputStream): Any {
    val output64 = Base64InputStream(stream, Base64.DEFAULT)
    val bufferedReader = BufferedReader(InputStreamReader(output64))
    val jsonElement = JsonReader(bufferedReader).use { reader -> JsonParser.parseReader(reader) }
    if (jsonElement !is JsonObject) {
      throw IllegalArgumentException("Expected a JSON object but got: $jsonElement")
    }
    val clazz = detectClass(jsonElement)
    return Gson().fromJson(jsonElement, clazz).toDomain()
  }

  companion object {
    private const val TAG = "DataConverter"
  }
}

private fun Any.toJson(): Any {
  return when (this) {
    is ReminderV2 -> this.toJson()
    is GroupV2 -> this.toJson()
    is Tag -> this.toJson()
    is Routine -> this.toJson()
    is RoutineExecutionRecord -> this.toJson()
    is WorkflowRule -> this.toJson()
    is WorkflowTemplate -> this.toJson()
    else -> this
  }
}

/**
 * Guesses the target class from the JSON object's own field names, since callers no longer supply
 * a [DataType]. Each branch is keyed on field(s) unique to that schema; [SharedNote]/[NoteV3Json]/
 * [OldNote] overlap the most (near-identical note formats), so they're ordered most-specific-first
 * and rely on `title` being present for every [NoteV3Json] (even when empty, Gson still writes it)
 * but absent from the older [OldNote] schema.
 */
private fun detectClass(json: JsonObject): Class<*> = when {
  json.has("recurrenceType") && json.has("actionType") -> ReminderV2Json::class.java
  json.has("steps") && json.has("recurrenceType") -> RoutineJson::class.java
  json.has("completedStepIds") -> RoutineExecutionJson::class.java
  json.has("scopeType") && json.has("triggerType") -> WorkflowRuleJson::class.java
  json.has("category") && json.has("triggerType") -> WorkflowTemplateJson::class.java
  json.has("notification") && json.has("createdAt") -> GroupV2Json::class.java
  json.has("tagId") && json.has("itemId") && json.has("itemType") -> TagAssignment::class.java
  json.has("id") && json.has("name") && json.has("color") -> TagJson::class.java
  json.has("eventTime") && json.has("startTime") -> Reminder::class.java
  json.has("isDefaultGroup") -> ReminderGroup::class.java
  json.has("showedYear") || json.has("contactId") -> Birthday::class.java
  json.has("latitude") && json.has("longitude") -> Place::class.java
  json.has("recurObject") -> RecurPreset::class.java
  json.has("text") && json.has("opacity") -> SharedNote::class.java
  json.has("images") && json.has("title") -> NoteV3Json::class.java
  json.has("images") -> OldNote::class.java
  else -> throw IllegalArgumentException("Unable to determine object type from JSON structure: ${json.keySet()}")
}

private fun Any.toDomain(): Any {
  return when (this) {
    is GroupV2Json -> this.toDomain()
    is ReminderV2Json -> this.toDomain()
    is TagJson -> this.toDomain()
    is RoutineJson -> this.toDomain()
    is RoutineExecutionJson -> this.toDomain()
    is WorkflowRuleJson -> this.toDomain()
    is WorkflowTemplateJson -> this.toDomain()
    is ReminderGroup -> this.toGroupV2()
    is Reminder -> this.toReminderV2()
    else -> this
  }
}

private fun GroupV2Json.toDomain(): GroupV2 = GroupV2(
  uuId = uuId,
  title = title,
  color = color,
  isDefault = isDefault,
  notification = notification.toDomain(),
  createdAt = LocalDateTime.parse(createdAt, jsonDateTimeFormatter),
  version = version
)

private fun TagJson.toDomain(): Tag = Tag(
  id = id,
  name = name,
  color = color,
  version = version,
  syncState = SyncState.Synced
)

private fun RoutineJson.toDomain(): Routine = Routine(
  id = id,
  title = title,
  description = description,
  color = color,
  isPinned = isPinned,
  icon = icon,
  steps = steps.map { it.toDomain() },
  autoAdvance = autoAdvance,
  soundAlertsEnabled = soundAlertsEnabled,
  recurrence = toRoutineRecurrenceRule(recurrenceType, recurrencePayload),
  lastResetAt = lastResetAt?.let { LocalDateTime.parse(it, jsonDateTimeFormatter) },
  createdAt = LocalDateTime.parse(createdAt, jsonDateTimeFormatter),
  updatedAt = LocalDateTime.parse(updatedAt, jsonDateTimeFormatter),
  sync = SyncMetadata(version = version)
)

private fun RoutineStepJson.toDomain(): RoutineStep = RoutineStep(
  id = id,
  title = title,
  description = description,
  durationSeconds = durationSeconds,
  scheduledTime = scheduledTime,
  isCompleted = isCompleted,
  order = order
)

private fun RoutineExecutionJson.toDomain(): RoutineExecutionRecord = RoutineExecutionRecord(
  id = id,
  routineId = routineId,
  executedAt = LocalDateTime.parse(executedAt, jsonDateTimeFormatter),
  totalTimeSpentSeconds = totalTimeSpentSeconds,
  completedStepIds = completedStepIds,
  totalStepsCount = totalStepsCount
)

/** Internal, not private, so [DataConverterImplTest] can round-trip it directly without going
 * through the Base64-wrapped `toInputStream`/`toData` pipeline - `android.util.Base64OutputStream`
 * isn't available under plain JUnit and Robolectric's shadow doesn't cover its streaming API
 * either (see the `@Ignore`d `SyncDataConverterImplSettingsTest` in `app` for the same wall). */
internal fun WorkflowRuleJson.toDomain(): WorkflowRule = WorkflowRule(
  uuId = uuId,
  title = title,
  templateId = templateId,
  scope = toWorkflowScope(scopeType, scopeId),
  trigger = toWorkflowTrigger(triggerType, triggerPayload),
  conditions = conditionsPayload.toWorkflowConditions(),
  action = toWorkflowAction(actionType, actionPayload),
  isEnabled = isEnabled,
  createdAt = LocalDateTime.parse(createdAt, jsonDateTimeFormatter),
  lastRunAt = lastRunAt?.let { LocalDateTime.parse(it, jsonDateTimeFormatter) },
  version = version,
  syncState = SyncState.Synced
)

internal fun WorkflowTemplateJson.toDomain(): WorkflowTemplate = WorkflowTemplate(
  id = id,
  title = title,
  description = description,
  category = runCatching { WorkflowTemplateCategory.valueOf(category) }
    .getOrDefault(WorkflowTemplateCategory.REMINDER_LIFECYCLE),
  supportedScopeTypes = supportedScopeTypes.mapNotNull { runCatching { WorkflowScopeType.valueOf(it) }.getOrNull() },
  trigger = toWorkflowTrigger(triggerType, triggerPayload),
  action = toWorkflowAction(actionType, actionPayload),
  isBuiltIn = isBuiltIn,
  useCount = useCount,
  createdAt = LocalDateTime.parse(createdAt, jsonDateTimeFormatter),
  version = version,
  syncState = SyncState.Synced
)

private fun WorkflowScope.toWorkflowScopeColumns(): Pair<String, String?> = when (this) {
  is WorkflowScope.Global -> "GLOBAL" to null
  is WorkflowScope.ForGroup -> "GROUP" to groupId
  is WorkflowScope.ForReminder -> "REMINDER" to reminderId
}

private fun toWorkflowScope(scopeType: String, scopeId: String?): WorkflowScope = when (scopeType) {
  "GROUP" -> scopeId?.let { WorkflowScope.ForGroup(it) } ?: WorkflowScope.Global
  "REMINDER" -> scopeId?.let { WorkflowScope.ForReminder(it) } ?: WorkflowScope.Global
  else -> WorkflowScope.Global
}

/** [WorkflowTrigger.ScheduleReached.atDateTime] is deliberately NOT passed through
 * `workflowGson.toJson(this)` like every other payload field - plain `Gson()` has no registered
 * adapter for ThreeTenBP's `LocalDateTime`, so reflecting into its internal fields would be
 * unannotated and unsafe under R8 (see the class doc on [WorkflowTrigger]). Formatting it with
 * [jsonDateTimeFormatter] first keeps the wire payload to types Gson already handles safely. */
private data class ScheduleReachedColumns(val atDateTime: String, val recurrence: String)

private fun WorkflowTrigger.toColumns(): Pair<String, String> = when (this) {
  is WorkflowTrigger.ReminderCreated -> "REMINDER_CREATED" to ""
  is WorkflowTrigger.ReminderCompleted -> "REMINDER_COMPLETED" to ""
  is WorkflowTrigger.ReminderSnoozedNTimes -> "REMINDER_SNOOZED_N_TIMES" to workflowGson.toJson(this)
  is WorkflowTrigger.GroupAllCompleted -> "GROUP_ALL_COMPLETED" to ""
  is WorkflowTrigger.LocationEntered -> "LOCATION_ENTERED" to ""
  is WorkflowTrigger.LocationExited -> "LOCATION_EXITED" to ""
  is WorkflowTrigger.ReminderAgeExceeded -> "REMINDER_AGE_EXCEEDED" to workflowGson.toJson(this)
  is WorkflowTrigger.ReminderUnacknowledgedFor -> "REMINDER_UNACKNOWLEDGED_FOR" to workflowGson.toJson(this)
  is WorkflowTrigger.ScheduleReached -> "SCHEDULE_REACHED" to workflowGson.toJson(
    ScheduleReachedColumns(atDateTime.format(jsonDateTimeFormatter), recurrence.name)
  )
}

/** Falls back to [WorkflowTrigger.ReminderCompleted] (and logs) instead of throwing on a payload
 * it can't parse. Mirrors `WorkflowTriggerActionCodec.toWorkflowTrigger` in the `repository`
 * module (the Room-layer equivalent of this same wire shape) - kept as a separate copy rather
 * than a shared function since that codec is `internal` to the `repository` module. */
private fun toWorkflowTrigger(type: String, payload: String): WorkflowTrigger = runCatching {
  when (type) {
    "REMINDER_CREATED" -> WorkflowTrigger.ReminderCreated
    "REMINDER_COMPLETED" -> WorkflowTrigger.ReminderCompleted
    "REMINDER_SNOOZED_N_TIMES" -> workflowGson.fromJson(payload, WorkflowTrigger.ReminderSnoozedNTimes::class.java)
    "GROUP_ALL_COMPLETED" -> WorkflowTrigger.GroupAllCompleted
    "LOCATION_ENTERED" -> WorkflowTrigger.LocationEntered
    "LOCATION_EXITED" -> WorkflowTrigger.LocationExited
    "REMINDER_AGE_EXCEEDED" -> workflowGson.fromJson(payload, WorkflowTrigger.ReminderAgeExceeded::class.java)
    "REMINDER_UNACKNOWLEDGED_FOR" ->
      workflowGson.fromJson(payload, WorkflowTrigger.ReminderUnacknowledgedFor::class.java)
    "SCHEDULE_REACHED" -> workflowGson.fromJson(payload, ScheduleReachedColumns::class.java).let {
      WorkflowTrigger.ScheduleReached(
        atDateTime = LocalDateTime.parse(it.atDateTime, jsonDateTimeFormatter),
        recurrence = runCatching { ScheduleRecurrence.valueOf(it.recurrence) }.getOrDefault(ScheduleRecurrence.ONCE)
      )
    }
    else -> WorkflowTrigger.ReminderCompleted
  }
}.getOrElse { e ->
  Logger.e(FILES_TAG, "Failed to parse workflow trigger, type=$type, payload=$payload", e)
  WorkflowTrigger.ReminderCompleted
}

private fun WorkflowAction.toColumns(): Pair<String, String> = when (this) {
  is WorkflowAction.ArchiveReminder -> "ARCHIVE_REMINDER" to ""
  is WorkflowAction.CompleteReminder -> "COMPLETE_REMINDER" to ""
  is WorkflowAction.PurgeReminder -> "PURGE_REMINDER" to ""
  is WorkflowAction.ApplyNotificationOverride -> "APPLY_NOTIFICATION_OVERRIDE" to workflowGson.toJson(this)
  is WorkflowAction.ClearNotificationOverride -> "CLEAR_NOTIFICATION_OVERRIDE" to ""
  is WorkflowAction.ActivateReminder -> "ACTIVATE_REMINDER" to workflowGson.toJson(this)
  is WorkflowAction.MoveToGroup -> "MOVE_TO_GROUP" to workflowGson.toJson(this)
  is WorkflowAction.SendBroadcastIntent -> "SEND_BROADCAST_INTENT" to workflowGson.toJson(this)
  is WorkflowAction.RunBackgroundTask -> "RUN_BACKGROUND_TASK" to workflowGson.toJson(this)
  is WorkflowAction.ApplyTag -> "APPLY_TAG" to workflowGson.toJson(this)
  is WorkflowAction.RemoveTag -> "REMOVE_TAG" to workflowGson.toJson(this)
}

/** Falls back to [WorkflowAction.ArchiveReminder] (and logs) instead of throwing on a payload it
 * can't parse - same reasoning and repository-module counterpart as [toWorkflowTrigger]. */
private fun toWorkflowAction(type: String, payload: String): WorkflowAction = runCatching {
  when (type) {
    "ARCHIVE_REMINDER" -> WorkflowAction.ArchiveReminder
    "COMPLETE_REMINDER" -> WorkflowAction.CompleteReminder
    "PURGE_REMINDER" -> WorkflowAction.PurgeReminder
    "APPLY_NOTIFICATION_OVERRIDE" ->
      workflowGson.fromJson(payload, WorkflowAction.ApplyNotificationOverride::class.java)
    "CLEAR_NOTIFICATION_OVERRIDE" -> WorkflowAction.ClearNotificationOverride
    "ACTIVATE_REMINDER" -> workflowGson.fromJson(payload, WorkflowAction.ActivateReminder::class.java)
    "MOVE_TO_GROUP" -> workflowGson.fromJson(payload, WorkflowAction.MoveToGroup::class.java)
    "SEND_BROADCAST_INTENT" -> workflowGson.fromJson(payload, WorkflowAction.SendBroadcastIntent::class.java)
    "RUN_BACKGROUND_TASK" -> workflowGson.fromJson(payload, WorkflowAction.RunBackgroundTask::class.java)
    "APPLY_TAG" -> workflowGson.fromJson(payload, WorkflowAction.ApplyTag::class.java)
    "REMOVE_TAG" -> workflowGson.fromJson(payload, WorkflowAction.RemoveTag::class.java)
    else -> WorkflowAction.ArchiveReminder
  }
}.getOrElse { e ->
  Logger.e(FILES_TAG, "Failed to parse workflow action, type=$type, payload=$payload", e)
  WorkflowAction.ArchiveReminder
}

private data class WorkflowConditionColumns(val type: String, val payload: String)

private fun WorkflowCondition.toWorkflowConditionColumns(): WorkflowConditionColumns = when (this) {
  is WorkflowCondition.PriorityAtLeast -> WorkflowConditionColumns("PRIORITY_AT_LEAST", workflowGson.toJson(this))
  is WorkflowCondition.WithinTimeWindow -> WorkflowConditionColumns("WITHIN_TIME_WINDOW", workflowGson.toJson(this))
  is WorkflowCondition.GroupIs -> WorkflowConditionColumns("GROUP_IS", workflowGson.toJson(this))
  is WorkflowCondition.TitleContains -> WorkflowConditionColumns("TITLE_CONTAINS", workflowGson.toJson(this))
  is WorkflowCondition.HasTag -> WorkflowConditionColumns("HAS_TAG", workflowGson.toJson(this))
}

/** Drops (and logs) a condition it can't parse, rather than throwing - one bad condition must not
 * take down the whole rule/conditions list; [toWorkflowConditions] already filters out nulls. */
private fun toWorkflowCondition(columns: WorkflowConditionColumns): WorkflowCondition? = runCatching {
  when (columns.type) {
    "PRIORITY_AT_LEAST" -> workflowGson.fromJson(columns.payload, WorkflowCondition.PriorityAtLeast::class.java)
    "WITHIN_TIME_WINDOW" -> workflowGson.fromJson(columns.payload, WorkflowCondition.WithinTimeWindow::class.java)
    "GROUP_IS" -> workflowGson.fromJson(columns.payload, WorkflowCondition.GroupIs::class.java)
    "TITLE_CONTAINS" -> workflowGson.fromJson(columns.payload, WorkflowCondition.TitleContains::class.java)
    "HAS_TAG" -> workflowGson.fromJson(columns.payload, WorkflowCondition.HasTag::class.java)
    else -> null
  }
}.getOrElse { e ->
  Logger.e(FILES_TAG, "Failed to parse workflow condition, columns=$columns", e)
  null
}

private val workflowConditionColumnsListType = object : TypeToken<List<WorkflowConditionColumns>>() {}.type

private fun List<WorkflowCondition>.toConditionsPayload(): String =
  workflowGson.toJson(map { it.toWorkflowConditionColumns() })

private fun String.toWorkflowConditions(): List<WorkflowCondition> =
  runCatching { workflowGson.fromJson<List<WorkflowConditionColumns>>(this, workflowConditionColumnsListType) }
    .getOrNull()
    .orEmpty()
    .mapNotNull { toWorkflowCondition(it) }

private fun ReminderV2Json.toDomain(): ReminderV2 = ReminderV2(
  uuId = uuId,
  summary = summary,
  description = description,
  noteId = noteId,
  groupId = groupId,
  recurrence = toRecurrenceRule(recurrenceType, recurrencePayload),
  schedule = ReminderSchedule(
    startDateTime = LocalDateTime.parse(startDateTime, jsonDateTimeFormatter),
    eventDateTime = eventDateTime?.let { LocalDateTime.parse(it, jsonDateTimeFormatter) },
    updatedAt = updatedAt?.let { LocalDateTime.parse(it, jsonDateTimeFormatter) }
  ),
  notification = notification.toDomain(),
  calendarExport = calendarExport?.toDomain(),
  taskExport = taskExport?.toDomain(),
  location = location?.toDomain(),
  action = toReminderAction(actionType, actionTarget, actionSubject),
  attachmentFiles = attachmentFiles,
  places = places,
  shoppingItems = shoppingItems.map { it.toDomain() },
  builderScheme = builderScheme,
  uniqueId = uniqueId,
  isActive = isActive,
  isRemoved = isRemoved,
  isPinned = isPinned,
  eventCount = eventCount,
  sync = SyncMetadata(version = version),
  offlineOnly = offlineOnly
)

private fun NotificationSettingsOverrideJson.toDomain(): NotificationSettingsOverride =
  NotificationSettingsOverride(
    color = color,
    vibrate = vibrate,
    vibrationPattern = vibrationPattern,
    repeatNotification = repeatNotification,
    volume = volume,
    soundUri = soundUri,
    quietHoursFrom = quietHoursFrom,
    quietHoursTo = quietHoursTo,
    activeHours = activeHours,
    delayMinutes = delayMinutes,
    priority = priority?.let { runCatching { ReminderPriority.valueOf(it) }.getOrNull() },
    category = category?.let { runCatching { ReminderNotificationCategory.valueOf(it) }.getOrNull() },
    bypassDoNotDisturb = bypassDoNotDisturb,
    wakeScreen = wakeScreen,
    lockScreenVisibility = lockScreenVisibility?.let {
      runCatching { LockScreenVisibility.valueOf(it) }.getOrNull()
    },
    remindBefore = remindBefore
  )

private fun CalendarExportSettingsJson.toDomain(): CalendarExportSettings = CalendarExportSettings(
  calendarId = calendarId,
  duration = duration,
  allDay = allDay
)

private fun TaskExportSettingsJson.toDomain(): TaskExportSettings = TaskExportSettings(
  taskListId = taskListId
)

private fun LocationSettingsJson.toDomain(): LocationSettings = LocationSettings(
  isNotificationShown = isNotificationShown,
  isLocked = isLocked,
  hasDelayedReminder = hasDelayedReminder
)

private fun ShopItemV2Json.toDomain(): ShopItemV2 = ShopItemV2(
  uuId = uuId,
  summary = summary,
  isChecked = isChecked,
  isDeleted = isDeleted,
  createdAt = LocalDateTime.parse(createdAt, jsonDateTimeFormatter)
)

/** Falls back to [RecurrenceRule.Once] (and logs) instead of throwing on a payload it can't
 * parse — e.g. a backup file written before [RecurrenceRule]'s fields were `@SerializedName`
 * protected — so one unreadable reminder doesn't fail the whole backup restore. Mirrors
 * `ReminderV2Mapper.toRecurrenceRule` in the `repository` module. */
private fun toRecurrenceRule(type: String, payload: String): RecurrenceRule = runCatching {
  when (type) {
    "ONCE" -> RecurrenceRule.Once
    "COUNTDOWN" -> recurrenceGson.fromJson(payload, RecurrenceRule.Countdown::class.java)
    "DAILY" -> recurrenceGson.fromJson(payload, RecurrenceRule.Daily::class.java)
    "WEEKLY" -> recurrenceGson.fromJson(payload, RecurrenceRule.Weekly::class.java).also {
      requireNotNull(it.weekdays) { "weekdays is null" }
    }
    "MONTHLY" -> recurrenceGson.fromJson(payload, RecurrenceRule.Monthly::class.java)
    "RELATIVE_MONTHLY" -> recurrenceGson.fromJson(payload, RecurrenceRule.RelativeMonthly::class.java)
    "YEARLY" -> recurrenceGson.fromJson(payload, RecurrenceRule.Yearly::class.java)
    "LOCATION_ENTER" -> RecurrenceRule.LocationEnter
    "LOCATION_EXIT" -> RecurrenceRule.LocationExit
    "ICALENDAR" -> recurrenceGson.fromJson(payload, RecurrenceRule.ICalendar::class.java).also {
      requireNotNull(it.rrule) { "rrule is null" }
    }
    else -> RecurrenceRule.Once
  }
}.getOrElse { e ->
  Logger.e(FILES_TAG, "Failed to parse recurrence rule, type=$type, payload=$payload", e)
  RecurrenceRule.Once
}

/** Same [RecurrenceRule] type+payload wire shape as [toRecurrenceRule], but nullable - a `Routine`
 * can be on-demand ([Routine.recurrence] == null, wire type `"NONE"`), unlike [ReminderV2] which
 * always has a rule. Mirrors `RoutineMapper.toRecurrenceRule` in the `repository` module (the
 * Room-layer equivalent of this same split), including its fallback to `null` instead of throwing
 * on a payload it can't parse. */
private fun toRoutineRecurrenceRule(type: String, payload: String): RecurrenceRule? = runCatching {
  when (type) {
    "NONE" -> null
    "ONCE" -> RecurrenceRule.Once
    "COUNTDOWN" -> recurrenceGson.fromJson(payload, RecurrenceRule.Countdown::class.java)
    "DAILY" -> recurrenceGson.fromJson(payload, RecurrenceRule.Daily::class.java)
    "WEEKLY" -> recurrenceGson.fromJson(payload, RecurrenceRule.Weekly::class.java).also {
      requireNotNull(it.weekdays) { "weekdays is null" }
    }
    "MONTHLY" -> recurrenceGson.fromJson(payload, RecurrenceRule.Monthly::class.java)
    "RELATIVE_MONTHLY" -> recurrenceGson.fromJson(payload, RecurrenceRule.RelativeMonthly::class.java)
    "YEARLY" -> recurrenceGson.fromJson(payload, RecurrenceRule.Yearly::class.java)
    "LOCATION_ENTER" -> RecurrenceRule.LocationEnter
    "LOCATION_EXIT" -> RecurrenceRule.LocationExit
    "ICALENDAR" -> recurrenceGson.fromJson(payload, RecurrenceRule.ICalendar::class.java).also {
      requireNotNull(it.rrule) { "rrule is null" }
    }
    else -> null
  }
}.getOrElse { e ->
  Logger.e(FILES_TAG, "Failed to parse routine recurrence rule, type=$type, payload=$payload", e)
  null
}

private fun RecurrenceRule?.toRoutineColumns(): Pair<String, String> = when (this) {
  null -> "NONE" to ""
  is RecurrenceRule.Once -> "ONCE" to ""
  is RecurrenceRule.Countdown -> "COUNTDOWN" to recurrenceGson.toJson(this)
  is RecurrenceRule.Daily -> "DAILY" to recurrenceGson.toJson(this)
  is RecurrenceRule.Weekly -> "WEEKLY" to recurrenceGson.toJson(this)
  is RecurrenceRule.Monthly -> "MONTHLY" to recurrenceGson.toJson(this)
  is RecurrenceRule.RelativeMonthly -> "RELATIVE_MONTHLY" to recurrenceGson.toJson(this)
  is RecurrenceRule.Yearly -> "YEARLY" to recurrenceGson.toJson(this)
  is RecurrenceRule.LocationEnter -> "LOCATION_ENTER" to ""
  is RecurrenceRule.LocationExit -> "LOCATION_EXIT" to ""
  is RecurrenceRule.ICalendar -> "ICALENDAR" to recurrenceGson.toJson(this)
}

private fun toReminderAction(type: String, target: String, subject: String): ReminderAction =
  when (type) {
    "NONE" -> ReminderAction.None
    "CALL" -> ReminderAction.Call(target)
    "SMS" -> ReminderAction.Sms(target, subject)
    "LINK" -> ReminderAction.Link(target)
    "APP" -> ReminderAction.App(target)
    "EMAIL" -> ReminderAction.Email(target, subject)
    "SHOPPING" -> ReminderAction.Shopping
    else -> ReminderAction.None
  }

private val jsonDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
private val recurrenceGson = Gson()
private val workflowGson = Gson()
private const val FILES_TAG = "DataConverter"

private fun GroupV2.toJson(): GroupV2Json {
  return GroupV2Json(
    uuId = uuId,
    title = title,
    color = color,
    isDefault = isDefault,
    notification = notification.toJson(),
    createdAt = createdAt.format(jsonDateTimeFormatter),
    version = version
  )
}

private fun Tag.toJson(): TagJson = TagJson(
  id = id,
  name = name,
  color = color,
  version = version
)

private fun Routine.toJson(): RoutineJson {
  val (recurrenceType, recurrencePayload) = recurrence.toRoutineColumns()
  return RoutineJson(
    id = id,
    title = title,
    description = description,
    color = color,
    isPinned = isPinned,
    icon = icon,
    steps = steps.map { it.toJson() },
    autoAdvance = autoAdvance,
    soundAlertsEnabled = soundAlertsEnabled,
    recurrenceType = recurrenceType,
    recurrencePayload = recurrencePayload,
    lastResetAt = lastResetAt?.format(jsonDateTimeFormatter),
    createdAt = createdAt.format(jsonDateTimeFormatter),
    updatedAt = updatedAt.format(jsonDateTimeFormatter),
    version = sync.version
  )
}

private fun RoutineStep.toJson(): RoutineStepJson = RoutineStepJson(
  id = id,
  title = title,
  description = description,
  durationSeconds = durationSeconds,
  scheduledTime = scheduledTime,
  isCompleted = isCompleted,
  order = order
)

private fun RoutineExecutionRecord.toJson(): RoutineExecutionJson = RoutineExecutionJson(
  id = id,
  routineId = routineId,
  executedAt = executedAt.format(jsonDateTimeFormatter),
  totalTimeSpentSeconds = totalTimeSpentSeconds,
  completedStepIds = completedStepIds,
  totalStepsCount = totalStepsCount
)

private fun ReminderV2.toJson(): ReminderV2Json {
  val (recurrenceType, recurrencePayload) = recurrence.toColumns()
  val (actionType, actionTarget, actionSubject) = action.toColumns()
  return ReminderV2Json(
    uuId = uuId,
    summary = summary,
    description = description,
    noteId = noteId,
    groupId = groupId,
    recurrenceType = recurrenceType,
    recurrencePayload = recurrencePayload,
    startDateTime = schedule.startDateTime.format(jsonDateTimeFormatter),
    eventDateTime = schedule.eventDateTime?.format(jsonDateTimeFormatter),
    updatedAt = schedule.updatedAt?.format(jsonDateTimeFormatter),
    notification = notification.toJson(),
    calendarExport = calendarExport?.toJson(),
    taskExport = taskExport?.toJson(),
    location = location?.toJson(),
    actionType = actionType,
    actionTarget = actionTarget,
    actionSubject = actionSubject,
    attachmentFiles = attachmentFiles,
    places = places,
    shoppingItems = shoppingItems.map { it.toJson() },
    builderScheme = builderScheme,
    uniqueId = uniqueId,
    isActive = isActive,
    isRemoved = isRemoved,
    isPinned = isPinned,
    eventCount = eventCount,
    version = sync.version,
    offlineOnly = offlineOnly
  )
}

private fun NotificationSettingsOverride.toJson(): NotificationSettingsOverrideJson =
  NotificationSettingsOverrideJson(
    color = color,
    vibrate = vibrate,
    vibrationPattern = vibrationPattern,
    repeatNotification = repeatNotification,
    volume = volume,
    soundUri = soundUri,
    quietHoursFrom = quietHoursFrom,
    quietHoursTo = quietHoursTo,
    activeHours = activeHours,
    delayMinutes = delayMinutes,
    priority = priority?.name,
    category = category?.name,
    bypassDoNotDisturb = bypassDoNotDisturb,
    wakeScreen = wakeScreen,
    lockScreenVisibility = lockScreenVisibility?.name,
    remindBefore = remindBefore
  )

private fun CalendarExportSettings.toJson(): CalendarExportSettingsJson = CalendarExportSettingsJson(
  calendarId = calendarId,
  duration = duration,
  allDay = allDay
)

private fun TaskExportSettings.toJson(): TaskExportSettingsJson = TaskExportSettingsJson(
  taskListId = taskListId
)

private fun LocationSettings.toJson(): LocationSettingsJson = LocationSettingsJson(
  isNotificationShown = isNotificationShown,
  isLocked = isLocked,
  hasDelayedReminder = hasDelayedReminder
)

private fun ShopItemV2.toJson(): ShopItemV2Json = ShopItemV2Json(
  uuId = uuId,
  summary = summary,
  isChecked = isChecked,
  isDeleted = isDeleted,
  createdAt = createdAt.format(jsonDateTimeFormatter)
)

private fun RecurrenceRule.toColumns(): Pair<String, String> = when (this) {
  is RecurrenceRule.Once -> "ONCE" to ""
  is RecurrenceRule.Countdown -> "COUNTDOWN" to recurrenceGson.toJson(this)
  is RecurrenceRule.Daily -> "DAILY" to recurrenceGson.toJson(this)
  is RecurrenceRule.Weekly -> "WEEKLY" to recurrenceGson.toJson(this)
  is RecurrenceRule.Monthly -> "MONTHLY" to recurrenceGson.toJson(this)
  is RecurrenceRule.RelativeMonthly -> "RELATIVE_MONTHLY" to recurrenceGson.toJson(this)
  is RecurrenceRule.Yearly -> "YEARLY" to recurrenceGson.toJson(this)
  is RecurrenceRule.LocationEnter -> "LOCATION_ENTER" to ""
  is RecurrenceRule.LocationExit -> "LOCATION_EXIT" to ""
  is RecurrenceRule.ICalendar -> "ICALENDAR" to recurrenceGson.toJson(this)
}

internal fun WorkflowRule.toJson(): WorkflowRuleJson {
  val (scopeType, scopeId) = scope.toWorkflowScopeColumns()
  val (triggerType, triggerPayload) = trigger.toColumns()
  val (actionType, actionPayload) = action.toColumns()
  return WorkflowRuleJson(
    uuId = uuId,
    title = title,
    templateId = templateId,
    scopeType = scopeType,
    scopeId = scopeId,
    triggerType = triggerType,
    triggerPayload = triggerPayload,
    conditionsPayload = conditions.toConditionsPayload(),
    actionType = actionType,
    actionPayload = actionPayload,
    isEnabled = isEnabled,
    createdAt = createdAt.format(jsonDateTimeFormatter),
    lastRunAt = lastRunAt?.format(jsonDateTimeFormatter),
    version = version
  )
}

internal fun WorkflowTemplate.toJson(): WorkflowTemplateJson {
  val (triggerType, triggerPayload) = trigger.toColumns()
  val (actionType, actionPayload) = action.toColumns()
  return WorkflowTemplateJson(
    id = id,
    title = title,
    description = description,
    category = category.name,
    supportedScopeTypes = supportedScopeTypes.map { it.name },
    triggerType = triggerType,
    triggerPayload = triggerPayload,
    actionType = actionType,
    actionPayload = actionPayload,
    isBuiltIn = isBuiltIn,
    useCount = useCount,
    createdAt = createdAt.format(jsonDateTimeFormatter),
    version = version
  )
}

private fun ReminderAction.toColumns(): Triple<String, String, String> = when (this) {
  is ReminderAction.None -> Triple("NONE", "", "")
  is ReminderAction.Call -> Triple("CALL", target, "")
  is ReminderAction.Sms -> Triple("SMS", target, subject)
  is ReminderAction.Link -> Triple("LINK", target, "")
  is ReminderAction.App -> Triple("APP", target, "")
  is ReminderAction.Email -> Triple("EMAIL", target, subject)
  is ReminderAction.Shopping -> Triple("SHOPPING", "", "")
}
