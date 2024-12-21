package com.elementary.tasks.notes.create

import android.view.View
import com.github.naz013.feature.common.android.visibleGone

class TabController(
  private val tabs: List<TabView>,
  private val listener: Listener
) {

  private var currentTab: Tab = Tab.NONE
  private var tabVisible = false

  fun hide() {
    if (tabVisible) {
      listener.onHide()
      setSelectorVisible(false, currentTab)
    }
    currentTab = Tab.NONE
    tabVisible = false
  }

  fun isTabVisible(): Boolean = tabVisible

  fun onTabClick(tab: Tab) {
    if (currentTab == tab) {
      if (tabVisible) {
        listener.onHide()
      } else {
        listener.onShow()
      }
      setSelectorVisible(!tabVisible, currentTab)
      tabVisible = !tabVisible
    } else {
      if (tabVisible) {
        listener.onHide()
        setSelectorVisible(false, currentTab)
      }

      listener.onTabSelected(tab)
      listener.onShow()
      setSelectorVisible(true, tab)

      tabVisible = true
    }
    currentTab = tab
  }

  private fun setSelectorVisible(visible: Boolean, tab: Tab) {
    tabs.firstOrNull { it.tab == tab }?.run {
      selectorView.visibleGone(visible)
    }
  }

  data class TabView(
    val tab: Tab,
    val selectorView: View
  )

  enum class Tab {
    COLOR,
    FONT,
    REMINDER,
    NONE
  }

  interface Listener {
    fun onTabSelected(tab: Tab)
    fun onShow()
    fun onHide()
  }
}
