# Re:Zero Lore and Setting Notes for a Minecraft Forge Hardcore Expansion

Research date: 2026-10-01

## Source policy

This note prioritizes primary and official material:

- **Author-hosted original web novel**: Tappei Nagatsuki's serialized text on Shousetsuka ni Narou. This is useful for direct claim tracing, but it may differ from later published light-novel revisions.
- **Official Kadokawa/original-work portal**: story, character, and book-synopsis pages.
- **Official anime portal**: character and episode-story pages.

No general fandom Wiki was used for the core claims below. Where canon is ambiguous, the note marks it as ambiguous instead of filling the gap with fan theory.

## 1. Return by Death

### Canon anchors

- Subaru first concludes that dying returns him to an earlier point while he keeps memories of the previous attempt. He initially describes it as a time-rewind-like phenomenon and calls the experience "Return by Death" in the author-hosted web novel. ([Web novel ch. 15](https://ncode.syosetu.com/n2267be/15/))
- Death is not clean or abstract. Subaru experiences the pain of dying, and the narrative repeatedly treats each death as a traumatic event rather than a simple checkpoint reset. ([Web novel ch. 15](https://ncode.syosetu.com/n2267be/15/), [ch. 16](https://ncode.syosetu.com/n2267be/16/))
- Subaru cannot freely disclose the ability. An attempt to explain it brings a hostile Witch-related intervention, and the Witch's residual scent intensifies around that intervention. ([Web novel ch. 68](https://ncode.syosetu.com/n2267be/68/))
- Repeated death and failure accumulate psychological damage. A later passage describes him falling into mental collapse and approaching madness before being pulled back. ([Web novel ch. 161](https://ncode.syosetu.com/n2267be/161/))
- The official anime story page for a later arc explicitly frames accumulated deaths as eroding the mind and records earlier death memories as part of the character's burden. ([Official anime story page](https://re-zero-anime.jp/tv/story/))

### Design-oriented reading

- Treat the mechanic as a **scenario reset plus retained memory**, not as an ordinary respawn. The interesting part is knowledge carried across attempts, not infinite lives.
- Keep the ability **secret or dangerous to reveal**. In a mod, this can be represented by a reveal-penalty, an enforcement effect, or a narrative warning rather than by blocking chat.
- Separate **world-state reset** from **player knowledge**. The world returns, but the player's journal, map notes, recipes discovered, or boss-pattern knowledge can persist as the "memory" layer.
- Repeated deaths should have a visible toll: insomnia, fear, hallucinations, reduced maximum composure, or changed NPC reactions.
- Canon ambiguity is important: the exact checkpoint rules, why Subaru is chosen, and the full motive behind the intervention are presented as mysteries, not as a finished technical spec. ([Web novel ch. 15](https://ncode.syosetu.com/n2267be/15/), [ch. 241](https://ncode.syosetu.com/n2267be/241/))

## 2. Witch's Scent

### Canon anchors

- Subaru carries a "Witch's residual scent" after becoming entangled with the Witch of Envy. Characters with acute senses can detect it and react with fear or disgust. ([Web novel ch. 68](https://ncode.syosetu.com/n2267be/68/), [ch. 71](https://ncode.syosetu.com/n2267be/71/))
- Mabeasts react excessively to that scent. The web novel states the in-world belief that Mabeasts are enemies of humanity created by the Witch and are drawn toward the scent. ([Web novel ch. 68](https://ncode.syosetu.com/n2267be/68/))
- When Subaru attempts to reveal Return by Death, the scent becomes stronger along with the Witch-related interference. This makes the scent both a targeting beacon and a consequence of forbidden disclosure. ([Web novel ch. 68](https://ncode.syosetu.com/n2267be/68/))

### Design-oriented reading

- Model it as a hidden **Witch Taint / Scent** statistic rather than a normal potion effect.
- Let it increase hostile detection, Mabeast spawn pressure, spirit suspicion, and cult attention. It can also be used deliberately as bait at high risk.
- Do not make cleansing trivial. Canon treats the scent as persistent and poorly understood; cleansing should be temporary, expensive, location-bound, or incomplete.
- Make it observable through indirect clues: animal behavior, NPC dialogue, hostile pathing, or specialized detection tools. Direct UI numbers reduce the unsettling ambiguity.

## 3. Witches and Gospels

### Canon anchors

- The Witch of Envy is remembered as a world-threatening figure. Emilia is feared because she resembles her. ([Official anime character page](https://re-zero-anime.jp/tv/character/))
- Echidna identifies herself as the Witch of Greed and says she is one of six witches bearing the names of deadly sins who were destroyed by the Witch of Envy. ([Official original-work character page](https://re-zero.com/character/))
- Sin Archbishops use Gospels. Petelgeuse carries a black-bound Gospel, treats its contents as instruction, and interprets events as trials tied to his faith. The text is not readable to Subaru. ([Web novel ch. 155](https://ncode.syosetu.com/n2267be/155/))
- Petelgeuse also speaks of the seats of the Sin Archbishops, an empty Pride seat at that point, and a Witch Factor moving toward the next Pride. This establishes that the Gospels, seats, and Witch Factors are connected parts of a larger mystery rather than separate trivia. ([Web novel ch. 140](https://ncode.syosetu.com/n2267be/140/))

### Design-oriented reading

- Separate **Witches** as historical/mythic patrons from **Sin Archbishops** as active wielders of political and supernatural power.
- Gospels should be dangerous quest objects, not ordinary spellbooks. Reading one can reveal an objective, change a faction's behavior, or mark the reader for attention.
- Avoid reproducing prayers, creeds, or long scripture-like text. Use original procedural riddles, omens, and contradiction-based objectives instead.
- Keep Witch history fragmentary. Let different factions give biased versions rather than delivering one complete lore encyclopedia.

## 4. Authorities

### Canon anchors

- Authorities are treated as exceptional powers distinct from ordinary magic and spirit arts. A character explicitly calls the Unseen Hand a technique unlike magic or spirit arts. ([Web novel ch. 145](https://ncode.syosetu.com/n2267be/145/))
- The Sloth Authority is described as including invisible deadly hands, mental contamination that breaks morale and sanity, and possession that makes a single kill insufficient. ([Web novel ch. 155](https://ncode.syosetu.com/n2267be/155/))
- The sin-named Witches are also described as possessing extraordinary Authorities. ([Web novel ch. 240](https://ncode.syosetu.com/n2267be/240/))
- Witch Factors are tied to the Sin Archbishop seats and to the Witches. Echidna says the Witch of Envy's power was incomplete when the Witch Factor was missing. ([Web novel ch. 140](https://ncode.syosetu.com/n2267be/140/), [ch. 241](https://ncode.syosetu.com/n2267be/241/))

### Design-oriented reading

- Authorities should be **boss-only exception mechanics**. They can break normal rules: invisible hazards, forced target swaps, false deaths, memory attacks, or staggered possession phases.
- Do not make them ordinary player spells with mana costs. Their cost is narrative, psychological, organizational, or identity-based.
- Give each Authority one clear rule and one clear counter-rule. Example: "invisible pursuit until the anchor object is destroyed" is more Re:Zero-like than a generic damage aura.
- Avoid direct named reproduction in a public mod unless rights are cleared. Mechanical inspiration is much safer than copying names, visuals, text, or iconic scene structure.

## 5. Witch Cult

### Canon anchors

- The Witch Cult is described as an infamous worldwide organization, and Petelgeuse is an executive Sin Archbishop who worships the Witch of Envy. ([Official anime character page](https://re-zero-anime.jp/tv/character/))
- The web novel says Sloth and Greed are among the most publicly notorious Sin Archbishops, but details of the Cult are not widely known. ([Web novel ch. 140](https://ncode.syosetu.com/n2267be/140/))
- Cult activity includes coordinated road blockades and the use of Mabeasts as strategic weapons. The White Whale blockade is tied to the Cult's larger operation against Emilia's group. ([Web novel ch. 140](https://ncode.syosetu.com/n2267be/140/))
- Cultists can be fanatical and difficult to reason with, while elite Sin Archbishops operate as individual ideological disasters. ([Web novel ch. 141](https://ncode.syosetu.com/n2267be/141/), [ch. 155](https://ncode.syosetu.com/n2267be/155/))

### Design-oriented reading

- Build the Cult as **cells, splinters, and handlers**, not a single polished evil kingdom.
- World events should signal Cult activity: roads closed, supply caravans missing, animal migrations, strange lights, black-clad scouts, or a Gospel-driven ritual timer.
- Elite enemies should fight through environmental control and belief mechanics, while ordinary cultists provide numbers and logistics.
- Keep knowledge asymmetrical. Rumors, captured notes, and interrogation should be more useful than a complete faction wiki.

## 6. Mabeasts

### Canon anchors

- Mabeasts are described in-world as human enemies created by the Witch and are strongly attracted to Subaru's Witch scent. ([Web novel ch. 68](https://ncode.syosetu.com/n2267be/68/))
- The Ulgarm pack is a Mabeast threat associated with a curse and life-draining attacks; Subaru's crisis in that arc centers on hunting the source under time pressure. ([Web novel ch. 64](https://ncode.syosetu.com/n2267be/64/), [ch. 68](https://ncode.syosetu.com/n2267be/68/))
- The White Whale is called an "End Beast" and acts as a strategic calamity, not just a large monster encounter. It is connected to the Witch Cult's blockade and its legend persists in later discussion. ([Web novel ch. 126](https://ncode.syosetu.com/n2267be/126/), [ch. 140](https://ncode.syosetu.com/n2267be/140/))

### Design-oriented reading

- Use ecology over spawners: blood, noise, fog, corpse piles, Witch Scent, and night cycles should influence Mabeast pressure.
- Pack Mabeasts should track and surround, while calamity-class Mabeasts should alter weather, travel, or world events.
- Drops should be contaminated or dangerous. Harvesting a Mabeast can attract more, spread curses, or create unstable materials.
- For a safe adaptation, create original creature families and only borrow broad behavior patterns: scent hunts, curse marks, apex weather events, and coordinated pack tactics.

## 7. Spirits, Contracts, and Mental Degradation

### Canon anchors

- Spirits consume mana even while manifested and may return to a vessel to recover mana. Puck's manifestation upkeep is explicitly discussed. ([Web novel ch. 6](https://ncode.syosetu.com/n2267be/6/))
- Magic users use their internal mana and a Gate; spirit users work through spirits and use atmospheric mana for spirit arts. ([Web novel ch. 58](https://ncode.syosetu.com/n2267be/58/))
- A contract is binding even when provisional, and it can carry immediate physical effects such as returning vitality. ([Web novel ch. 47](https://ncode.syosetu.com/n2267be/47/))
- Transferring mana directly to another person is advanced, dangerous, and can injure the target if the balance is wrong. ([Web novel ch. 74](https://ncode.syosetu.com/n2267be/74/))
- There are two different kinds of "madness" in the material: trauma-driven mental collapse from repeated failures, and supernatural mental attacks such as the Sloth Authority's contamination. ([Web novel ch. 161](https://ncode.syosetu.com/n2267be/161/), [ch. 155](https://ncode.syosetu.com/n2267be/155/))
- The area around the Witch of Envy's seal is surrounded by dense miasma that attacks mind, body, and soul; even Cult members reportedly avoid it. ([Web novel ch. 179](https://ncode.syosetu.com/n2267be/179/))

### Design-oriented reading

- Use two tracks:
  - **Trauma / Composure** from experiences, deaths, failures, and isolation.
  - **Corruption / Miasma** from supernatural sources, cursed ground, forbidden readings, or boss attacks.
- When both are low, trigger "Madness" states with specific symptoms rather than a single generic debuff.
- Spirit companions should have contracts, mana upkeep, trust, rest cycles, refusal conditions, and long-term consequences.
- Healing should require skill, time, reagents, or spirit cooperation. Instant healing potions should be rare or absent in a Hardcore mode.

## 8. Safe Concepts and Items for a Hardcore Survival Mod

| Concept | Canon basis | Safe adaptation idea |
| --- | --- | --- |
| Magic tools | Rare tools let non-mages use magical effects; early text calls them "magic tools." ([Web novel ch. 9](https://ncode.syosetu.com/n2267be/9/)) | Rare utility items with fuel, durability, or unstable charges. |
| Mana types | Fire, water/healing, wind, and earth mana are described with different life/body associations. ([Web novel ch. 39](https://ncode.syosetu.com/n2267be/39/)) | Four schools: heat, restoration, movement/warding, and body/fortitude. |
| Healing magic | Water mana relates to life and healing, but direct mana transfer is dangerous and advanced. ([Web novel ch. 39](https://ncode.syosetu.com/n2267be/39/), [ch. 74](https://ncode.syosetu.com/n2267be/74/)) | Rituals and skilled healing stations, not easy combat potion spam. |
| Magic stone / ore | Magic ore can be used as a disposable attack or energy release. ([Web novel ch. 140](https://ncode.syosetu.com/n2267be/140/)) | Unstable fuel, trap material, or explosive component. |
| Sealing stone | The Witch of Envy is sealed with sealing stone, but the site is surrounded by dangerous miasma. ([Web novel ch. 165](https://ncode.syosetu.com/n2267be/165/), [ch. 178](https://ncode.syosetu.com/n2267be/178/), [ch. 179](https://ncode.syosetu.com/n2267be/179/)) | Rare anti-magic containment material with dangerous side effects. |
| Spirit contract | Contracts bind spirits and can grant effects, but spirits consume mana and have needs. ([Web novel ch. 6](https://ncode.syosetu.com/n2267be/6/), [ch. 47](https://ncode.syosetu.com/n2267be/47/), [ch. 58](https://ncode.syosetu.com/n2267be/58/)) | Companion AI with mana upkeep, trust, and contract clauses. |
| Gospel | Gospels guide Sin Archbishops and are not ordinary readable books. ([Web novel ch. 155](https://ncode.syosetu.com/n2267be/155/)) | Dangerous quest item that changes objectives and attracts attention. |
| Witch Scent | Mabeasts and sensitive characters react to it. ([Web novel ch. 68](https://ncode.syosetu.com/n2267be/68/), [ch. 71](https://ncode.syosetu.com/n2267be/71/)) | Hidden heat stat that affects mob detection and faction events. |
| Miasma | The Witch's seal has miasma that harms mind, body, and soul. ([Web novel ch. 179](https://ncode.syosetu.com/n2267be/179/)) | Biome/weather hazard requiring wards, rot, or temporary protection. |

## Minecraft-safe adaptation suggestions

- **Keep the mechanics, change the expression.** Use original names, icons, structures, item text, and faction identities unless you have explicit rights. Do not copy character names, logos, dialogue, prayers, or recognizable scenes by default.
- **Make Return by Death opt-in.** In Hardcore survival, a global auto-reset would erase the mode's tension. Prefer a scenario-specific "memory loop" questline or a world option.
- **Use persistent knowledge, not persistent loot.** The player remembers patterns, but resources and world state reset in a loop. This captures the central burden without making death a farming exploit.
- **Turn Witch Scent into a heat system.** Increase detection radius, spawn weights, and special encounter chance. Offer partial cleansing through wards, rituals, or safe zones.
- **Make Gospels quest contracts.** A Gospel changes the active objective and creates both a reward and a threat. It should not simply grant free spells.
- **Keep Authorities boss-only at first.** Implement them as event modifiers: invisible hazards, possession phases, forced movement, sanity pressure, or false-death mechanics.
- **Separate sanity and corruption.** Ordinary recovery, shelter, companionship, food, and sleep can restore composure. Cleansing corruption should require rare sites, items, or rituals.
- **Spirits are companions, not pets.** They need mana, rest, trust, and contract limits. A spirit can refuse an order if the contract or relationship is strained.
- **Avoid instant healing loops.** Use treatment time, clean bandages, medicinal herbs, water magic, or spirit aid. Keep combat medicine scarce.
- **Use Mabeasts as ecology.** Bind spawn pressure to scent, blood, weather, light, and player noise. Calamity Mabeasts should be server/world events with evacuation and preparation phases.
- **Write original lore text.** If a UI needs scripture-like flavor, use short original riddles or reported rumors, not paraphrased blocks from the novels or anime.
- **Expose uncertainty carefully.** Characters in-world should disagree about Witches, Gospels, and Authorities. This keeps the lore mysterious and avoids presenting one fan interpretation as canon.

## Source list

### Primary author-hosted web novel

- Work index: [https://ncode.syosetu.com/n2267be/](https://ncode.syosetu.com/n2267be/)
- Chapter 6: [https://ncode.syosetu.com/n2267be/6/](https://ncode.syosetu.com/n2267be/6/)
- Chapter 9: [https://ncode.syosetu.com/n2267be/9/](https://ncode.syosetu.com/n2267be/9/)
- Chapter 15: [https://ncode.syosetu.com/n2267be/15/](https://ncode.syosetu.com/n2267be/15/)
- Chapter 16: [https://ncode.syosetu.com/n2267be/16/](https://ncode.syosetu.com/n2267be/16/)
- Chapter 39: [https://ncode.syosetu.com/n2267be/39/](https://ncode.syosetu.com/n2267be/39/)
- Chapter 47: [https://ncode.syosetu.com/n2267be/47/](https://ncode.syosetu.com/n2267be/47/)
- Chapter 58: [https://ncode.syosetu.com/n2267be/58/](https://ncode.syosetu.com/n2267be/58/)
- Chapter 64: [https://ncode.syosetu.com/n2267be/64/](https://ncode.syosetu.com/n2267be/64/)
- Chapter 68: [https://ncode.syosetu.com/n2267be/68/](https://ncode.syosetu.com/n2267be/68/)
- Chapter 71: [https://ncode.syosetu.com/n2267be/71/](https://ncode.syosetu.com/n2267be/71/)
- Chapter 74: [https://ncode.syosetu.com/n2267be/74/](https://ncode.syosetu.com/n2267be/74/)
- Chapter 126: [https://ncode.syosetu.com/n2267be/126/](https://ncode.syosetu.com/n2267be/126/)
- Chapter 140: [https://ncode.syosetu.com/n2267be/140/](https://ncode.syosetu.com/n2267be/140/)
- Chapter 141: [https://ncode.syosetu.com/n2267be/141/](https://ncode.syosetu.com/n2267be/141/)
- Chapter 145: [https://ncode.syosetu.com/n2267be/145/](https://ncode.syosetu.com/n2267be/145/)
- Chapter 155: [https://ncode.syosetu.com/n2267be/155/](https://ncode.syosetu.com/n2267be/155/)
- Chapter 161: [https://ncode.syosetu.com/n2267be/161/](https://ncode.syosetu.com/n2267be/161/)
- Chapter 165: [https://ncode.syosetu.com/n2267be/165/](https://ncode.syosetu.com/n2267be/165/)
- Chapter 178: [https://ncode.syosetu.com/n2267be/178/](https://ncode.syosetu.com/n2267be/178/)
- Chapter 179: [https://ncode.syosetu.com/n2267be/179/](https://ncode.syosetu.com/n2267be/179/)
- Chapter 240: [https://ncode.syosetu.com/n2267be/240/](https://ncode.syosetu.com/n2267be/240/)
- Chapter 241: [https://ncode.syosetu.com/n2267be/241/](https://ncode.syosetu.com/n2267be/241/)

### Official original-work and anime pages

- Official original-work portal: [https://re-zero.com/](https://re-zero.com/)
- Official original-work character page: [https://re-zero.com/character/](https://re-zero.com/character/)
- Official original-work book/synopsis page: [https://re-zero.com/books/](https://re-zero.com/books/)
- Official anime portal: [https://re-zero-anime.jp/](https://re-zero-anime.jp/)
- Official anime character page: [https://re-zero-anime.jp/tv/character/](https://re-zero-anime.jp/tv/character/)
- Official anime story page: [https://re-zero-anime.jp/tv/story/](https://re-zero-anime.jp/tv/story/)
