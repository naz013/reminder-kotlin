# Workflow automation

Local-only trigger → condition → action rules over reminders and groups, in the spirit of a small Tasker.
Everything runs on-device: triggers are in-process events or local OS signals, actions are local writes or
local Android calls. Nothing calls a server, and cloud backup is never a trigger or an action target.
This replaces `workflow-engine-research.md`, which proposed this engine before it was built.

## Status

Shipped. `FeatureFlag.WORKFLOW_ENABLED` has a `false` code default and is enabled for users through Remote Config. `WorkflowConfig.isEnabled` is the flag alone;
`WorkflowConfig.isRunnable` additionally requires a PRO build, and the engine checks it at execution time, so
free users are routed to the PRO screen.

## Where the code is

| Layer | Module | Contents |
|---|---|---|
| Model | `core:domain` (`domain.workflow`) | `WorkflowRule`, `WorkflowScope` (`Global` / `ForGroup` / `ForReminder`), `WorkflowTrigger`, `WorkflowCondition`, `WorkflowAction`, `WorkflowTemplate`. Payload variants are Gson round-tripped, so every field carries `@SerializedName` (R8 safety). |
| Logic | `logic:logic-workflow` | `WorkflowEngine`, `WorkflowTriggerRunner`, `WorkflowActionDispatcher`, rule and template use cases. The engine returns a `PendingWorkflowAction` for the actions it cannot finish itself (`CompleteReminder`, `ActivateReminder`). |
| UI and background | `feature:feature-workflow` | Template gallery, rule builder, per-group and per-reminder rule screens, and the periodic `BackgroundTask`s: `RunWorkflowRulesTask`, `RunWorkflowUnacknowledgedRulesTask`, `RunWorkflowWifiPollTask`, `WeeklySummaryTask`. |

## Vocabulary

- **Triggers:** reminder created, reminder completed, snoozed N times, group all completed, location entered or
  exited, reminder age exceeded, unacknowledged for a duration, schedule reached, Bluetooth device connected or
  disconnected, Wi-Fi connected or disconnected.
- **Conditions:** priority at least, within a time window, group is, title contains, has tag.
- **Actions:** archive, complete, purge or activate a reminder, apply or clear a notification override, move to
  a group, apply or remove a tag, run a background task, send a broadcast intent (so Tasker-style tools can
  react).

Rules scope the same way notification settings do (reminder, then group, then global); see
[reminderv2-notification-hierarchy.md](reminderv2-notification-hierarchy.md).

## Design constraints that still apply

- **The process can be dead.** `TableChangeNotifier` is an in-process broadcast, so triggers that must survive
  process death (snooze counts, age, unacknowledged time, Wi-Fi) are polled by periodic `BackgroundTask`s
  through `WorkScheduler`, not observed.
- **Location triggers reuse the existing distance-polling service** (`GeolocationService`,
  `CheckLocationReminderUseCase`). There is no Play Services geofencing.
- **Bulk-apply triggers.** Schedule, Bluetooth and Wi-Fi triggers are not tied to one reminder's state, so their
  action applies to every reminder in the rule's scope.
- **Add a new trigger or action** by adding the sealed variant, handling it in `WorkflowEngine` /
  `WorkflowActionDispatcher`, and adding its builder option and formatter in `feature-workflow`'s
  `WorkflowRuleBuilderPickers.kt` / `WorkflowRuleBuilderFormatters.kt`.

## Ideas not built

- Chained reminders (completing one activates the next) beyond what `ActivateReminder` already allows.
- Auto re-lock after backgrounding.
- Auto-purge of attached places and shopping data with old completed reminders.
