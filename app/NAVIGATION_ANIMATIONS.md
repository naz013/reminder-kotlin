# Navigation Animations

This document describes the navigation animation system used in the Reminder Kotlin app for natural-feeling transitions.

## Overview

The app uses different animation strategies based on the type of navigation:

1. **Bottom Navigation (Lateral)** - Fade animations
2. **In-Depth Navigation (Hierarchical)** - Slide from right animations
3. **Modal Navigation** - Slide from bottom animations

## Fragment Hierarchy

### BaseTopFragment
Fragments extending `BaseTopFragment` are attached to the BottomNavigationView and represent top-level destinations:
- Home (Events)
- Notes
- Calendar
- Tasks

These fragments use **fade animations** when switching between them, providing a smooth lateral navigation experience.

### BaseNavigationFragment
All other fragments extend `BaseNavigationFragment` directly or through other base classes. These represent in-depth navigation and use **slide-from-right animations** for a natural hierarchical feel.

## Animation Files

### Fade Animations (Bottom Navigation)
- `fragment_fade_in.xml` - 250ms fade in with fast_out_slow_in interpolator
- `fragment_fade_out.xml` - 250ms fade out with fast_out_slow_in interpolator

### Slide Animations (In-Depth Navigation)
- `nav_default_enter_anim.xml` - Slide from right (100% to 0%) with fade in
- `nav_default_exit_anim.xml` - Slide to left (-25%) with fade out
- `nav_default_pop_enter_anim.xml` - Slide from left (-25% to 0%) with fade in
- `nav_default_pop_exit_anim.xml` - Slide to right (0% to 100%) with fade out

### Modal Animations (Bottom Sheet Style)
- `fragment_slide_top.xml` - Slide up from bottom with fade in
- `fragment_slide_bottom.xml` - Slide down to bottom with delayed fade out
- `fragment_wait.xml` - Static animation for underlying fragments

## Implementation Details

### Bottom Navigation Setup
The `BottomNavActivity` configures bottom navigation with fade animations in `setupBottomNavigationAnimations()`:

```kotlin
private fun setupBottomNavigationAnimations() {
  binding.bottomNavigation.setOnItemSelectedListener { item ->
    val navOptions = NavOptions.Builder()
      .setEnterAnim(R.anim.fragment_fade_in)
      .setExitAnim(R.anim.fragment_fade_out)
      .setPopEnterAnim(R.anim.fragment_fade_in)
      .setPopExitAnim(R.anim.fragment_fade_out)
      .setLaunchSingleTop(true)
      .setPopUpTo(R.id.actionHome, false, true)
      .build()

    navController.navigate(targetDestination, null, navOptions)
    true
  }
}
```

### Navigation Graph
The `home_nav.xml` navigation graph defines animations for each action. Actions from top-level fragments to settings and other screens use modal animations, while in-depth navigation uses slide animations.

### NavigationAnimations Helper
The `NavigationAnimations` object provides pre-configured NavOptions for different animation types:
- `bottomNavOptions()` - Fade animations for lateral navigation
- `inDepthNavOptions()` - Slide animations for hierarchical navigation
- `modalNavOptions()` - Bottom slide animations for modal screens

## Design Principles

### Material Design Guidelines
All animations follow Material Design motion principles:
- **Duration**: 250-350ms for optimal perception
- **Interpolators**: `fast_out_slow_in` for natural deceleration
- **Opacity**: Combined with motion for depth perception

### Navigation Patterns
1. **Lateral Navigation** (same hierarchy level): Use fade transitions
   - Example: Switching between Home, Notes, Calendar, Tasks

2. **Forward Navigation** (entering detail): Slide from right
   - Example: Opening a reminder detail from the list

3. **Backward Navigation** (returning): Reverse of forward
   - Example: Pressing back to return to the list

4. **Modal Presentation** (temporary screen): Slide from bottom
   - Example: Opening settings, dialogs

## Testing Animations

To test the animations:
1. Run the app and switch between bottom navigation tabs - should see smooth fade transitions
2. Navigate into a detail screen - should see slide-from-right animation
3. Press back - should see reverse slide animation
4. Open settings from home - should see slide-up-from-bottom animation

## Customization

To customize animations for specific navigation actions, update the corresponding action in `home_nav.xml`:

```xml
<action
    android:id="@+id/action_to_detail"
    app:destination="@id/detailFragment"
    app:enterAnim="@anim/custom_enter"
    app:exitAnim="@anim/custom_exit"
    app:popEnterAnim="@anim/custom_pop_enter"
    app:popExitAnim="@anim/custom_pop_exit" />
```

