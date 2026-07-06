# Feature Adoption Analytics — Google Tasks

## Overview

`FeatureUsedEvent` (Firebase event `feature_used`) tells you how often a feature is used in total,
but it fires on **every** use, so it can't answer "how many users have ever tried this feature" or
"is adoption of Google Tasks growing." This document covers the new `FeatureAdoptedEvent` type,
added specifically to answer that question for Google Tasks, and how to build a Google Analytics
(GA4) dashboard around it.

The event type is generic (it reuses the existing `Feature` enum), so the same pattern can be
reused for other features later — just fire `FeatureAdoptedEvent(Feature.X)` once per user, guarded
by a persisted flag, the first time they complete that feature's core action.

## What was implemented

| Piece | Location |
|---|---|
| `Event.FEATURE_ADOPTED` (`"feature_adopted"`) | [analytics/src/main/kotlin/com/github/naz013/analytics/AnalyticEvent.kt](../analytics/src/main/kotlin/com/github/naz013/analytics/AnalyticEvent.kt) |
| `FeatureAdoptedEvent(feature: Feature)` | same file — reuses `Parameter.TYPE`, same as `FeatureUsedEvent` |
| `Prefs.hasAdoptedGoogleTasks` (persisted, one-time flag) | [app/src/main/java/com/elementary/tasks/core/utils/params/Prefs.kt](../app/src/main/java/com/elementary/tasks/core/utils/params/Prefs.kt) |
| Fired from the "create new Google Task" path | [app/src/main/java/com/elementary/tasks/googletasks/task/EditGoogleTaskViewModel.kt](../app/src/main/java/com/elementary/tasks/googletasks/task/EditGoogleTaskViewModel.kt) `save()` |

The adoption moment was deliberately chosen as **the first Google Task the user creates**, not the
OAuth login step. Logging in without ever creating a task isn't real adoption — it's the login flow
being started, possibly abandoned. `EditGoogleTaskViewModel.save()` already distinguishes "new task"
from "edit existing task" (the `editedTask == null` branch), so the adoption check rides on that
existing branch with no risk of re-firing on edits.

```kotlin
} else {
  analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GOOGLE_TASK))     // fires every time
  if (!prefs.hasAdoptedGoogleTasks) {
    prefs.hasAdoptedGoogleTasks = true
    analyticsEventSender.send(FeatureAdoptedEvent(Feature.CREATE_GOOGLE_TASK)) // fires once ever
  }
  newGoogleTask(update(GoogleTask(), summary, note, reminder), reminder)
}
```

Resulting Firebase event: `feature_adopted` with param `type = create_google_task`.

Note: like every other analytics event in this app, this respects the user's analytics opt-out
(`AnalyticsStateProvider.analyticsEnabled`) and `FirebaseAnalytics.setAnalyticsCollectionEnabled`.
The local `hasAdoptedGoogleTasks` flag still gets set even if the event isn't sent (analytics
disabled), so re-enabling analytics later won't cause a false "adopted today" event for a
long-time user.

## Setting up the GA4 dashboard

### 1. Register a custom dimension for the `type` parameter

`feature_adopted` carries its feature name in the `type` param, same as `feature_used`. GA4 won't
let you build reports/explorations on a parameter until it's registered as a custom dimension.

**Admin → Custom definitions → Create custom dimensions**
- Dimension name: `Feature` (or reuse the one you already created for `feature_used`, if you did — the parameter name is identical, so one custom dimension covers both events)
- Scope: **Event**
- Event parameter: `type`

Registration can take a few hours to start populating reports for new events.

### 2. Mark `feature_adopted` as a key event

**Admin → Events**, find `feature_adopted` (it will appear once the app has sent it at least once),
toggle **Mark as key event**. This surfaces it in the default Reports UI, lets you use it as a
conversion in Explorations, and makes it available for Audiences without extra configuration.

### 3. Build the adoption dashboard

Use **Explore → Blank** (free-form) for each of the following, or assemble them onto one canvas.

**a) Daily/weekly new adopters (trend line)**
- Dimension: `Date` (or `Week`)
- Metric: `Event count`, filter `Event name = feature_adopted` AND `Feature (type) = create_google_task`
- Chart type: line. This is your adoption trend — since the event fires once per user ever, event
  count here is equivalent to unique new adopters per period.

**b) Cumulative adopters (running total)**
- GA4's Explore UI doesn't do running totals natively. Two options:
  - Quickest: export the daily series from (a) and compute a running sum in Sheets/Looker Studio.
  - Better for a live dashboard: connect **Looker Studio** to the GA4 property, use the same
    `feature_adopted` filter, and add a calculated field with a cumulative aggregation
    (`RUNNING_SUM` over `Event count` ordered by date).

**c) Adoption rate (% of active users who have adopted)**
- Explore → Free form
- Metric 1: `Event count` filtered to `feature_adopted` / `type = create_google_task`
- Metric 2: `Total users` (unfiltered, or filtered to the same date range)
- Compute Metric 1 ÷ Metric 2 as a calculated metric, or do it in Looker Studio. This tells you what
  fraction of your user base has ever adopted Google Tasks, not just how many raw events fired.

**d) Adoption funnel**
- **Explore → Funnel exploration**
- Steps:
  1. `screen_opened` where `screen = google_tasks_list` (user viewed the Google Tasks screen)
  2. `feature_used` where `type = login_google_task` (user linked their Google account)
  3. `feature_adopted` where `type = create_google_task` (user actually adopted the feature)
- This shows exactly where people drop off: browsing → linking → adopting. If most drop-off is
  between steps 2 and 3, the login flow works fine but the task-creation UI doesn't hook people;
  if it's between 1 and 2, the login flow itself is the blocker.

**e) Adoption cohort / adoption curve since install**
- **Explore → Cohort exploration**
- Cohort inclusion event: `first_open`
- Return event/condition: `feature_adopted` where `type = create_google_task`
- Granularity: Weekly, 8–12 weeks
- This answers "of users who installed the app in week N, what % had adopted Google Tasks by
  week N+1, N+2, ... N+8" — the classic feature-adoption curve, and it's the best view for
  judging whether onboarding/discovery changes actually move adoption.

**f) Adopters vs. non-adopters retention comparison**
- **Admin → Audiences → New audience**: define "Google Tasks Adopters" = users who triggered
  `feature_adopted` with `type = create_google_task`.
- Use this audience as a comparison in the standard **Retention** report (Reports → Retention,
  add a comparison for the audience) to see whether adopting Google Tasks correlates with better
  app retention. This is the number to use if you need to justify further investment in the
  feature.

### 4. Optional: BigQuery export

If you need cumulative totals, multi-event joins, or anything the Explore UI can't express
(e.g. "median days-to-adopt from install"), link the property to **BigQuery export**
(Admin → BigQuery Links) and query the `event_name = 'feature_adopted'` rows directly — this also
sidesteps GA4's data-thresholding on small user counts, which can hide adoption numbers early on
when the user base triggering the event is still small.

## Extending this to other features

To track adoption for another feature (e.g. birthdays, groups, presets):
1. Add a persisted one-time flag to `Prefs` (mirror `hasAdoptedGoogleTasks`).
2. At the point where the feature's core action first succeeds, guard the existing
   `FeatureUsedEvent` call with the flag and additionally send
   `FeatureAdoptedEvent(Feature.YOUR_FEATURE)`, then set the flag.
3. No GA4-side changes are needed — `type` is already a registered custom dimension and
   `feature_adopted` is already a key event, so the new feature's adoption data shows up in the
   same dashboard automatically once you filter by its `type` value.
