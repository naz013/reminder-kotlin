package com.github.naz013.tags

import com.github.naz013.tags.compose.TagEditViewModel
import com.github.naz013.tags.compose.TagsViewModel
import com.github.naz013.tags.details.TagDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val tagsModule = module {
  viewModelOf(::TagsViewModel)
  viewModel { (id: String?) -> TagEditViewModel(id, get(), get(), get(), get(), get()) }
  viewModel { (id: String) ->
    TagDetailsViewModel(id, get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
  }
}
