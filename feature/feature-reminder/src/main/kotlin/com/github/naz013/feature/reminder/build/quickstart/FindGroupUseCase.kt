package com.github.naz013.feature.reminder.build.quickstart

import com.github.naz013.ui.group.UiGroupListAdapter
import com.github.naz013.ui.group.UiGroupList
import com.github.naz013.repository.GroupV2Repository

class FindGroupUseCase(
  private val groupV2Repository: GroupV2Repository,
  private val uiGroupListAdapter: UiGroupListAdapter,
) {

  suspend operator fun invoke(uuid: String): UiGroupList? {
    return groupV2Repository.getById(uuid)
      ?.let { uiGroupListAdapter.convert(it) }
  }
}
