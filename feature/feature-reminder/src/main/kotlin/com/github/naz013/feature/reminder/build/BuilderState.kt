package com.github.naz013.feature.reminder.build

sealed class BuilderState

data object EmptyState : BuilderState()

data object ReadyState : BuilderState()

data object ErrorState : BuilderState()
