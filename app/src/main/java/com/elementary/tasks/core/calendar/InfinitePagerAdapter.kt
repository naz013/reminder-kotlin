package com.elementary.tasks.core.calendar

import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class InfinitePagerAdapter(private val adapter: PagerAdapter) : PagerAdapter() {

  private val realCount: Int
    get() = adapter.count

  override fun getCount(): Int {
    return Integer.MAX_VALUE
  }

  override fun instantiateItem(container: ViewGroup, position: Int): Any {
    val virtualPosition = position % realCount
    return adapter.instantiateItem(container, virtualPosition)
  }

  override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    val virtualPosition = position % realCount
    adapter.destroyItem(container, virtualPosition, `object`)
  }

  override fun finishUpdate(container: ViewGroup) {
    adapter.finishUpdate(container)
  }

  override fun isViewFromObject(view: View, `object`: Any): Boolean {
    return adapter.isViewFromObject(view, `object`)
  }

  override fun restoreState(bundle: Parcelable?, classLoader: ClassLoader?) {
    adapter.restoreState(bundle, classLoader)
  }

  override fun saveState(): Parcelable? {
    return adapter.saveState()
  }

  override fun startUpdate(container: ViewGroup) {
    adapter.startUpdate(container)
  }
}
