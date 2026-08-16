# Quotes. — rebuild (Stardance 2026)

Android home-screen quote widget app. Full rewrite of the v2 app, informed by its lessons.
Brand is **Quotes.** with the trailing period.

## Hard rules
- ALWAYS launch Claude Code from this folder (`cd C:\Users\fabia\Documents\Quotes` first) — Hackatime attributes AI coding time by session directory, and sessions started elsewhere log to the wrong project. The Hackatime project name is `Quotes.` (with the dot).
- The old app at `C:\Users\fabia\AndroidStudioProjects\Quote` is REFERENCE ONLY. Read it to recall decisions; NEVER copy files or paste code from it. Every line here is written fresh (Hackatime-tracked for Stardance).
- No `Co-Authored-By` trailers on commits. History reads as Fabian's.
- No pushes without Fabian's explicit go.
- Repo: `https://github.com/Pilot-572/Quotes..git` (public, GPL-3.0) — never commit keystores or `keystore.properties` (gitignored).

## Stack
Kotlin, Jetpack Compose, Room, Glance (widget), WorkManager (scheduled refresh).
minSdk 26, package `xyz.crt572.quotes`.

## Baked in from day one (v2 bolted these on late — don't repeat that)
- `Quote` schema includes nullable per-quote style override columns (size, bold, italic, alignment) in v1 of the DB. No migrations for styles.
- Widget is Glance from the start, respects per-quote overrides, auto-fits text.
- Playlists are a core entity (playlist ↔ quotes many-to-many), not an afterthought.
- Release signing config + Play Store listing prep early, not last.

## Data model (v2-proven)
Quote: `text` required; `author`, `source` optional. Widget renders what exists, skips what doesn't.
Example render: `"Money wins." -Logan Roy, S2 Ep.5`

## Build order
1. Room: entities (Quote, Playlist, join), DAOs, DB singleton
2. Compose UI: quote list, add/edit sheet (with style section), playlists screen
3. Glance widget: transparent background, alignment + style overrides, auto-fit
4. WorkManager: scheduled random-quote refresh (presets: 8AM / 6h / 12h / 24h / custom)
5. Share-as-image, Play Store polish

## Known v2 traps
- Style sheet inside a scrollable sheet: stacked scroll + clickable regions broke input (old fix in `f4c209e`).
- Widget attribution line needs a reserved height budget or long author/source clips the quote.
