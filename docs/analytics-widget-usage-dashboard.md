# Widget Usage Analytics — Pie Chart Dashboard

## Overview

The `appwidgets` module ships seven home-screen widget types (Calendar, Notes, Birthdays,
Combined Buttons, Google Tasks, Single Note, and the Glance-based "Active reminders"/Events
widget). This document covers the two Firebase events that let you build a GA4 pie chart of
"which widget type is used most," and how to build that dashboard.

Two events exist, answering two different questions:

| Event | Fires when | Answers |
|---|---|---|
| `widget_used` | The widget's config screen is saved (the OS auto-launches config on every add, and it re-fires if the user reopens config later) | Which widget type do people add/configure most? |
| `widget_interacted` | The user taps something on a widget already sitting on the home screen (a list item, an "add" button, calendar prev/next) | Which widget type do people actually *use* day to day? |

Both share the same `type` parameter and the same `Widget` enum values
(`events`, `birthdays`, `notes`, `calendar`, `combined`, `google_tasks`, `single_note`), so they
plug into the same kind of GA4 report — just filter by event name to pick which question you're
answering.

## What was implemented

| Piece | Location |
|---|---|
| `Event.WIDGET_INTERACTED` (`"widget_interacted"`) | [analytics/src/main/kotlin/com/github/naz013/analytics/AnalyticEvent.kt](../analytics/src/main/kotlin/com/github/naz013/analytics/AnalyticEvent.kt) |
| `WidgetInteractedEvent(widget: Widget)` | same file — reuses `Parameter.TYPE` and the existing `Widget` enum, same pattern as `WidgetUsedEvent` |
| `widget_used` (pre-existing, unchanged) | fired from each widget's Config ViewModel `onSaveClick`, e.g. [appwidgets/.../calendar/CalendarWidgetConfigViewModel.kt](../appwidgets/src/main/kotlin/com/github/naz013/appwidgets/calendar/CalendarWidgetConfigViewModel.kt) |
| `widget_interacted` — classic (`RemoteViews`) widgets | [appwidgets/.../AppWidgetActionActivity.kt](../appwidgets/src/main/kotlin/com/github/naz013/appwidgets/AppWidgetActionActivity.kt) reads a new `WIDGET_TYPE` intent extra and sends the event; each widget provider/factory stamps its own `Widget` value onto the click intent (e.g. `CalendarWidget.kt`, `NotesFactory.kt`, `BirthdaysFactory.kt`, `TasksFactory.kt`, `CombinedButtonsWidget.kt`, `SingleNoteWidget.kt`) |
| `widget_interacted` — Calendar prev/next | [appwidgets/.../calendar/CalendarNextReceiver.kt](../appwidgets/src/main/kotlin/com/github/naz013/appwidgets/calendar/CalendarNextReceiver.kt) and `CalendarPreviousReceiver.kt` send it directly, since month navigation is a `BroadcastReceiver`, not a routed activity |
| `widget_interacted` — Events (Glance) widget | [appwidgets/.../events/EventsGlanceAppWidget.kt](../appwidgets/src/main/kotlin/com/github/naz013/appwidgets/events/EventsGlanceAppWidget.kt) passes a `widgetTypeKey` through Glance's `actionParametersOf(...)` on the add-button and item-click actions, which land back on `AppWidgetActionActivity` the same way the classic widgets do |

Deliberately **not** instrumented: the settings-gear tap on any widget. That already opens the
config screen, and saving it fires `widget_used` — counting the gear tap itself as an
"interaction" would double up the "added/configured" signal under a different event name.

Resulting Firebase events: `widget_used` and `widget_interacted`, both with param
`type = <calendar|notes|birthdays|combined|google_tasks|single_note|events>`.

## Setting up the GA4 dashboard

### 1. Register a custom dimension for the `type` parameter

Both widget events carry their widget name in the `type` param — the same parameter name other
events in this app already use (`feature_used`, `preset_used`, etc.), so GA4 needs it registered
as a custom dimension once before it can be used in reports.

**Admin → Custom definitions → Create custom dimensions**
- Dimension name: `Type` (reuse the existing one if a `type`-scoped custom dimension already
  exists from another event — the parameter name is identical, one dimension covers all of them)
- Scope: **Event**
- Event parameter: `type`

Registration only applies to data collected after you create it, and can take a few hours to
start populating reports.

### 2. Build the "most added" pie chart

**Explore → Blank (free-form)**
- Dimension: the `Type` custom dimension from step 1
- Metric: `Event count`
- Filter: `Event name exactly matches widget_used`
- Visualization: **Pie chart**, Values = `Event count`, Breakdown = `Type`

This is a breakdown of which widget type gets added/reconfigured most often.

### 3. Build the "most actively used" pie chart

Duplicate the tab from step 2 (or start a new one) and change the filter to
`Event name exactly matches widget_interacted`. Same dimension, same metric, same pie chart
visualization — this one shows which widget type people actually tap into day to day, which is
usually the more meaningful "most used" number since it's driven by ongoing behavior rather than
a one-time setup action.

Put both pies as separate tabs in the same Exploration so they're easy to flip between and
compare — a widget that's added a lot but rarely interacted with (or vice versa) is itself a
useful signal.

### 4. Optional: pin to a persistent dashboard

GA4 Explorations aren't addable to the classic Reports "dashboard" surface directly. For a
persistent, always-visible pie chart (rather than something you have to open Explore to view),
connect **Looker Studio** to the GA4 property and rebuild the same two charts there using a
GA4 connector with the same `Event name` filter and `type` dimension.

### 5. Optional: BigQuery export

If you need combined breakdowns GA4's Explore UI can't express (e.g. "ratio of
`widget_interacted` to `widget_used` per type, per week" as a single trend), link the property to
**BigQuery export** (Admin → BigQuery Links) and query `event_name IN ('widget_used',
'widget_interacted')` directly. This also sidesteps GA4's data-thresholding on small user counts.

## Extending this to a new widget type

If a new widget type is added to `appwidgets` later:
1. Add the new value to the `Widget` enum in `AnalyticEvent.kt`.
2. Fire `WidgetUsedEvent(Widget.YOUR_WIDGET)` from its config screen's save action (mirror any
   existing `*ConfigViewModel`).
3. Fire `WidgetInteractedEvent(Widget.YOUR_WIDGET)` from its real click paths — if it's a classic
   `RemoteViews` widget routing through `AppWidgetActionActivity`, just stamp
   `AppWidgetActionActivity.WIDGET_TYPE` onto its click intents like the existing providers do;
   no changes are needed in `AppWidgetActionActivity` itself.
4. No GA4-side changes are needed — `type` is already a registered custom dimension, so the new
   widget's data shows up in both pie charts automatically once you have data with its `type`
   value.
