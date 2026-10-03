# RE_MC

[English](README.md) | [简体中文](README.zh-CN.md) | [日本語](README.ja.md)

RE_MC is a Minecraft Forge 1.20.1 mod that adds a **Subaru Mode** and Death Return mechanics inspired by Re:Zero.
Subaru Mode is selected when creating a world. It behaves as a separate mode rather than modifying vanilla Hardcore, and cheats can be enabled separately.

When a player dies in Subaru Mode, the world is restored to the latest valid checkpoint, players return to their recorded state, and the triggering player receives a cinematic Return transition.

## Requirements

- Minecraft 1.20.1
- Forge 47.x
- Java 17
- A world created with Subaru Mode enabled

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
- At lower Spirit levels, random exposed block faces briefly show corrupted block textures; they now expire naturally and no longer require interaction to clear.
- Adds Spirit Herb, Witch's Tea, and Gospel. The Unseen Hand is no longer an item.
- Unseen Hand defaults to the V key. Active use locks every hostile within 20 blocks, sends layered black smoke arms with palms, fingers, purple glow, and dark red cores from the player and body shadow toward each enemy through obstacles, repeatedly punching them with Knockback IV force, and costs 10% Spirit. Each hand lasts until its enemy dies or 10 seconds pass.
- Passive trigger: below 3 hearts with at least 4 enemies within 20 blocks, it automatically consumes 20% Spirit and activates.
- Active and passive share a 5-minute cooldown; Death Return or sleeping resets it.
- Witch's Scent now decays over time and is not increased by Unseen Hand.
- Above 30% Witch's Scent, nearby mob spawning increases and forbidden chat phrases trigger a staged Witch curse that smoothly drains the health bar to zero over about 2 seconds.
- Sleeping restores Spirit. Post-return aura effect remains around the player for 5 minutes.

## Commands

```text
/remc return status
/remc return log [player]
/remc return checkpoint create
/remc return checkpoint force
/remc return recover
/remc unseenhand debug [player]
/remc spirit full [player]
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
- `witchScentDecayPerMinute`
- `sleepSpiritRestore`

## Build

```powershell
.\gradlew.bat build --no-daemon
```

Output:

```text
build/libs/re_mc-1.2.0.jar
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
