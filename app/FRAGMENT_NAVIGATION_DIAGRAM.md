# Fragment Navigation Diagrams

This document contains visual diagrams of the fragment navigation graph using Mermaid syntax.

## Bottom Navigation Structure

```mermaid
graph TB
    BottomNav[Bottom Navigation Bar]
    BottomNav --> Home[actionHome<br/>HomeFragment]
    BottomNav --> Events[actionEvents<br/>HomeEventsFragment]
    BottomNav --> Calendar[actionCalendar<br/>CalendarFragment]
    BottomNav --> Notes[actionNotes<br/>NotesFragment]
    BottomNav --> Tasks[actionGoogle<br/>GoogleTasksFragment]

    style Home fill:#e1f5ff
    style Events fill:#e1f5ff
    style Calendar fill:#e1f5ff
    style Notes fill:#e1f5ff
    style Tasks fill:#e1f5ff
```

## Complete Navigation Flow

```mermaid
graph TB
    %% Top Level
    Home[Home<br/>START]
    Events[Events View]
    Calendar[Calendar]
    Notes[Notes]
    Tasks[Google Tasks]

    %% Home Connections
    Home --> Settings[Settings Hub]
    Home --> CloudDrives1[Cloud Drives]
    Home --> Changes1[What's New]

    %% Settings Tree
    Settings --> General[General Settings]
    Settings --> Reminders[Reminders Settings<br/>param: screen_title?]
    Settings --> Export[Export Settings]
    Settings --> Notification[Notification Settings]
    Settings --> CalendarSettings[Calendar Settings]
    Settings --> Birthday[Birthday Settings]
    Settings --> Security[Security Settings]
    Settings --> Location[Location Settings]
    Settings --> Other[Other Settings]
    Settings --> NoteSettings1[Note Settings<br/>param: screen_title?]
    Settings --> Tests[Tests]
    Settings --> Pro[Pro Version]
    Settings --> Troubleshooting[Troubleshooting]

    General --> UIPreview[UI Preview]

    Reminders --> Presets[Manage Presets]

    Export --> CloudDrives2[Cloud Drives]

    CalendarSettings --> EventsImport[Events Import]

    Security --> AddPin[Add PIN]
    Security --> ChangePin[Change PIN]
    Security --> DisablePin[Disable PIN]

    Location --> MapStyle[Map Style]
    Location --> Places[Places]

    Other --> Permissions[Permissions]
    Other --> Changes2[What's New]
    Other --> OSS[Open Source Licenses]
    Other --> Privacy[Privacy Policy]
    Other --> Terms[Terms & Conditions]

    Tests --> ExportTest[Object Export Test]

    %% Events Connections
    Events --> Groups[Groups]
    Events --> Archive[Archive/Trash]
    Events --> Reminders2[Reminders Settings]
    Reminders2 --> Presets2[Manage Presets]

    %% Calendar Connections
    Calendar --> DayView[Day/Week View<br/>REQUIRED: date Long]
    Calendar --> CalendarSettings2[Calendar Settings]
    CalendarSettings2 --> EventsImport2[Events Import]

    %% Notes Connections
    Notes --> ArchivedNotes[Archived Notes]
    Notes --> NoteSettings2[Note Settings]

    %% Tasks Connections
    Tasks --> TaskList[Task List<br/>REQUIRED: arg_id String]
    TaskList --> PreviewTask[Preview Task<br/>REQUIRED: item_id String]

    %% Styling
    style Home fill:#4CAF50,color:#fff
    style Events fill:#2196F3,color:#fff
    style Calendar fill:#FF9800,color:#fff
    style Notes fill:#9C27B0,color:#fff
    style Tasks fill:#F44336,color:#fff
    style Settings fill:#FFC107
    style DayView fill:#ffebee
    style TaskList fill:#ffebee
    style PreviewTask fill:#ffebee
```

## Settings Section Detailed

```mermaid
graph TB
    Settings[Settings Hub<br/>12 destinations]

    Settings --> Gen[General Settings]
    Settings --> Rem[Reminders Settings]
    Settings --> Exp[Export Settings]
    Settings --> Not[Notification Settings]
    Settings --> Cal[Calendar Settings]
    Settings --> Bir[Birthday Settings]
    Settings --> Sec[Security Settings]
    Settings --> Loc[Location Settings]
    Settings --> Oth[Other Settings]
    Settings --> Note[Note Settings]
    Settings --> Test[Tests]
    Settings --> Pro[Pro Version]
    Settings --> Trbl[Troubleshooting]

    Gen --> UI[UI Preview]

    Rem --> Pre[Manage Presets]

    Exp --> Cld[Cloud Drives]

    Cal --> Imp[Events Import]

    Sec --> Add[Add PIN]
    Sec --> Chg[Change PIN]
    Sec --> Dis[Disable PIN]

    Loc --> Map[Map Style]
    Loc --> Plc[Places]

    Oth --> Perm[Permissions]
    Oth --> Chng[What's New]
    Oth --> OSS[OSS Licenses]
    Oth --> Priv[Privacy Policy]
    Oth --> Term[Terms]

    Test --> ObjTest[Object Export Test]

    style Settings fill:#FFC107
    style Sec fill:#f44336,color:#fff
    style Oth fill:#9C27B0,color:#fff
```

## Orphan Fragments (No Incoming Actions)

These fragments are defined in the navigation graph but have no explicit navigation actions pointing to them. They are likely invoked programmatically (e.g., from FAB buttons, list item clicks):

```mermaid
graph TB
    subgraph Orphans[Fragments with No Incoming Actions]
        EditBirthday[Edit Birthday]
        EditGroup[Edit Group]
        EditPlace[Edit Place]
        EditTask[Edit Google Task]
        EditTaskList[Edit Task List]
        PreviewReminder[Preview Reminder]
        FullscreenMap[Fullscreen Map]
        BuildReminder[Build Reminder]
        PreviewNote[Preview Note]
    end

    PreviewBirthday[Preview Birthday] --> EditBirthday

    style Orphans fill:#ffebee
    style PreviewBirthday fill:#e1f5ff
```

## Parameter Requirements

```mermaid
graph LR
    subgraph Required Parameters
        DayView[Day View<br/>date: Long]
        TaskList[Task List<br/>arg_id: String]
        PreviewTask[Preview Task<br/>item_id: String]
    end

    subgraph Optional Parameters
        RemSettings[Reminders Settings<br/>screen_title: String?]
        NoteSettings[Note Settings<br/>screen_title: String?]
    end

    style Required Parameters fill:#ffcdd2
    style Optional Parameters fill:#fff9c4
```

## Deep Links

```mermaid
graph LR
    External[External App/Link]
    External -->|reminderapp.com/calendar/date| DayView[Day View Fragment]

    style External fill:#4CAF50,color:#fff
    style DayView fill:#2196F3,color:#fff
```

## Fragment Access Patterns

```mermaid
graph TB
    subgraph Multi-Access Fragments
        A[Reminders Settings]
        B[Cloud Drives]
        C[What's New]
        D[Note Settings]
    end

    Settings1[Settings] --> A
    Events[Events View] --> A

    Home[Home] --> B
    Settings2[Settings/Export] --> B

    Home2[Home] --> C
    Settings3[Settings/Other] --> C

    Settings4[Settings] --> D
    Notes[Notes List] --> D

    style Multi-Access Fragments fill:#C8E6C9
```

## Navigation Complexity Score

Fragments ranked by number of outgoing navigation actions:

```mermaid
graph LR
    Settings[Settings: 12 actions] --> Most[Most Complex]
    OtherSettings[Other Settings: 5 actions] --> High
    Security[Security: 3 actions] --> Medium
    Home[Home: 3 actions] --> Medium
    Events[Events: 3 actions] --> Medium
    Location[Location: 2 actions] --> Low
    Calendar[Calendar: 2 actions] --> Low
    Notes[Notes: 2 actions] --> Low
    Others[15 fragments: 1 action each] --> Minimal

    style Settings fill:#f44336,color:#fff
    style OtherSettings fill:#FF9800,color:#fff
    style Security fill:#FFC107
    style Home fill:#FFC107
    style Events fill:#FFC107
```

## Animation Schemes

```mermaid
graph TB
    subgraph Slide Animation
        Home --> Settings
        Home --> CloudDrives
        Home --> Changes
        Notes --> NoteSettings
    end

    subgraph Default Animation
        Settings --> AllOthers[All Settings Destinations]
        Events --> AllDestinations[All Destinations]
        Calendar --> AllCalendar[All Destinations]
        Tasks --> AllTasks[All Destinations]
    end

    style Slide Animation fill:#E3F2FD
    style Default Animation fill:#FFF9C4
```

## Fragment Categories

```mermaid
mindmap
  root((Fragments<br/>47 total))
    Home & Dashboard
      HomeFragment
      HomeEventsFragment
    Reminders & Events
      PreviewReminderFragment
      BuildReminderFragment
      ArchiveFragment
      FullscreenMapFragment
    Calendar
      CalendarFragment
      WeekViewFragment
    Notes
      NotesFragment
      ArchivedNotesFragment
      PreviewNoteFragment
    Google Tasks
      GoogleTasksFragment
      TaskListFragment
      PreviewGoogleTaskFragment
      EditGoogleTaskFragment
      EditGoogleTaskListFragment
    Birthdays
      PreviewBirthdayFragment
      EditBirthdayFragment
    Groups & Places
      GroupsFragment
      EditGroupFragment
      PlacesFragment
      EditPlaceFragment
    Settings
      20+ Settings Fragments
```

---

## How to View These Diagrams

These diagrams are written in Mermaid syntax. You can view them in:

1. **GitHub** - Automatically renders Mermaid diagrams
2. **VS Code** - Install "Markdown Preview Mermaid Support" extension
3. **JetBrains IDEs** - Built-in Mermaid support in Markdown preview
4. **Online** - https://mermaid.live/

---

## Legend

- **Blue Boxes** - Top-level navigation fragments (bottom nav)
- **Green Boxes** - Entry points
- **Red Boxes** - High complexity (many connections)
- **Orange Boxes** - Medium complexity
- **Yellow Boxes** - Low complexity
- **Pink/Light Red Boxes** - Require parameters
- **Light Yellow Boxes** - Optional parameters
- **Light Green Boxes** - Multi-access fragments

---

**Generated:** 2025-11-02
**Source:** `app/src/main/res/navigation/home_nav.xml`

