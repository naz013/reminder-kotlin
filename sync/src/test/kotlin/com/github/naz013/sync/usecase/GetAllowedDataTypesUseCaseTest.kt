package com.github.naz013.sync.usecase

import com.github.naz013.sync.DataType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAllowedDataTypesUseCaseTest {

  private lateinit var getAllowedDataTypesUseCase: GetAllowedDataTypesUseCase

  @Before
  fun setUp() {
    getAllowedDataTypesUseCase = GetAllowedDataTypesUseCase()
  }

  @Test
  fun `test allowed data types`() {
    val allowedDataTypes = getAllowedDataTypesUseCase()
    assertTrue(allowedDataTypes.isNotEmpty())
    // Add more specific assertions based on expected data types
  }

  @Test
  fun `invoke do not returns the legacy data types`(){
    val allowedDataTypes = getAllowedDataTypesUseCase()
    assertFalse(allowedDataTypes.contains(DataType.NotesV2))
  }
}
