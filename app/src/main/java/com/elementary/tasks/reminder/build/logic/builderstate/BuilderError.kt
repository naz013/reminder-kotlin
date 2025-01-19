package com.elementary.tasks.reminder.build.logic.builderstate

import com.github.naz013.domain.reminder.BiType

sealed class BuilderError {
  data object Unknown : BuilderError()
  data object InvalidState : BuilderError()
  data class RequiresBiType(
    val value: BiTypeCollection
  ) : BuilderError()

  sealed class BiTypeCollection {
    data class Single(
      val value: BiType
    ) : BiTypeCollection()

    sealed class Multiple : BiTypeCollection() {
      abstract val biTypes: List<BiType>

      data class And(
        override val biTypes: List<BiType>
      ) : Multiple() {
        constructor(vararg biType: BiType) : this(biType.toList())
      }

      data class Or(
        override val biTypes: List<BiType>
      ) : Multiple() {
        constructor(vararg biType: BiType) : this(biType.toList())
      }
    }
  }
}
