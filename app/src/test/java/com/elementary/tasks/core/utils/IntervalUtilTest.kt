package com.elementary.tasks.core.utils

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class IntervalUtilTest {

    private lateinit var data: List<Int>

    @Before
    fun setup() {
        data = IntervalUtil.getWeekRepeat(mon = true, tue = false, wed = false, thu = false, fri = false, sat = false, sun = false)
    }

    @Test
    fun getWeekRepeat_shouldReturnSevenItems() {
        assertEquals(7, data.size)
    }

    @Test
    fun getWeekRepeat_shouldHasMondayChecked() {
        assertEquals(1, data[1])
    }

    @Test
    fun isWeekday_shouldReturnTrue() {
        assertEquals(true, IntervalUtil.isWeekday(data))
    }

    @After
    fun tearDown() {
    }
}