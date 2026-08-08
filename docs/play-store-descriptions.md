# Google Play Store Listing — Descriptions

Draft short and full descriptions for both flavors, ready to paste into Play Console
(**Store presence → Main store listing**). Text is plain (no markdown) so it can be copied
directly into the respective fields.

Google Play limits:
- Short description: 80 characters max
- Full description: 4000 characters max

These drafts emphasize the app's offline-first, local-storage design and treat all cloud
features (Google Drive/Dropbox backup, Google Tasks sync) as explicitly optional. They do not
reference the in-progress workflow-automation feature, which isn't visible to users yet. Source
material: [app-overview.md](app-overview.md), [architecture.md](architecture.md), and
`legal-docs/public/privacy-policy.html` (confirms local-first storage, optional cloud backup to
the user's own account, free-version AdMob ads vs. ad-free Pro).

---

## Free version (`Reminder`)

### Short description (78 / 80 chars)

```
Offline reminders, notes & birthdays. Private by design, data stays on device.
```

### Full description (2457 / 4000 chars)

```
Reminder is a fast, all-in-one task manager built to work fully offline. Create reminders, take notes and track birthdays without ever going online - everything is stored in a private database on your device, not on our servers.

REMINDERS & SCHEDULING
- One-time, daily, weekly, monthly, yearly and fully custom recurring reminders (RRULE support)
- Location-based reminders that fire when you arrive at or leave a place
- Reminder actions: place a call, send an SMS or email, open a link, launch an app, or work through a shopping list
- Organize with groups, priority levels and colored tags
- Shopping-list reminders with sub-tasks
- Quiet hours (Do Not Disturb) to silence notifications on your own schedule
- Smart lists - Today, Overdue, This week and more - computed instantly on your device
- A complete history of every reminder that has fired
- Save your favorite repeat patterns and reuse them in one tap

NOTES
- Quick notes with rich-text formatting and photo attachments
- Search and filter across everything you've written
- Export notes as standard iCalendar files

BIRTHDAYS
- Track birthdays separately from your phone's calendar
- See who's celebrating today or coming up soon

CALENDAR
- One combined calendar view for reminders, birthdays and Google Tasks due dates

GOOGLE TASKS (optional, sign-in required)
- Browse and manage your Google Task lists with full two-way sync

WIDGETS & QUICK ACCESS
- Home-screen widgets for reminders, notes, birthdays and tasks
- A one-tap "Add reminder" Quick Settings tile

MAKE IT YOURS
- Material 3 design with your choice of color scheme
- Custom notification sounds and vibration patterns
- Import and export your data as iCalendar (.ics) files
- Lock the app with your fingerprint or face

PRIVATE BY DESIGN
Reminder was built offline-first. Your reminders, notes, birthdays, groups and places live in a local database on your device and are never uploaded anywhere unless you ask for it. Turn on Google Drive or Dropbox backup whenever you want to sync across devices - your data goes straight to a private folder in your own cloud account, never to a server we run. Location, contacts, camera, microphone and biometric permissions are all optional and are only ever used for the specific feature they power.

This free version is supported by ads. For an ad-free experience with on-device streak insights, encrypted local backups and Gemini AppFunctions integration, check out Reminder PRO.
```

---

## Pro version (`Reminder PRO`)

### Short description (75 / 80 chars)

```
Private, ad-free reminders, notes & tasks. Local encrypted backup included.
```

### Full description (3097 / 4000 chars)

```
Reminder PRO is a fast, all-in-one task manager built to work fully offline. Create reminders, take notes and track birthdays without ever going online - everything is stored in a private database on your device, not on our servers. Ad-free, with on-device insights and encrypted local backup built in.

REMINDERS & SCHEDULING
- One-time, daily, weekly, monthly, yearly and fully custom recurring reminders (RRULE support)
- Location-based reminders that fire when you arrive at or leave a place
- Reminder actions: place a call, send an SMS or email, open a link, launch an app, or work through a shopping list
- Organize with groups, priority levels and colored tags
- Shopping-list reminders with sub-tasks
- Quiet hours (Do Not Disturb) to silence notifications on your own schedule
- Smart lists - Today, Overdue, This week and more - computed instantly on your device
- A complete history of every reminder that has fired
- Save your favorite repeat patterns and reuse them in one tap

STREAKS & INSIGHTS (PRO)
- See your reminder streaks, weekly activity trends and busiest days at a glance
- Calculated entirely on your device from your own history - nothing is sent anywhere

NOTES
- Quick notes with rich-text formatting and photo attachments
- Search and filter across everything you've written
- Export notes as standard iCalendar files

BIRTHDAYS
- Track birthdays separately from your phone's calendar
- See who's celebrating today or coming up soon

CALENDAR
- One combined calendar view for reminders, birthdays and Google Tasks due dates

GOOGLE TASKS (optional, sign-in required)
- Browse and manage your Google Task lists with full two-way sync

GEMINI APPFUNCTIONS (PRO)
- Let Gemini and other on-device assistants create, find, complete and manage your reminders, notes, birthdays and tasks
- Runs through Android's on-device AppFunctions API - no network round-trip, nothing leaves your phone

LOCAL ENCRYPTED BACKUP (PRO)
- Export everything to a single AES-256 encrypted file, protected by a passphrase only you know
- Save it wherever you choose - entirely offline, no internet connection involved
- Restore anytime without losing anything already on your device

WIDGETS & QUICK ACCESS
- Home-screen widgets for reminders, notes, birthdays and tasks
- A one-tap "Add reminder" Quick Settings tile

MAKE IT YOURS
- Material 3 design with your choice of color scheme
- Custom notification sounds and vibration patterns
- Import and export your data as iCalendar (.ics) files
- Lock the app with your fingerprint or face

PRIVATE BY DESIGN
Reminder PRO was built offline-first. Your reminders, notes, birthdays, groups and places live in a local database on your device and are never uploaded anywhere unless you ask for it. Turn on Google Drive or Dropbox backup whenever you want to sync across devices - your data goes straight to a private folder in your own cloud account, never to a server we run. Location, contacts, camera, microphone and biometric permissions are all optional and are only ever used for the specific feature they power. No ads, no AdMob SDK, ever.
```

---

## Notes on claims made

- **"Fully offline" / "no internet needed"** applies to core reminders, notes, birthdays, groups,
  tags, places and local backup/restore (Pro) — all confirmed local-only in
  `legal-docs/public/privacy-policy.html`.
- **Google Tasks sync** and **Google Drive/Dropbox backup** are called out as optional and
  requiring sign-in, since they do need connectivity — avoids an overbroad "100% offline" claim
  that Play could flag against actual behavior.
- **Free-version ads / Pro ad-free** claim is sourced directly from the privacy policy's
  "Advertising (free version only)" section (AdMob, free only; no AdMob SDK in Pro).
- Firebase Analytics/Crashlytics usage (mentioned in the privacy policy) is intentionally not
  called out in the marketing copy — it's disclosed via the Play Data Safety form and privacy
  policy, not typically restated in store description body text.
- The in-progress workflow/automation engine (`docs/workflow-engine-research.md`) is deliberately
  excluded, per instructions, since it isn't shipped or user-visible yet.
