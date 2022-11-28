package com.elementary.tasks.core.data.adapter

interface UiAdapter<Data, Result> {
  fun create(data: Data): Result
}