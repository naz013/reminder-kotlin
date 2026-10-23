package com.github.naz013.feature.birthday.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.github.naz013.common.TextProvider
import com.github.naz013.feature.common.viewmodel.PendingDeleteCoordinator
import com.github.naz013.feature.common.viewmodel.PendingEntity
import com.github.naz013.ui.common.R
import com.github.naz013.logic.birthday.DeleteBirthdayUseCase
import com.github.naz013.ui.birthday.UiBirthdayPreviewAdapter
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.ui.tag.TagChipStateAdapter
import kotlinx.coroutines.flow.combine

internal class PreviewBirthdayViewModel(
  private val id: String,
  private val birthdayRepository: BirthdayRepository,
  private val dispatcherProvider: DispatcherProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiBirthdayPreviewAdapter: UiBirthdayPreviewAdapter,
  private val deleteBirthdayUseCase: DeleteBirthdayUseCase,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val tagChipStateAdapter: TagChipStateAdapter,
  private val pendingDeleteCoordinator: PendingDeleteCoordinator,
  private val textProvider: TextProvider,
) : ViewModel() {

  // Plain ViewModel-lifetime field, not part of the reactive pipeline below - the confetti
  // animation must play at most once per ViewModel instance regardless of how many times the
  // underlying birthday re-emits (edited elsewhere) or how many times `state` is freshly
  // re-subscribed (e.g. the screen re-entering the foreground).
  private var canShowAnimation = true

  // Fully derived from the Birthday/Tag tables via Room's Flow invalidation - no manual
  // load()/refresh() call anywhere in this ViewModel. Editing this birthday from another screen
  // (or the Edit screen popping back to this same preview pane in two-pane mode) is reflected
  // here automatically as soon as it's saved.
  val state = combine(
    birthdayRepository.observeById(id),
    tagAssignmentRepository.observeTagsForItem(id, TaggedItemType.BIRTHDAY),
    pendingDeleteCoordinator.pendingIds(PendingEntity.BIRTHDAY),
  ) { birthday, tags, pendingIds ->
    buildState(birthday.takeUnless { id in pendingIds }, tags)
  }.stateInWhileSubscribed(PreviewBirthdayState())

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  fun onSmsClicked() {
    state.value.birthday?.number?.also {
      event.emit(ViewModelEvent.SendSms(it))
    }
  }

  fun onCallClicked() {
    state.value.birthday?.number?.also {
      event.emit(ViewModelEvent.MakeCall(it))
    }
  }

  fun onDeleteClick() {
    if (id in pendingDeleteCoordinator.pendingIds(PendingEntity.BIRTHDAY).value) return
    pendingDeleteCoordinator.markPending(
      type = PendingEntity.BIRTHDAY,
      id = id,
      message = textProvider.getText(R.string.birthday_deleted),
    ) {
      deleteBirthdayUseCase(id)
    }
    event.emit(ViewModelEvent.Deleted(id))
  }

  private suspend fun buildState(
    birthday: Birthday?,
    tags: List<Tag>,
  ): PreviewBirthdayState {
    val tagChips = tags.map { tagChipStateAdapter(it) }
    if (birthday == null) {
      return PreviewBirthdayState(tags = tagChips)
    }

    analyticsEventSender.send(FeatureUsedEvent(Feature.BIRTHDAY_PREVIEW))

    val uiBirthday = uiBirthdayPreviewAdapter.convert(birthday)
    val shouldPlayConfetti = uiBirthday.hasBirthdayToday && canShowAnimation
    canShowAnimation = false

    return PreviewBirthdayState(
      birthday = uiBirthday,
      playConfetti = shouldPlayConfetti,
      canShowAnimation = false,
      tags = tagChips,
    )
  }

  sealed interface ViewModelEvent {
    data class Deleted(val id: String) : ViewModelEvent

    data class SendSms(val number: String) : ViewModelEvent

    data class MakeCall(val number: String) : ViewModelEvent
  }
}
