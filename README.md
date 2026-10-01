# RE_MC

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md)

RE_MC is a Minecraft Forge 1.20.1 mod that adds a Hardcore-only **Death Return** mechanic inspired by Re:Zero.

When a player dies in Hardcore mode, the world is restored to the latest valid checkpoint, players return to their recorded state, and the triggering player receives a cinematic Return transition.

## Requirements

- Minecraft 1.20.1
- Forge 47.x
- Java 17
- Hardcore world mode enabled

## Features

- Hardcore-only Death Return trigger.
- Initial checkpoint created after world startup.
- Automatic checkpoint attempts every 15 minutes.
- Manual checkpoint commands with permission levels.
- Candidate checkpoint grace period before promotion.
- Full world snapshot/rollback pipeline for the current implementation.
- Death log and return counter per player.
- Random Death Return sound playback after successful return.
- Client-side Return transition with black-out and purple Witch aura effects.
- Spirit HUD above the health bar with animated ten-segment display.
- Death Return reduces Spirit by 5%-30% depending on the death cause.
- Spirit recovers by 1% per minute while online and applies tiered debuffs at low values.
- Low Spirit gradually desaturates and dims the world, stopping at near-monochrome rather than full black and white.
- Post-return aura effect that remains around the player for 5 minutes.

## Commands

```text
/remc return status
/remc return log [player]
/remc return checkpoint create
/remc return checkpoint force
/remc return recover
```

Permission levels:

- `status`, `log`: player accessible; viewing another player requires permission level 2.
- `checkpoint create`: permission level 2.
- `checkpoint force`, `recover`: permission level 4.

## Configuration

The server config is written to `serverconfig/re_mc-server.toml`.

Important options include:

- `enabled`
- `hardcoreOnly`
- `debugAllowNonHardcore`
- `autoIntervalMinutes`
- `safeHealthPercent`
- `damageFreeSeconds`
- `candidateGraceSeconds`
- `captureTimeoutSeconds`
- `allowBossAndRaid`
- `showTransition`
- `spiritEnabled`
- `spiritRecoveryPerMinute`
- `spiritHudEnabled`

## Build

```powershell
.\gradlew.bat build --no-daemon
```

Output:

```text
build/libs/re_mc-1.0.0.jar
```

## Development Notes

- The Death Return snapshot system currently performs full directory snapshots.
- Full in-place world rebuilding uses Forge access transformers and Minecraft server lifecycle internals.
- Test with a dedicated Hardcore world before using it in a long-lived world.
- Keep backups of important worlds.

## Third-Party Audio Notice

The bundled files under `src/main/resources/assets/re_mc/sounds/deathreturn/` are user-provided third-party sound assets. They are **not** covered by the MIT License for this project. Replace or license them before redistributing this mod publicly or commercially.

## License

Code is licensed under the MIT License. See `LICENSE` for details.
