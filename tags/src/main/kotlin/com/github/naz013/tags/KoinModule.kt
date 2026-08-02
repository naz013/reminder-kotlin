package com.github.naz013.tags

import com.github.naz013.tags.compose.TagEditViewModel
import com.github.naz013.tags.compose.TagsViewModel
import com.github.naz013.tags.db.TagsDb
import com.github.naz013.tags.impl.TagAssignmentRepositoryImpl
import com.github.naz013.tags.impl.TagRepositoryImpl
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val tagsModule = module {
  single { TagsDb.getInstance(get()) }

  factory { TagRepositoryImpl(get<TagsDb>().tagDao()) as TagRepository }
  factory { TagAssignmentRepositoryImpl(get<TagsDb>().tagAssignmentDao()) as TagAssignmentRepository }

  viewModelOf(::TagsViewModel)
  viewModel { (id: String?) -> TagEditViewModel(id, get(), get(), get()) }
}
