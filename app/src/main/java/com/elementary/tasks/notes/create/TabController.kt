package com.elementary.tasks.notes.create

class TabController(
  private val listener: Listener
) {

  private var prevTab: Tab = Tab.NONE
  private var tabVisible = false

  fun hide() {
    if (tabVisible) {
      listener.onHide()
    }
    tabVisible = false
  }

  fun isTabVisible(): Boolean = tabVisible

  fun onTabClick(tab: Tab) {
    if (prevTab == tab) {
      if (tabVisible) {
        listener.onHide()
      } else {
        listener.onShow()
      }
      tabVisible = !tabVisible
    } else {
      if (tabVisible) {
        listener.onHide()
        listener.onTabSelected(tab)
        listener.onShow()
      } else {
        listener.onTabSelected(tab)
        listener.onShow()
      }
      tabVisible = true
    }
    prevTab = tab
  }

  enum class Tab {
    COLOR,
    FONT,
    NONE
  }

  interface Listener {
    fun onTabSelected(tab: Tab)
    fun onShow()
    fun onHide()
  }
}
