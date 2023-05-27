package com.elementary.tasks.reminder.create.fragments.recur

import com.elementary.tasks.core.utils.datetime.recurrence.RecurParamType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BuilderParamLogicTest {

  private lateinit var builderParamLogic: BuilderParamLogic
  private val emptyExpected = emptyList<BuilderParam<*>>()

  @Before
  fun setUp() {
    builderParamLogic = BuilderParamLogic()
  }

  @Test
  fun testAddParam_whenAllIsEmpty() {
    builderParamLogic.setAllParams(emptyList())

    builderParamLogic.addOrUpdateParam(BuilderParam(RecurParamType.INTERVAL, 0))

    assertEquals(emptyExpected, builderParamLogic.getUsed())
    assertEquals(emptyExpected, builderParamLogic.getAvailable())
  }

  @Test
  fun testAddParams_whenAllIsEmpty() {
    builderParamLogic.setAllParams(emptyList())

    builderParamLogic.addOrUpdateParams(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 0)
      )
    )

    assertEquals(emptyExpected, builderParamLogic.getUsed())
    assertEquals(emptyExpected, builderParamLogic.getAvailable())
  }

  @Test
  fun testRemoveParam_whenAllIsEmpty() {
    builderParamLogic.setAllParams(emptyList())

    builderParamLogic.removeParam(BuilderParam(RecurParamType.INTERVAL, 0))

    assertEquals(emptyExpected, builderParamLogic.getUsed())
    assertEquals(emptyExpected, builderParamLogic.getAvailable())
  }

  @Test
  fun testAddParam_whenOnlyOneElementLeft() {
    builderParamLogic.setAllParams(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 0)
      )
    )

    builderParamLogic.addOrUpdateParam(BuilderParam(RecurParamType.INTERVAL, 5))

    assertEquals(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 5)
      ),
      builderParamLogic.getUsed()
    )
    assertEquals(emptyExpected, builderParamLogic.getAvailable())
  }

  @Test
  fun testAddParams_whenOnlyOneElementLeft() {
    builderParamLogic.setAllParams(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 0)
      )
    )

    builderParamLogic.addOrUpdateParams(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 5)
      )
    )

    assertEquals(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 5)
      ),
      builderParamLogic.getUsed()
    )
    assertEquals(emptyExpected, builderParamLogic.getAvailable())
  }

  @Test
  fun testRemoveParam_whenOnlyOneElementLeft() {
    builderParamLogic.setAllParams(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 0)
      )
    )

    builderParamLogic.addOrUpdateParam(BuilderParam(RecurParamType.INTERVAL, 5))

    assertEquals(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 5)
      ),
      builderParamLogic.getUsed()
    )
    assertEquals(emptyExpected, builderParamLogic.getAvailable())

    builderParamLogic.removeParam(BuilderParam(RecurParamType.INTERVAL, 10))

    assertEquals(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 5)
      ),
      builderParamLogic.getAvailable()
    )
    assertEquals(emptyExpected, builderParamLogic.getUsed())
  }

  @Test
  fun testAddParam_whenOnlyTwoElementsLeft() {
    builderParamLogic.setAllParams(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 0),
        BuilderParam(RecurParamType.COUNT, 0)
      )
    )

    builderParamLogic.addOrUpdateParam(BuilderParam(RecurParamType.INTERVAL, 5))

    assertEquals(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 5)
      ),
      builderParamLogic.getUsed()
    )
    assertEquals(
      listOf(
        BuilderParam(RecurParamType.COUNT, 0)
      ),
      builderParamLogic.getAvailable()
    )
  }

  @Test
  fun testAddParams_whenOnlyTwoElementsLeft() {
    builderParamLogic.setAllParams(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 0),
        BuilderParam(RecurParamType.COUNT, 0)
      )
    )

    builderParamLogic.addOrUpdateParams(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 5)
      )
    )

    assertEquals(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 5)
      ),
      builderParamLogic.getUsed()
    )
    assertEquals(
      listOf(
        BuilderParam(RecurParamType.COUNT, 0)
      ),
      builderParamLogic.getAvailable()
    )
  }

  @Test
  fun testRemoveParam_whenOnlyTwoElementsLeft() {
    builderParamLogic.setAllParams(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 0),
        BuilderParam(RecurParamType.COUNT, 0)
      )
    )

    builderParamLogic.addOrUpdateParam(BuilderParam(RecurParamType.INTERVAL, 5))

    assertEquals(
      listOf(
        BuilderParam(RecurParamType.INTERVAL, 5)
      ),
      builderParamLogic.getUsed()
    )
    assertEquals(
      listOf(
        BuilderParam(RecurParamType.COUNT, 0)
      ),
      builderParamLogic.getAvailable()
    )

    builderParamLogic.removeParam(BuilderParam(RecurParamType.INTERVAL, 10))

    assertEquals(
      emptyExpected,
      builderParamLogic.getUsed()
    )
    val expected = listOf(
      BuilderParam(RecurParamType.INTERVAL, 0),
      BuilderParam(RecurParamType.COUNT, 0)
    )
    val result = builderParamLogic.getAvailable()
    assertEquals(expected.size, result.size)
    assertTrue(result.containsAll(expected))
  }
}
