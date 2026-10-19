# ui-icons-solar

Icon pack module. Contains the "Solar Linear" icons as Android VectorDrawables (`src/main/res/drawable/ic_solar_*.xml`)
and the `com.github.naz013.ui.icons.solar.SolarIcons` catalog of `@DrawableRes` ids, one nested object per
upstream category (`SolarIcons.Time.AlarmAdd`, `SolarIcons.Sports.Dumbbell`, ...). Feature code references
only the catalog, never `R.drawable.ic_solar_*` directly.

## Notice

- The icons are the **Solar Icon Set** by **480 Design**, Linear style:
  https://github.com/480-Design/Solar-Icon-Set (`icons/SVG/Linear`, branch `main`).
- The set is listed as CC0 (public domain) on SVG Repo. The upstream GitHub repository itself does not ship an
  explicit license file.
- The original SVGs were converted to VectorDrawable XML: strokes keep their width (1.5), round caps/joins and
  fill rules; the colour is normalised to opaque black so Compose `Icon(tint = ...)` works; `circle`/`ellipse`/`rect`
  shapes became path data; the few dashed strokes (`stroke-dasharray`, unsupported by VectorDrawable) were
  baked into short stroke segments.
- Icons whose name exists in more than one category are named `ic_solar_<category>_<name>`.
