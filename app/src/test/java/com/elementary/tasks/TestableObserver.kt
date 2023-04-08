package com.elementary.tasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.junit.Assert.assertEquals
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/* Copyright 2019 Google LLC.
   SPDX-License-Identifier: Apache-2.0 */
fun <T> LiveData<T>.getOrAwaitValue(
  time: Long = 2,
  timeUnit: TimeUnit = TimeUnit.SECONDS
): T? {
  var data: T? = null
  val latch = CountDownLatch(1)
  val observer = object : Observer<T> {
    override fun onChanged(o: T) {
      data = o
      latch.countDown()
      this@getOrAwaitValue.removeObserver(this)
    }
  }

  this.observeForever(observer)

  // Don't wait indefinitely if the LiveData is not set.
  latch.await(time, timeUnit)

  @Suppress("UNCHECKED_CAST")
  return data as T
}

class TestableObserver<T> : Observer<T> {

  private val history: MutableList<T> = mutableListOf()

  override fun onChanged(value: T) {
    history.add(value)
  }

  fun numberOfEmissions(): Int {
    return history.size
  }

  fun assertNoEmission() {
    assertEquals(0, history.count())
  }

  fun assertOnlyEmission(t: T) {
    assertEquals(1, history.count())
    assertEquals(t, history.first())
  }

  fun assertAllEmitted(values: List<T>) {
    assertEquals(values.count(), history.count())
    history.forEachIndexed { index, t ->
      assertEquals(values[index], t)
    }
  }
}
