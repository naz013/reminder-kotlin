# Google Play Store Listing — Translations

Translations of [../play-store-descriptions.md](../play-store-descriptions.md) (the English source of
truth) into 24 languages: the 16 the app already ships in its UI, plus 8 additional high-potential
markets recommended for expanding reach and conversion (see "Growth languages" below). Localizing the
*Play Store listing* itself — not just the app UI — is one of the highest-ROI, lowest-cost ASO moves
available; see
[research/GOOGLE_PLAY_ADS_CAMPAIGN_PROPOSAL.md](../../research/GOOGLE_PLAY_ADS_CAMPAIGN_PROPOSAL.md)
(Phase 0, item 6).

Each file follows the same structure as the English source: Free short/full description, then Pro
short/full description, each in a fenced code block ready to paste directly into the corresponding Play
Console locale tab.

## Locale mapping — app UI languages

These match an existing `values-<locale>` resource folder under `ui-common/src/main/res/`, so the
listing language and the in-app experience agree once a user installs.

| File | Language | Suggested Play Console locale | Matches app UI locale |
|---|---|---|---|
| [de.md](de.md) | German | `de-DE` | `values-de` |
| [fr.md](fr.md) | French | `fr-FR` | `values-fr` |
| [es.md](es.md) | Spanish | `es-ES` | `values-es` |
| [ru.md](ru.md) | Russian | `ru-RU` | `values-ru` |
| [uk.md](uk.md) | Ukrainian | `uk` | `values-uk` |
| [it.md](it.md) | Italian | `it-IT` | `values-it` |
| [pt.md](pt.md) | Portuguese | `pt-BR`* | `values-pt` |
| [pl.md](pl.md) | Polish | `pl-PL` | `values-pl` |
| [ro.md](ro.md) | Romanian | `ro` | `values-ro` |
| [cs.md](cs.md) | Czech | `cs-CZ` | `values-cs` |
| [bg.md](bg.md) | Bulgarian | `bg` | `values-bg` |
| [tr.md](tr.md) | Turkish | `tr-TR` | `values-tr` |
| [ja.md](ja.md) | Japanese | `ja-JP` | `values-ja` |
| [ko.md](ko.md) | Korean | `ko-KR` | `values-ko` |
| [zh.md](zh.md) | Chinese (Simplified) | `zh-CN`* | `values-zh` |
| [hi.md](hi.md) | Hindi | `hi-IN` | `values-hi` |

\* The app's `values-pt` and `values-zh` resource folders don't distinguish a region (Brazil vs.
Portugal; Simplified vs. Traditional script). These translations default to **Brazilian Portuguese**
and **Simplified Chinese** as the larger Play Store markets for each — if the target audience is
mainly European Portuguese or Traditional-script Chinese readers, some regional word choices may
need adjusting before publishing.

## Locale mapping — growth languages (⚠️ not yet app UI languages)

These were added on top of the app's existing 16 UI languages, picked for install-volume or
willingness-to-pay reasons (Indonesia for sheer Android volume; the Gulf Arabic-speaking markets and
the Nordics for stronger Pro-conversion economics; Vietnamese/Thai for volume similar to Indonesian).
**The app's own UI is not translated into any of these languages yet** — a user in one of these
markets would install an app whose Play Store listing is in their language but whose actual screens
are in English. That's a real risk for reviews/uninstalls ("app isn't actually in my language"), so
treat publishing these listings as step one of a two-step plan, not a complete one:

1. Publish these listings to validate whether the market actually converts (installs, and ideally
   Pro attribution via the referrer plumbing from the Phase 0 funnel work) before investing in full
   app UI localization.
2. If a language shows real traction, prioritize adding it as a proper `values-<locale>` app UI
   translation — otherwise the listing is over-promising.

| File | Language | Suggested Play Console locale |
|---|---|---|
| [id.md](id.md) | Indonesian | `id-ID` |
| [ar.md](ar.md) | Arabic (Modern Standard) | `ar` |
| [sv.md](sv.md) | Swedish | `sv-SE` |
| [da.md](da.md) | Danish | `da-DK` |
| [nb.md](nb.md) | Norwegian Bokmål | `nb-NO` (Play Console may just say "Norwegian") |
| [vi.md](vi.md) | Vietnamese | `vi-VN` |
| [th.md](th.md) | Thai | `th-TH` |
| [nl.md](nl.md) | Dutch | `nl-NL` |

## Before publishing: verify character limits

Google Play enforces **80 characters** for the short description and **4000 characters** for the full
description, regardless of language. All 24 files were machine-verified (character length extracted
and checked against these limits) after translation, and everything currently in this folder passes.
If you edit any short description afterward, re-check its length — German, Dutch, and other
compound-word-heavy languages in particular can run long for the same meaning, and several short
descriptions across this set were trimmed at least once to fit under 80.

## Content notes

- Product/feature names (Google Tasks, Google Drive, Dropbox, Gemini AppFunctions, Material 3,
  iCalendar, AES-256, AdMob) are kept untranslated/as-is across all locales, matching how Google and
  most Play Store listings handle proper nouns and technical terms.
- Section headers (REMINDERS & SCHEDULING, NOTES, PRIVATE BY DESIGN, etc.) are translated, not kept in
  English, since they're plain descriptive labels rather than product names.
- The same privacy/offline/local-storage claims and scope limits documented in the English source
  apply identically here — see that file's "Notes on claims made" section.
