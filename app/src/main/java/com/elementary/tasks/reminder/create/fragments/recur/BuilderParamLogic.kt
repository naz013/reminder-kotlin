package com.elementary.tasks.reminder.create.fragments.recur

import com.elementary.tasks.core.utils.datetime.recurrence.RecurParamType

class BuilderParamLogic {

  private var allParams: Set<BuilderParam<*>> = emptySet()
  private val usedParams: MutableSet<BuilderParam<*>> = HashSet()

  fun setAllParams(allParams: List<BuilderParam<*>>) {
    this.allParams = allParams.toHashSet()
  }

  fun addOrUpdateParam(builderParam: BuilderParam<*>) {
    if (allParams.isEmpty()) return
    val index = usedParams.indexOf(builderParam)
    if (index != -1) {
      usedParams.remove(builderParam)
    }
    usedParams.add(builderParam)
  }

  fun addOrUpdateParams(params: List<BuilderParam<*>>) {
    if (allParams.isEmpty()) return
    params.forEach { builderParam ->
      val index = usedParams.indexOf(builderParam)
      if (index != -1) {
        usedParams.remove(builderParam)
      }
      usedParams.add(builderParam)
    }
  }

  fun removeParam(builderParam: BuilderParam<*>) {
    if (allParams.isEmpty()) return
    if (builderParam.recurParamType == RecurParamType.COUNT) {
      return
    }
    usedParams.remove(builderParam)
  }

  fun getAvailable(): List<BuilderParam<*>> {
    return allParams.toList() - usedParams
  }

  fun getUsed(): List<BuilderParam<*>> {
    return usedParams.toList()
  }

  fun clearUsed() {
    usedParams.clear()
  }
}
