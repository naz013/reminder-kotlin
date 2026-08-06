# Legal documents (Privacy Policy / Terms of Use)

Static HTML pages served via Firebase Hosting, publicly reachable at:

- Privacy Policy: `https://future-graph-651.web.app/privacy-policy.html`
- Terms of Use: `https://future-graph-651.web.app/terms-of-use.html`

These are the same URLs used both by the public web link and by the in-app WebView
(`PrivacyPolicyActivity`, `PrivacyPolicyFragment`, `TermsFragment` in `app`, via the `legal-api`/`legal`
Gradle modules — see `legal/README.md`... actually see the module's `KoinModule.kt` for wiring).

## Files

- `public/style.css` — shared stylesheet for all legal pages.
- `public/privacy-policy.html`
- `public/terms-of-use.html`

## Style rules (keep the pages "native" to the app)

- All colors live in the `:root` / `@media (prefers-color-scheme: dark)` blocks at the top of
  `style.css`. Never hardcode a color directly in a `<style>` tag or inline — add/adjust a variable
  instead, so light/dark mode stays automatic (mirrors how `app/src/main/assets/files/oss.html` already
  does it).
- The palette matches the app's Material 3 teal scheme
  (`ui-common/src/main/res/values/colors.xml` for light, `values-night/colors.xml` for dark). If that
  palette changes in the app, update the hex values in `:root` here to match.
- Content is grouped into `<details>/<summary>` collapsible sections, one per topic — same pattern as
  `oss.html`. Keep each section short; add a new `<details>` block rather than growing an existing one
  into an unrelated topic.
- Font is Montserrat (the app's brand font per `ui-common/values/type.xml`), loaded from Google Fonts
  with a system-font fallback stack so the page still looks fine if fonts.googleapis.com is unreachable.

## Previewing changes locally

A dev-server config is already set up at `.claude/launch.json` (`legal-docs` entry, serves
`legal-docs/public` on port 4173) — use it to preview edits before deploying.

## Publishing an update

1. Edit the HTML/CSS in `public/`.
2. Bump the version badge text and `<footer>` text in the page(s) you changed.
3. Deploy to Firebase Hosting from the repo root:
   ```bash
   firebase deploy --only hosting --project future-graph-651
   ```
   (requires `firebase login` once, with access to the `future-graph-651` Firebase project).
4. Update the matching Remote Config value in the Firebase Console
   (Remote Config → `privacy_policy_document` or `terms_of_use_document`) so the app can detect the
   change:
   ```json
   { "version": 2, "url": "https://future-graph-651.web.app/privacy-policy.html" }
   ```
   Bump `version` by 1 each time you publish a substantive change — this is what
   `LegalDocumentRepository.hasUpdate()` in the `legal` module compares against the last version the user
   has seen (mirrors how `WhatsNewManager` compares app version codes). The `url` only needs to change if
   you rename the file or move to a different host/domain.
5. Publish the Remote Config change in the console. Existing installs will pick it up on their next
   `LegalDocumentRepository.refresh()` call (app startup), respecting Remote Config's normal fetch
   throttling.
