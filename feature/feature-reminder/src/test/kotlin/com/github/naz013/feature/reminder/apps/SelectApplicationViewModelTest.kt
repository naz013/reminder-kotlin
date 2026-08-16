package com.github.naz013.feature.reminder.apps

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.common.PackageManagerWrapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SelectApplicationViewModelTest : BaseTest() {
  private val packageManagerWrapper = mockk<PackageManagerWrapper>()
  private val packageManager = mockk<PackageManager>(relaxed = true)

  private lateinit var viewModel: SelectApplicationViewModel

  private fun appInfo(
    packageName: String,
    label: String,
  ): ApplicationInfo {
    val info = mockk<ApplicationInfo>(relaxed = true)
    info.packageName = packageName
    every { info.loadLabel(packageManager) } returns label
    every { info.loadIcon(packageManager) } returns mockk<Drawable>(relaxed = true)
    return info
  }

  @Before
  override fun setUp() {
    super.setUp()
    // toBitmap() is a real androidx-core extension that, under the JVM unit-test stub of
    // android.jar, ends up calling Bitmap.createBitmap/Canvas with values that trip a
    // "must not be null" NPE - mock the extension itself rather than fighting the stub chain.
    mockkStatic("androidx.core.graphics.drawable.DrawableKt")
    every { any<Drawable>().toBitmap() } returns mockk<Bitmap>(relaxed = true)

    every { packageManagerWrapper.packageManager } returns packageManager
    every { packageManagerWrapper.getInstalledApplications() } returns emptyList()

    viewModel =
      SelectApplicationViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        packageManagerWrapper = packageManagerWrapper,
      )
  }

  @After
  override fun tearDown() {
    super.tearDown()
    unmockkStatic("androidx.core.graphics.drawable.DrawableKt")
  }

  @Test
  fun `loads empty state when no applications are installed`() {
    assertEquals(AppListState.Empty, viewModel.state.value.listState)
  }

  @Test
  fun `loads installed applications sorted by name`() {
    every { packageManagerWrapper.getInstalledApplications() } returns
      listOf(
        appInfo("com.b.app", "Zebra"),
        appInfo("com.a.app", "Apple"),
      )

    viewModel =
      SelectApplicationViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        packageManagerWrapper = packageManagerWrapper,
      )

    val ready = viewModel.state.value.listState as AppListState.Ready
    assertEquals(listOf("Apple", "Zebra"), ready.apps.map { it.name })
  }

  @Test
  fun `onSearchQueryChange filters the list by name`() {
    every { packageManagerWrapper.getInstalledApplications() } returns
      listOf(
        appInfo("com.b.app", "Zebra"),
        appInfo("com.a.app", "Apple"),
      )
    viewModel =
      SelectApplicationViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        packageManagerWrapper = packageManagerWrapper,
      )

    viewModel.onSearchQueryChange("zeb")

    assertEquals("zeb", viewModel.state.value.searchQuery)
    val ready = viewModel.state.value.listState as AppListState.Ready
    assertEquals(listOf("Zebra"), ready.apps.map { it.name })
  }

  @Test
  fun `onSearchQueryChange shows empty state when nothing matches`() {
    every { packageManagerWrapper.getInstalledApplications() } returns
      listOf(appInfo("com.a.app", "Apple"))
    viewModel =
      SelectApplicationViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        packageManagerWrapper = packageManagerWrapper,
      )

    viewModel.onSearchQueryChange("nothing matches this")

    assertEquals(AppListState.Empty, viewModel.state.value.listState)
  }

  @Test
  fun `onSearchQueryChange with blank query restores the full list`() {
    every { packageManagerWrapper.getInstalledApplications() } returns
      listOf(
        appInfo("com.b.app", "Zebra"),
        appInfo("com.a.app", "Apple"),
      )
    viewModel =
      SelectApplicationViewModel(
        dispatcherProvider = mockDispatcherProvider(),
        packageManagerWrapper = packageManagerWrapper,
      )

    viewModel.onSearchQueryChange("apple")
    viewModel.onSearchQueryChange("  ")

    val ready = viewModel.state.value.listState as AppListState.Ready
    assertEquals(listOf("Apple", "Zebra"), ready.apps.map { it.name })
  }

  @Test
  fun `onAppClick emits AppSelected event with the package name`() {
    val app = UiApplicationList(name = "Apple", packageName = "com.a.app", icon = null)

    viewModel.onAppClick(app)

    val event = viewModel.event.value?.peekContent()
    assertEquals(SelectApplicationViewModel.ViewModelEvent.AppSelected("com.a.app"), event)
  }
}
