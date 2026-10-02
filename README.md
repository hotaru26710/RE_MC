# RE_MC

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md)

RE_MC is a Minecraft Forge 1.20.1 mod that adds a **Subaru Mode** and Death Return mechanics inspired by Re:Zero.
Subaru Mode is selected when creating a world. It behaves as a separate mode rather than modifying vanilla Hardcore, and cheats can be enabled separately.

When a player dies in Hardcore mode, the world is restored to the latest valid checkpoint, players return to their recorded state, and the triggering player receives a cinematic Return transition.

## Requirements

- Minecraft 1.20.1
- Forge 47.x
- Java 17
- Hardcore world mode enabled

## Features

- Subaru Mode enables Death Return without modifying vanilla Hardcore.
- Subaru Mode is chosen when creating a world and can independently allow cheats.
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
- At lower Spirit levels, random exposed block faces can show corrupted block textures; some persist until the player tries to interact with that block.
- Very low Spirit can create client-only hallucination zombies that approach the player without causing damage.
- Adds Spirit Herb, Witch's Tea, Gospel, and Unseen Hand items.
- Unseen Hand pulls one hostile target with an invisible force, costs Spirit, and increases Witch's Scent.
- Adds Witch's Scent, a persistent corruption stat that makes hallucinations more frequent.
- Above 30% Witch's Scent, nearby mob spawning increases and forbidden chat phrases trigger a staged Witch curse death.
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
build/libs/re_mc-1.1.0.jar
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
