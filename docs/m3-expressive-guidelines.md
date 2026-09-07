# M3 Expressive Design Guidelines — Reference for Screen Audits

A condensed, audit-oriented digest of the official Material 3 / M3 Expressive specification
(`m3.material.io/foundations`, `m3.material.io/styles`, and `m3.material.io/components`), written to be
used by an agent (or a human) that is checking one app screen at a time for compatibility with M3
Expressive.

**Relationship to the other M3 docs in this folder:**
- [`m3-expressive-adoption.md`](m3-expressive-adoption.md) is this repo's own plan/changelog — what
  M3 Expressive means for *this specific codebase* (Compose BOM/version gotchas, `ui-common` foundation
  work, what landed on Home/Agenda). Read it for "what has already been decided and built here."
- [`m3-expressive-screen-inventory.md`](m3-expressive-screen-inventory.md) is the per-screen tracking
  table — every navigable screen in the app and its migration status.
- **This document** is the source-of-truth spec digest — the actual Material rules (layout, color, type,
  shape, elevation, motion, states, accessibility, components) with concrete numbers, meant to be applied
  screen by screen. Use it to answer "does screen X actually follow the spec," then record the result in
  the inventory table and flag gaps for the adoption plan.

Sourced by walking every page under `m3.material.io/foundations` (Layout, Design tokens, Interaction
states, Accessibility), the relevant `m3.material.io/styles` sections (Color, Typography, Shape,
Elevation, Motion, Icons), and every overview page under `m3.material.io/components` (all 33 components)
in September 2026. Where the spec renders values inside an interactive token
module (images/canvas) rather than plain text, this doc uses the well-established M3 baseline values and
flags them — cross-check against `androidx.compose.material3.Typography`/`Shapes` defaults in code
(this repo pins `material3` `1.5.0-alpha27`, which has real values, not stubs — see
`m3-expressive-adoption.md` §2) as the more authoritative source when precision matters.

---

## How to use this doc for a screen audit

For each screen in `m3-expressive-screen-inventory.md`, walk sections 1–9 below and note gaps. Section 10
turns this into a single checklist. A "gap" isn't automatically a bug — cross-reference against
`m3-expressive-adoption.md`'s tactic #7 ("reserve 1–2 hero moments per screen, don't make everything
loud"): not every element needs emphasized type, a unique shape, or a container color. Flag gaps, then
let a human/PM decide which are worth fixing.

Section 9 (Components) is the fastest place to find *concrete, non-debatable* gaps: several components
were explicitly marked **"no longer recommended"** in the M3 Expressive update in favor of a named
replacement. A screen using one of those old components isn't a style-preference gap — it's using a
component the spec itself says to stop using.

---

## 1. Layout & adaptive design

### 1.1 Breakpoints

Material defines five breakpoints (formerly "window size classes"). Apply to both Android and web; a
device can move between breakpoints at runtime (rotation, foldable unfold, multi-window/split-screen).

| Breakpoint | Width (dp) | Common devices |
|---|---|---|
| Compact | < 600 | Phone in portrait |
| Medium | 600–839 | Tablet in portrait; foldable in portrait (unfolded) |
| Expanded | 840–1199 | Phone in landscape; tablet in landscape; foldable in landscape (unfolded); desktop |
| Large | 1200–1599 | Desktop |
| Extra-large | 1600+ | Desktop; ultra-wide monitors |

Height breakpoints (compact/medium/expanded) also exist on Android for unusually short/tall available
space, but are rarely needed since most layouts scroll vertically.

Recommended pane count and component swaps per breakpoint:

| Breakpoint | Panes (recommended) | Navigation | Communication | Supplemental action |
|---|---|---|---|---|
| Compact | 1 | Navigation bar (or modal nav rail) | Simple / full-screen dialog | Bottom sheet |
| Medium | 1 (or 2) | Navigation bar (or modal nav rail) | Simple dialog | Menu |
| Expanded | 1 or 2 | Modal or standard expanded nav rail | Simple dialog | Menu |
| Large | 1 or 2 | Modal or standard expanded nav rail | Simple dialog | Menu |
| Extra-large | 1 to 3 | Modal or standard expanded nav rail | Simple dialog | Menu |

When adapting a layout across breakpoints, ask the spec's five questions in order: **what should be
revealed** (e.g. nav rail expands, a second pane appears), **how should the screen divide** (pane count),
**what should be resized** (cards, feeds, lists, panes — keep 40–60 characters per line), **what should be
repositioned** (reflow, anchoring), **what should be swapped** (functionally-equivalent components only —
e.g. bottom nav bar ↔ nav rail; never swap a button for a chip or menu).

### 1.2 Layout scaffold: bars, rails, panes

Every adaptive layout is built from three scaffold regions:

- **Bars** — frame the screen edge-to-edge (or pane-width) to aid navigation: an app bar at the top
  (outside the safety region) or a navigation bar at the bottom (above the safety region, for 3–5 primary
  destinations at compact/medium breakpoints).
- **Rails** — occupy the perimeter space adjacent to bars: a toolbar above a bottom nav bar; a navigation
  rail (collapsed, or expanded at larger breakpoints) on the leading edge; a companion/trailing rail for
  supporting controls.
- **Panes** — hold the primary content. All layouts use 1–3 *visible* panes: Compact/Medium → 1
  (Medium can do 2 for low-density content only); Expanded/Large → 2; Extra-large → up to 3.
  - **Fixed** panes don't resize with available space; **flexible** panes do (every layout needs ≥1
    flexible pane).
  - Panes can be **permanent** or **temporary** (dismissible, e.g. a bottom sheet or side sheet).
  - Recommended custom snap widths for resizable panes: **360dp** or **412dp**. Standard side sheets max
    out at 400dp (note: fixed panes at extra-large are recommended at 412dp — a 400dp side sheet used as
    a fixed pane is a known mismatch to watch for).
  - **Displaying multiple panes**: co-planar (side by side — use for persistent utilities), floating
    (above other panes — use for temporary tasks like dialogs, scrim optional on large screens), or
    docked (pinned to an edge, e.g. a bottom sheet).
  - **Adaptation strategies**: show/hide (panes enter/exit with breakpoint or orientation), levitate
    (co-planar ↔ floating/docked), reflow (panes reorganize position, e.g. supporting pane moves below
    primary in portrait).

### 1.3 Canonical layouts

Three canonical layouts cover almost every screen shape. Prefer building new/adapted screens on one of
these rather than a bespoke structure.

- **Feed** — grid of cards/content for browsing (news, photos, social). Single column at compact,
  multi-column at medium+, column count/width increases with breakpoint. Items reflow on rotation/unfold.
- **List-detail** — parent-child pairing (e.g. message list + selected conversation; settings + category
  detail). This maps directly onto this app's list screens (Notes, Reminders, Birthdays, Groups, Tags,
  Places, Google Tasks) paired with their editor/preview/detail screens — see
  [`home-two-pane-design.md`](home-two-pane-design.md) if that doc exists for this repo's specific
  two-pane home implementation.
  - Compact (0–599dp): 1 pane, list ↔ detail navigation, back button appears only in detail view.
  - Medium (600–839dp): 1 pane recommended (2 possible for low-density/quick browsing) — use a bottom nav
    bar or modal nav rail to maximize horizontal space if going 2-pane.
  - Expanded/Large/XL (840dp+): 2 panes, selection state shown only in the list pane.
  - On fold/rotate transitions, preserve selection state and scroll position; if no item is selected, the
    detail pane shows an empty state, not a blank one.
- **Supporting pane** — primary content (~2/3 width) + secondary/contextual content. Use only when the
  secondary content is meaningless without the primary (otherwise use list-detail).
  - Compact/Medium: supporting pane appears **below** the focus pane (flexible width) — a bottom sheet
    works well here.
  - Expanded+: supporting pane appears **beside** the focus pane, fixed width **360dp**.

### 1.4 Grids, rulers, spacing, density

- Grids adapt column count/width/spacing per breakpoint; place bars/rails first, then panes.
- **Rulers** are alignment lines: bar/safety rulers (reserve space for system UI — status bar, gesture
  nav), title ruler (aligns app-bar title/icons), content rulers (anchor headlines/carousels/hero content),
  margin rulers (adjustable — narrower for immersive full-bleed media, wider for text-forward content).
- Spacing groups content: **explicit grouping** uses visible boundaries (dividers, outlines, card
  elevation); **implicit grouping** uses proximity/whitespace alone (e.g. a caption under an image, items
  in a carousel).
- **Density**: default touch target minimum is **48×48dp** regardless of density setting. Component
  density scale is numbered from 0 (default) down to -1/-2/-3 (denser), typically shaving 4dp off
  top/bottom padding or height per step. Never auto-scale below 48×48dp by default — density is opt-in via
  a setting, not automatic per breakpoint/orientation. Don't densify focused-task UI (menus) or alerting
  UI (snackbars, dialogs).

### 1.5 Bidirectionality & RTL

- **Mirroring**: LTR ↔ RTL flips horizontal alignment/order (leading/trailing terminology exists in
  Material precisely so components mirror automatically) — but not everything mirrors: linear
  time/progress in Hebrew stays LTR, media player controls always stay LTR, charts/graphs stay LTR for
  Persian/Urdu, clock hands still turn clockwise.
- Directional icons (back/forward arrows) mirror; icons like help (?) mirror only in some RTL languages
  (Urdu, Persian) not others (Hebrew).
- Text: RTL is right-aligned with right-to-left flow; don't reverse fixed-order tokens like
  `username@domain.com` (domain always trails).
- All three canonical layouts (feed, list-detail, supporting pane) mirror in RTL. Nav rail moves to the
  right edge in RTL. Swipe-to-reveal and predictive-back gestures mirror direction.

---

## 2. Color

### 2.1 Token hierarchy

Three token classes, always prefer the more general one unless overriding for a specific component:
`ref` (reference — raw values, e.g. a palette hex), `sys` (system — role assignments per theme/context,
e.g. light vs dark), `comp` (component — what a specific component part uses, ideally pointing to a `sys`
token rather than a hardcoded value). **A component or screen hardcoding a hex/dp value instead of
pointing to a token is a compatibility gap** — it can't respond to theme changes, dynamic color, or future
system-token updates.

### 2.2 Color roles (26 standard roles, 6 groups)

| Group | Roles | Use for |
|---|---|---|
| Primary | primary, on primary, primary container, on primary container | Highest-emphasis fills/text/icons — FAB, high-emphasis buttons, active states |
| Secondary | secondary, on secondary, secondary container, on secondary container | Lower-emphasis components — filter chips, inactive/selected nav icon backgrounds, tonal buttons |
| Tertiary | tertiary, on tertiary, tertiary container, on tertiary container | Contrasting accents / balancing primary+secondary — badges, input field highlights |
| Error | error, on error, error container, on error container | Error/urgency states (e.g. invalid text field). Static even under dynamic color, but still light/dark aware |
| Surface | surface, on surface, on surface variant, + 5 surface container levels (lowest/low/default/high/highest) | Backgrounds and neutral containers — cards, sheets, dialogs, nav surfaces |
| Outline | outline, outline variant | outline = important boundaries (text field border); outline variant = decorative (dividers), or borders where inner content already has contrast |

Key pairing rules (breaking these breaks accessibility, especially under user-controlled contrast):
- Always pair a color with its matching **on-** color for text/icons on top of it (e.g. `primary` +
  `on primary`, never `primary` + `on-surface`).
- **container** roles are fills only — never use a container color for text/icons.
- **variant**-suffixed roles are always the lower-emphasis alternative to their non-variant pair.
- The most common surface pairing: `surface` for the body/background area, `surface container` for
  navigation/chrome areas — and this mapping should **stay consistent across breakpoints** (don't swap
  which region gets which role just because the screen got bigger; add *more* surface container levels
  for hierarchy at larger sizes instead).
- Default component→surface-container mapping: nav bar/rail → surface container, menus → surface
  container low ~ high depending on component, dialogs → surface container high, bottom/side sheets →
  surface container low.

**Inverse roles** (`inverse surface`, `inverse on surface`, `inverse primary`) — for elements that
deliberately contrast the surrounding UI, e.g. a snackbar.

**Add-on roles** (most screens won't need these — only reach for them with a specific reason):
- **Fixed / fixed dim** (`primary fixed`, `primary fixed dim`, same for secondary/tertiary) — same tone in
  light *and* dark theme, unlike regular containers which invert. Use when a color must stay visually
  constant regardless of theme (this repo's `ui-common/compose/Color.kt` already defines these — see
  `m3-expressive-adoption.md`).
- **On fixed / on fixed variant** — text/icons on top of a fixed color.
- **Surface dim / surface bright** — keep relative brightness across both themes (surface bright is
  always the brightest surface in *both* light and dark, unlike default `surface` which inverts).

### 2.3 Static vs. dynamic color schemes

- **Static (baseline)** — fixed color assignments, doesn't respond to wallpaper/content. Use if not yet
  ready for dynamic color, migrating from M2, targeting enterprise users, or building for iOS. This repo
  currently uses a static/custom scheme (see `ui-common/compose/Theme.kt` per the adoption doc) — that's a
  legitimate, spec-sanctioned choice, not automatically a gap.
- **Dynamic** — generates an accessible scheme from a source color: **user-generated** (wallpaper) or
  **content-based** (in-app image, e.g. album art). Gives personalization + user-controlled contrast, at
  the cost of exact-color consistency across devices.
- Regardless of scheme type, **role mappings stay the same** — only the underlying hex values change. This
  is why it's safe to design/audit against role names rather than literal colors.

### 2.4 Color contrast requirements (accessibility)

- Large text (≥14pt bold / ≥18pt regular) and graphics: **≥3:1** against background.
- Small text: **≥4.5:1** against background.
- Disabled states are exempt from contrast requirements.
- Non-text elements that are **clustered** (e.g. a row of buttons) need ≥3:1 container-to-background
  contrast so each is individually distinguishable. **Standalone** prominent elements (e.g. a lone FAB)
  are exempt from this specific rule since their prominence already makes them distinguishable.
- Material now supports **three contrast levels** (standard/medium/high) as a token-driven, user-facing
  setting — worth checking whether a screen's custom (non-token) colors would survive a contrast-level
  bump, or whether they're hardcoded and would silently ignore it.

---

## 3. Typography

### 3.1 Type scale: 5 roles × 3 sizes × 2 emphasis levels = 30 styles

Roles, from largest/rarest to smallest/most common: **Display** (large/medium/small), **Headline**
(large/medium/small), **Title** (large/medium/small), **Body** (large/medium/small), **Label**
(large/medium/small). Every role/size also has an **emphasized** variant (added in M3 Expressive) —
heavier weight + minor width/spacing adjustments, same size step. No screen should use all 15 — pick what
the content hierarchy actually needs.

| Role | Typical use | Notes |
|---|---|---|
| Display | Short, important text/numerals, largest on screen | Best on large screens; can use an expressive/decorative typeface |
| Headline | Short high-emphasis text on smaller screens | Marks primary passages/regions |
| Title | Medium-emphasis, short — app bar titles, section headers | Use caution with decorative typefaces here |
| Body | Longer passages of text | Must stay readable — avoid decorative fonts |
| Label | Small, utilitarian — button text, nav destination labels, captions | e.g. buttons use `label large` |

**Baseline vs. emphasized** — Material components don't use emphasized styles by default; it's an
explicit token swap (e.g. `md.sys.typescale.display-large` → `md.sys.typescale.emphasized.display-large`).
Recommended emphasized-style targets: badges, primary-action buttons, extended FAB, **selected** list/menu
items, unread-state indicators, headlines, editorial moments. Two independent reasons to reach for
emphasized: **weight** (text that's already bold/medium — swap to keep consistent weight+width language)
and **context** (selected state, unread, active — to signal hierarchy without changing color alone).

Approximate baseline scale values (well-established M3 baseline numbers — verify against
`androidx.compose.material3.Typography` in code for this repo's exact `1.5.0-alpha27` defaults rather than
assuming, especially for emphasized-variant weight/tracking):

| Style | Size / line height (sp) |
|---|---|
| Display Large | 57 / 64 |
| Display Medium | 45 / 52 |
| Display Small | 36 / 44 |
| Headline Large | 32 / 40 |
| Headline Medium | 28 / 36 |
| Headline Small | 24 / 32 |
| Title Large | 22 / 28 |
| Title Medium | 16 / 24 |
| Title Small | 14 / 20 |
| Body Large | 16 / 24 |
| Body Medium | 14 / 20 |
| Body Small | 12 / 16 |
| Label Large | 14 / 20 |
| Label Medium | 12 / 16 |
| Label Small | 11 / 16 |

### 3.2 Applying type

- Two typefaces by role: **brand** typeface for large sizes (Display/Headline — where expression matters)
  and **plain** typeface for small sizes (Body/Label — where readability matters). Roboto is the default
  for both; a custom typeface can be substituted per-axis.
- Line length target: **40–60 characters per line** across all breakpoints (adjust margins/type size to
  hit this as width changes, not just letting text reflow arbitrarily wide).
- Line-height ratio: **~1.2×** the type size for large styles (title/headline/display); **~1.5×** for
  small styles (body/label).
- Use **tabular (monospaced) figures** anywhere numbers update frequently and must stay optically aligned
  (timers, clocks, live counters) — proportional digits cause visual jitter.
- Language-height categories affect line-height automatically (small: Latin/Cyrillic/Greek/Hebrew; medium
  ~7% taller: CJK, Arabic, Thai, Vietnamese, etc.; large ~30% taller: Burmese, Telugu; extra-large ~100%:
  Nastaliq) — components with vertical padding should adapt; **fixed-height components may not**, which is
  a real i18n risk to check when reviewing screens with translated strings (this repo ships 20+ locale
  `strings.xml` files per CLAUDE.md).

### 3.3 Typography accessibility

- Default text color: `on surface` (or `on surface variant` for lower emphasis) — not an arbitrary gray.
- Hyperlinked text: `primary` (or `tertiary` for lower-prominence links), and must be underlined, not
  color-only.
- Same contrast thresholds as §2.4: 3:1 large text, 4.5:1 small text.

---

## 4. Shape

### 4.1 Corner radius scale (10 steps — M3 Expressive expanded this from M2's 3-level scale)

| Style | Corner radius |
|---|---|
| None | 0dp |
| Extra small | 4dp |
| Small | 8dp |
| Medium | 12dp |
| Large | 16dp |
| Large increased | 20dp *(new in Expressive)* |
| Extra large | 28dp |
| Extra large increased | 32dp *(new in Expressive)* |
| Extra extra large | 48dp *(new in Expressive)* |
| Full | fully rounded (was "50% of component size" pre-Expressive — now a distinct `full` token, not a percentage) |

Shapes can be **symmetric** (all corners equal) or **asymmetric** (individual corners differ — Material
uses this for tightly-grouped components like menus and split buttons; these are called "inner corners").

This repo already has a shared `AppShapes` token object (`tile`=12dp/medium, `card`=16dp/large,
`pill`=28dp/extra-large) per `m3-expressive-adoption.md` §3 — **a screen using a literal
`RoundedCornerShape(Ndp)` instead of `AppShapes.*` or `MaterialTheme.shapes.*` is a token-hygiene gap**,
even if the rendered radius happens to match the scale.

### 4.2 Customization rules

- **Optical roundness**: when nesting rounded shapes, don't reuse the same radius for outer and inner —
  compute `outer radius − padding = inner radius` (e.g. 48dp outer, 14dp padding → 34dp inner) or the
  nested shape reads as visually unbalanced.
- Corners can switch families from **rounded** to **cut** (straight diagonal instead of curved) for a
  style change — but add extra padding, since a cut corner clips content/images more aggressively than a
  rounded one of the same size at the same radius.
- Avoid `large`/`extra large`/`full` corners on information-dense containers (e.g. a data-heavy card) —
  they clip content.

### 4.3 Shape library & morphing (M3 Expressive)

35 named shapes beyond the rectangular corner scale (cookie, pill, burst, clover, etc.), plus **shape
morph** — built-in animated interpolation between shapes, used for things like loading indicators
changing shape over time, or a button group's shape responding to selection. Use shapes/morphing sparingly
and only where there's a *reason* (state change, delight moment) — not as decoration without meaning
("shape is versatile, not semantic": don't assign one specific shape a literal fixed meaning). This is the
highest-effort, most novel piece of Expressive; per the adoption doc it's explicitly out of scope for this
repo's first passes, so its absence on a screen is *not* a gap to flag yet — only flag if an *ad hoc*,
non-tokenized shape is already being used where a scale step or shared morph would be more correct.

---

## 5. Elevation

Z-axis distance between surfaces, in dp. M3 (unlike M2) prefers **surface tint/color** over shadows by
default — shadows are reserved for cases needing stronger separation or explicit affordance.

| Level | DP | Example components |
|---|---|---|
| 0 | 0dp | App bar (not scrolled), filled/tonal/outlined buttons, button groups, filled/outlined cards, carousel, chips, full-screen dialog, icon buttons, list, navigation rail, segmented button, side sheet (docked), slider, split button, tabs |
| 1 | 1dp | Banner, modal bottom sheet, elevated button, elevated card, elevated chips, modal navigation drawer, modal side sheet |
| 2 | 3dp | Scrolled app bar, menu, navigation bar, rich tooltip, toolbar |
| 3 | 6dp | Date/time pickers, modal dialogs, extended FAB, FAB, FAB menu close button, search |
| 4 | 8dp | (no resting-level component; used for hover/focus deltas) |
| 5 | 12dp | (no resting-level component; used for hover/focus deltas) |

Rule: **don't change a component's default resting elevation** arbitrarily. Elevation *does* change in
response to interaction (e.g. hover raises a FAB by 1 level, 3→4) — consistently, across all instances of
that component type, never as a one-off per screen.

---

## 6. Motion (M3 Expressive physics system)

M3 Expressive replaced hand-tuned easing/duration curves with a **spring-based physics system**
(`MotionScheme`, public since `material3` `1.5.0-alpha27` — see `m3-expressive-adoption.md` §2 for why
this repo bumped to that version). A screen still using literal `tween()` durations/constants is the
concrete, checkable gap here.

- Two preset schemes: **expressive** (overshoots toward the final value, adds bounce — the default,
  recommended for most products including hero moments) and **standard** (eases in, minimal bounce — for
  strictly utilitarian contexts). Custom schemes are also possible.
- Two spring *types*: **spatial** (position, size, rotation, corner radius — overshoots/bounces) and
  **effects** (color, opacity — no overshoot).
- Three *speeds* per type: **fast** (small components — switches, buttons), **default** (partial-screen
  animations — bottom sheet, expanded nav rail), **slow** (full-screen transitions/refreshes).
- Applied via tokens, e.g. `md.sys.motion.spring.fast.spatial`; in Compose,
  `MaterialTheme.motionScheme.defaultSpatialSpec()` / `.fastEffectsSpec()` etc. — the scheme
  (expressive/standard) is set once at the theme level, not per call site, so swapping schemes doesn't
  require touching every animation.

---

## 7. Interaction states

Six states, applied via a **state layer** — a semi-transparent overlay using the *same color as the
component's content* (its "on-" color), never a separate ad hoc color. Interactive target size is 48dp;
the state-layer visual itself is 40dp.

| State | State-layer opacity | Trigger |
|---|---|---|
| Enabled | — (default styling) | n/a |
| Disabled | — (reduced color/elevation, exempt from contrast reqs) | n/a |
| Hover | +8% | Cursor pauses over element (pointer input only) |
| Focused | +10% | Keyboard/voice navigation (Tab or equivalent) — needs a visible ring-style indicator on web |
| Pressed | +10% | Tap/click/voice activation — often paired with a ripple and/or elevation bump |
| Dragged | +16% | Press-and-move |

Only one of hover/focus/press/drag applies at a time (though they can combine with **selected** or
**activated** states, which are separate/persistent, not part of this 6-state list). Disabled components
never receive hover/focus/press/drag. Not every component family inherits every state — e.g. app bars,
badges, dialogs, menus, nav bar/drawer/rail, sheets, and tabs generally don't get hover/focus/press/drag
*as a whole component* (only their individually-actionable children do).

---

## 8. Accessibility target checklist

Concrete, checkable numbers — the fastest part of this doc to turn into a pass/fail per screen.

- **Touch targets**: minimum **48×48dp** (Android/Material default; note iOS's own guidance is 44×44dp —
  irrelevant for this Android-only app, but worth knowing if content is ever shared cross-platform).
  A visually smaller icon (e.g. 24dp) still needs the full 48dp *interactive* area via padding.
- **Pointer targets** (mouse/stylus, e.g. Chromebook/desktop-mode): minimum **44×44dp**.
- **Target spacing**: ≥**8dp** between adjacent targets for balanced density/usability.
- **Text contrast**: **3:1** large text/graphics, **4.5:1** small text (§2.4/§3.3) — disabled states
  exempt.
- **Clustered element contrast**: ≥3:1 container-to-background for grouped interactive elements (§2.4).
- **Heading/landmark structure** (most relevant to WebView-hosted screens like Licenses/Privacy/Terms,
  per `m3-expressive-screen-inventory.md`'s notes-on-scope): sequential heading levels (don't skip H2→H4),
  one `<h1>`/main landmark, unique labels on repeated landmark roles.
- **Reading/focus order** matches visual arrangement — critical for coplanar/floating/docked panes (§1.2):
  focus order should follow the panes' visual layout, and a **modal** floating pane must trap focus
  (return focus to the trigger on close) while a **non-modal** one must not.
- **Standard platform controls** over custom-built equivalents wherever possible — custom dialogs/controls
  require extra assistive-tech testing that standard ones get for free.

---

## 9. Components

Full catalog from `m3.material.io/components` (33 components across 6 groups), condensed to what matters
for an audit: what a component is for, its variants/sizes, and — most importantly — whether the M3
Expressive update marked it **"no longer recommended"** in favor of a replacement. Deprecation entries are
the single most actionable finding this doc can produce: they're not a judgment call, they're the spec
telling you what to swap.

### 9.1 Buttons

| Component | Variants / sizes | M3 Expressive notes |
|---|---|---|
| **Buttons** | Color: elevated, filled, filled tonal, outlined, text. Shape: round, square. Sizes: XS, S, M, L, XL | Default height now 40dp, fully rounded by default. Shape morphs on press/selection. Leading/trailing icon standard size 20dp. Neutral text button no longer recommended. |
| **Button groups** | Standard, connected. Any button size (XS–XL) | **New in Expressive.** Connected button groups **replace segmented buttons**. Supports single-select, multi-select, selection-required. Shape morphs on press/selection. |
| **Icon buttons** | Default, toggle. Color: filled, tonal, outlined, standard. Shape: round, square. Sizes: XS, S, M, L, XL. Width: narrow, default, wide | Toggle state: outlined icon = unselected, filled icon = selected. Web: show a tooltip on hover. Shape morphs on press/selection. |
| **Segmented buttons** | Single-select, multi-select | **No longer recommended** — use **connected button group** instead (same functionality, updated visual design). |
| **Split buttons** | Color: elevated, filled, tonal, outlined. Sizes: XS, S, M, L, XL | **New in Expressive.** A common button + a menu icon button that spins/morphs when activated. This repo already has a hand-rolled `SplitButton`/`OutlinedSplitButton` in `ui-common` (per `m3-expressive-adoption.md` §2) — worth checking it against the now-public real component API rather than keeping the hand-rolled version indefinitely. |
| **FABs** | Sizes: FAB (56dp), medium FAB, large FAB. Color: primary, secondary, tertiary, primary/secondary/tertiary container | Small FAB **no longer recommended** (use regular FAB). Surface-color FAB **no longer recommended**. Boxier shape (not a circle), can use dynamic color. |
| **Extended FABs** | Small (56dp), medium (80dp), large (96dp) | Baseline extended FAB (56dp pill shape) and surface-color extended FAB are **no longer recommended** — replace with the small extended FAB. Now boxier, same height as the matching FAB size. |
| **FAB menu** | One size, pairs with any FAB. Color: primary, secondary, tertiary | **New in Expressive.** Opens from a FAB to show 2–6 related actions. Replaces the old "speed dial" pattern (stacked small FABs) — directly relevant to this app's Home `AddButton`, flagged in `m3-expressive-adoption.md` as still out of scope but a natural next step. |

### 9.2 Date & time pickers

| Component | Variants | M3 Expressive / M3 notes |
|---|---|---|
| **Date pickers** | Docked, modal, modal input | Renamed from device-specific names (desktop → docked; mobile → modal/modal input) to breakpoint-neutral ones. Larger type/spacing for 48dp targets. |
| **Time pickers** | Dial, input | Modal, covers main content. Make sure the dial is easy to hit by hand on mobile. |

### 9.3 Loading & progress

| Component | Variants | M3 Expressive notes |
|---|---|---|
| **Loading indicator** | Contained, uncontained | **New in Expressive**, recommended replacement for most indeterminate circular progress indicators (<5s waits, pull-to-refresh). Not for indicators that transition indeterminate→determinate — use progress indicators for that. |
| **Progress indicators** | Linear, circular | Configurable track height and an optional **wavy** active-track shape for extra expressiveness. Same configuration should be used for every instance of a given process. |

### 9.4 Navigation

| Component | Variants | M3 Expressive notes |
|---|---|---|
| **Navigation bar** | Baseline (no longer recommended), flexible (new) | Use at compact/medium breakpoints, 3–5 equal-importance destinations that don't change screen to screen. Flexible variant is shorter and supports horizontal items at medium. Active-label color changed on-surface-variant → secondary. |
| **Navigation rail** | Collapsed (replaces baseline rail), expanded (replaces nav drawer) | Use at medium/expanded/large/XL. 3–7 destinations + optional FAB. Collapsed ↔ expanded can transition on the same device. Active state now uses a pill-shaped indicator. |
| **Navigation drawer** | Standard, modal | **No longer recommended** — use the **expanded navigation rail** instead (same functionality, adapts better across breakpoints). |

### 9.5 Sheets

| Component | Variants | Key specs |
|---|---|---|
| **Bottom sheets** | Standard, modal | Use at compact/medium. 28dp top corner radius, max-width 640dp, optional drag handle with an accessible 48dp hit target. Content should be secondary, not the app's main content. |
| **Side sheets** | Standard, modal | Modal side sheet corner radius 16dp. Supports a back icon for in-sheet navigation. RTL: side sheet moves to the left. |

### 9.6 All other components

| Component | Variants | Notes |
|---|---|---|
| **App bars** | Search app bar, small, medium flexible, large flexible | Renamed from "top app bar" → "app bar." Medium/large (non-flexible) **no longer recommended** — replaced by medium flexible / large flexible (shorter, larger title, subtitle support, left/center-aligned text). New search app bar variant. On scroll: color fill instead of drop shadow. |
| **Badges** | Small, large | Anchor at the icon's upper trailing edge, inside its bounding box. Max 4 characters including a `+`. |
| **Cards** | Elevated, filled, outlined | Lower elevation, no shadow by default (M3 vs. M2). |
| **Carousel** | Multi-browse, uncontained, uncontained multi-aspect-ratio, hero, centered hero, full-screen | Items resize/parallax as they scroll; snap into place. ~10 items expected for multi-item scroll carousels. |
| **Checkbox** | — | Has indeterminate and error states in addition to selected/unselected. Use over switch/radio when multiple items in a list can be selected — directly relevant to this app's subtask/checklist editors (`SubTasksValueEditor.kt`). |
| **Chips** | Assist, filter, input, suggestion | 0dp elevation by default. Stroke color uses `outline variant` (softened from `outline` in an Aug 2024 update) for hierarchy against buttons. |
| **Dialogs** | Basic, full-screen | Larger corner radius and headline type than M2; use to force a decision on a single task (e.g. confirming a destructive action). |
| **Divider** | Horizontal, vertical | Use only when items can't be grouped by open space alone — a divider groups, it doesn't merely separate two neighbors. |
| **Lists** | Baseline, **expressive (new, recommended for new designs)** — standard or segmented visual style | Item height is 56/72/88dp depending on the tallest element; middle-aligned unless ≥88dp or 3+ text lines (then top-aligned). Expressive variant adds highlighted selection states and flexible content slots. |
| **Menus** | Baseline, **vertical menu (new, recommended for new designs)** — standard or vibrant color | Vertical menu adds gaps, dividers, submenus, and a "vibrant" color style for emphasizing selected items. "Dropdown menu" and "exposed dropdown menu" are now just "menu." |
| **Radio button** | — | Use over switch when exactly one item can be selected from a set. |
| **Search** | Search bar + search view, contained (recommended) or divided | Bar grows wider on focus; suggestions/results appear in a list below it. This repo's `ui-common` `SearchBar` composable is the existing implementation to check against this spec. |
| **Sliders** | Standard, centered, range. Sizes: XS, S, M, L, XL. Horizontal or vertical, optional inset icon | "Continuous" renamed to "standard," "discrete" is now a "stops" configuration. Track/handle shape changes on press. |
| **Snackbar** | Dismissive (auto-disappears), non-dismissive (persists until user acts) | Bottom of screen, shouldn't interrupt the flow — relevant to this repo's recently-added undo-delete snackbars for reminders/notes/birthdays. |
| **Switch** | — | Taller/wider track than M2; optional icon inside the handle. Use over radio buttons when the item can be controlled independently of others (e.g. a settings toggle). |
| **Tabs** | Primary, secondary | Icons/labels are now vertically centered in the tab container (was not always true in M2). Can scroll horizontally. |
| **Text fields** | Filled, outlined | State (blank / has input / error) must be visually obvious at a glance. |
| **Toolbars** | Docked (replaces bottom app bar), floating | **Bottom app bar no longer recommended** — replace with the docked toolbar (shorter, more flexible). Floating toolbar is new: horizontal/vertical, standard/vibrant color, can pair with a FAB, can hold many controls. Never show a toolbar and a navigation bar at the same time. |
| **Tooltips** | Plain, rich | Plain = short label for an icon button; rich = title + description + optional link/buttons. |

**Deprecation summary — flag these immediately when found on a screen:**

| If a screen uses… | Replace with |
|---|---|
| Segmented button | Connected button group |
| Navigation drawer (standard or modal) | Expanded navigation rail |
| Baseline navigation bar | Flexible navigation bar |
| Bottom app bar | Docked toolbar |
| Medium / large app bar (non-flexible) | Medium flexible / large flexible app bar |
| Baseline or surface-color extended FAB | Small extended FAB |
| Small FAB or surface-color FAB | Regular FAB (56dp) in a container color |
| Stacked small FABs ("speed dial") | FAB menu |

---

## 10. Screen audit checklist (practical rubric)

Copy this into a per-screen note when auditing. Not every line applies to every screen (e.g. a
single-purpose dialog won't need breakpoint pane logic) — skip what's not applicable rather than forcing
a fit, per the "1–2 hero moments, don't make everything loud" principle.

**Layout**
- [ ] Does the screen visibly adapt at Medium/Expanded breakpoints (not just stretch)? (§1.1)
- [ ] If it's a list+detail pattern, does it follow the list-detail canonical layout's per-breakpoint pane
      rules, including selection-state and back-button placement? (§1.3)
- [ ] Line length stays ~40–60 characters as width grows, rather than running edge-to-edge? (§1.4)
- [ ] RTL: does mirroring cover icons, alignment, and gesture direction correctly? (§1.5)

**Color**
- [ ] Do container colors get paired with the matching `on-` color, never mismatched? (§2.2)
- [ ] Are `surface`/`surface container` roles applied consistently across breakpoints (same region → same
      role at every size)? (§2.2)
- [ ] Any hardcoded hex values that should be color-role tokens instead? (§2.1)
- [ ] Text/graphic contrast meets 3:1 (large) / 4.5:1 (small)? (§2.4)

**Typography**
- [ ] Is emphasized type used (or deliberately not used) for selection/primary-action/unread moments,
      rather than manual `FontWeight.Bold` overrides? (§3.1)
- [ ] Body text uses a role meant for long-form reading (`body*`), not `display`/`headline`? (§3.1)
- [ ] Tabular numbers used anywhere values update in place (timers, counters)? (§3.2)

**Shape**
- [ ] Do corner radii come from the shape scale/`AppShapes` tokens, not a literal `Ndp` value? (§4.1)
- [ ] Nested rounded containers use optically-correct (not identical) radii? (§4.2)

**Elevation**
- [ ] Does each component sit at its spec-default resting elevation, changing only on interaction? (§5)

**Motion**
- [ ] Do animations use `MaterialTheme.motionScheme` spring specs rather than local `tween()` constants? (§6)

**States**
- [ ] Do interactive elements show all applicable states (hover/focus/press/drag) via a state layer in
      the content's own `on-` color, not a separate ad hoc highlight color? (§7)

**Accessibility**
- [ ] All tap targets ≥48×48dp, with ≥8dp spacing between adjacent ones? (§8)
- [ ] Focus order matches visual layout, especially in multi-pane or floating-pane screens? (§8)

**Components**
- [ ] No deprecated component is in use (segmented button, navigation drawer, bottom app bar, non-flexible
      medium/large app bar, baseline/surface extended FAB, small/surface FAB, stacked-FAB speed dial)? (§9)
- [ ] Each component's variant matches its actual purpose (e.g. checkbox vs. switch vs. radio button used
      per the "independent toggle vs. single-select vs. multi-select" distinction, assist vs. suggestion
      vs. filter vs. input chip)? (§9.6)
- [ ] Worth adopting a newer recommended variant (expressive list, vertical menu, FAB menu, split button)
      instead of the baseline one, given this screen's content? (§9.6) — optional, not a compatibility gap
      on its own, per the "1–2 hero moments" principle.

---

## Sources

All pages fetched from `m3.material.io` (Foundations + Styles sections), September 2026:

- Layout: overview, scaffold (bars/rails/panes), grids & spacing (grids/spacing/density), breakpoints,
  bidirectionality & RTL, canonical examples (feed/list-detail/supporting pane)
- Design tokens: overview, how to use tokens
- Color: system overview, color roles, choosing a scheme, static, dynamic
- Typography: overview, type scale & tokens, applying type
- Shape: overview & principles, corner radius scale
- Elevation: overview, tokens
- Motion: physics system (how it works)
- Interaction states: overview, state layers, applying states
- Accessibility: principles, designing (color contrast, structure)
- Icons: overview
- Components (overview page for all 33): button groups, buttons, extended FABs, FAB menu, FABs, icon
  buttons, segmented buttons, split buttons, date pickers, time pickers, loading indicator, progress
  indicators, navigation bar, navigation drawer, navigation rail, bottom sheets, side sheets, app bars,
  badges, cards, carousel, checkbox, chips, dialogs, divider, lists, menus, radio button, search, sliders,
  snackbar, switch, tabs, text fields, toolbars, tooltips
