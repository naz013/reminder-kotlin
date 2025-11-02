package com.elementary.tasks.navigation.topfragment

import androidx.viewbinding.ViewBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment

/**
 * Base fragment for top-level destinations attached to the BottomNavigationView.
 *
 * This fragment serves as the parent class for all main navigation destinations that are
 * directly accessible from the bottom navigation bar (e.g., Home, Notes, Calendar, Tasks).
 * These fragments represent the primary navigation hierarchy and use fade animations
 * when switching between them to provide a smooth, natural user experience.
 *
 * For in-depth navigation (child fragments not attached to bottom navigation), use
 * [BaseNavigationFragment] or other appropriate base classes instead.
 *
 * @param B The ViewBinding type for this fragment
 */
abstract class BaseTopFragment<B : ViewBinding> : BaseNavigationFragment<B>()
