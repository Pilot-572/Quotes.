# Quotes.

An Android home-screen widget that shows a quote from your own collection and rotates it on a schedule you pick. Built for my own home screen first — it replaces manually retyping quotes into a notes widget.

> "Money wins." -Logan Roy, S2 Ep.5

## What it does

- **Your quotes, your library** — quote text is required; author and source are optional. The widget renders what exists and skips what doesn't.
- **Playlists** — group quotes into themed collections (Stoicism, Succession, Discipline, …) and rotate from one playlist or the whole library.
- **Per-quote style overrides** — size (auto-fit or S/M/L/XL), alignment, bold/italic, font family. Unset means "follow the widget default"; overrides are baked into the schema from v1, no migrations.
- **Transparent widget** — text floats on your wallpaper, alignment configurable, long quotes auto-fit instead of clipping.
- **Scheduled rotation** — presets for 8 AM daily / every 6h / 12h / 24h, or a custom interval.

## Stack

Kotlin · Jetpack Compose · Room · Glance (widget) · WorkManager — minSdk 26.

## Status

Active rebuild (v3) of my daily-driver v2 app, rewritten from scratch:

- [x] Room layer — Quote/Playlist entities, style overrides in schema v1, DAOs, tests
- [x] Compose UI — quote list, add/edit sheet with style section, playlists
- [ ] Glance widget — transparent, per-quote overrides, auto-fit text
- [ ] WorkManager scheduled rotation
- [ ] Share-as-image, Play Store polish

## Build

Open in Android Studio and run, or:

```
./gradlew assembleDebug
```

## License

GPL-3.0 — see [LICENSE](LICENSE).
