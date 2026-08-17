package com.github.naz013.feature.reminder

interface UiAdapter<Data, Result> {
  fun create(data: Data): Result
}
