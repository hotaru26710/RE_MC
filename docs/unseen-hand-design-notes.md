# Unseen Hand / Invisible Providence: Re:Zero Research and Minecraft Forge Design Notes

Research date: 2026-10-01

## Scope and source policy

This note prioritizes primary and official material:

- Tappei Nagatsuki's author-hosted Re:Zero web novel on Shousetsuka ni Narou.
- The official Re:Zero original-work portal.
- The official TV anime character page.
- Yen Press, the licensed English publisher, for English-series identity where relevant.

No general-purpose fan wiki was used. The author-hosted web novel and later published light novels can diverge in detail, and the web novel is still serialized. Claims below are therefore anchored to specific Narou episode numbers and marked ambiguous where the text does not provide a clean specification. Episode numbers refer to the Narou web-novel URLs, not necessarily the same numbering as printed light-novel chapters.

No game code was edited for this research.

## Short answer

The Unseen Hand is the most recognizable offensive manifestation of the Sloth Authority. The known named users are Petelgeuse Romanée-Conti, Sin Archbishop of Sloth, and Subaru Natsuki, who inherits the Sloth Witch Factor after Petelgeuse's death and calls his own version **Invisible Providence**. Petelgeuse's version is a mass of invisible black arms or palms that can strike, bind, crush, throw, and kill. Subaru's early version is much weaker: normally one invisible arm, limited force and fine control, and severe backlash. Non-users normally cannot see the hands; Subaru is an exceptional perceiver and can see Petelgeuse's Unseen Hand. The power is not ordinary magic or spirit arts, and the primary text does not show a conventional mana cost. For gameplay, the safest adaptation is a short-range, single-hand, utility-control power with clear impact cues, interruptibility, cooldowns, Spirit loss, and Witch Scent as residual corruption/attention rather than literal canon fuel.

## Canon anchors

### Identity, origin, and who can use it

- The official anime character page identifies Petelgeuse as a Witch Cult executive and Sin Archbishop of Sloth, says he worships the Witch of Envy, and states that he "controls the Unseen Hand" and repeatedly opposes Subaru. ([Official anime character page](https://re-zero-anime.jp/tv/character/))
- The official original-work portal describes Petelgeuse as an executive of the Witch Cult, the Sin Archbishop of Sloth, and a fanatic devoted to the Witch of Envy. ([Official original-work portal](https://re-zero.com/character/))
- Petelgeuse calls the power the "Authority of Sloth: Unseen Hand." ([Narou episode 115](https://ncode.syosetu.com/n2267be/115/))
- Subaru inherits the Sloth Witch Factor after killing Petelgeuse. That inheritance is what he identifies as the origin of his own invisible hand. ([Narou episode 375](https://ncode.syosetu.com/n2267be/375/))
- Subaru's early use is definitively weaker than Petelgeuse's: it has less power, fewer hands, and initially only one hand. ([Narou episode 286](https://ncode.syosetu.com/n2267be/286/))
- The Authority is not treated as ordinary magic or spirit arts. Wilhelm and Subaru discuss it as a technique distinct from both, and Subaru compares it more closely to psychokinesis than to a known spell category. ([Narou episode 145](https://ncode.syosetu.com/n2267be/145/))
- Subaru later calls his version **Invisible Providence** to avoid bad public associations with the name "Unseen Hand." The Japanese web novel spells this in katakana as インビジブル・プロヴィデンス. ([Narou episode 284](https://ncode.syosetu.com/n2267be/284/), [episode 286](https://ncode.syosetu.com/n2267be/286/))

**Design conclusion:** treat the power as an Authority or Witch Factor phenomenon, not as a normal spell-school unlock. It should be rare, identity-defining, and dangerous. A normal player should not be able to learn it from a generic spellbook.

### What it looks like

- Petelgeuse's Unseen Hand is repeatedly described as black arms or black palms emerging from his shadow or spreading from his body. It can form multiple limbs, a mass of grasping arms, or a larger combined palm. ([Narou episodes 145](https://ncode.syosetu.com/n2267be/145/), [150](https://ncode.syosetu.com/n2267be/150/), [155](https://ncode.syosetu.com/n2267be/155/))
- The hands are weaponized black force, not ordinary limbs. The text describes them as invisible, black, capable of fatal wounds, and able to lift or throw large targets. ([Narou episode 155](https://ncode.syosetu.com/n2267be/155/), [episode 150](https://ncode.syosetu.com/n2267be/150/))
- Subaru's Invisible Providence manifests as a black palm from the center of his chest. Only Subaru can see it. It can pass through an ally without harming them when contact is unintended, and it disperses when Subaru cuts the connection. ([Narou episode 414](https://ncode.syosetu.com/n2267be/414/))
- In the web novel, the hand is not visually absent to its user. It is a black, shadow-like construct to the user; it is simply invisible to everyone else. ([Narou episode 414](https://ncode.syosetu.com/n2267be/414/))

**Design conclusion:** use a black hand silhouette for the caster's local client, but do not render the full hand for other players by default. Other players should see impact points, target reactions, dust, sound, or a brief pressure cue rather than the hand itself.

### Number, range, strength, and scaling

- Petelgeuse's hand count is not fixed. During one exchange the count rises through thirteen, fourteen, and fifteen while becoming poorly coordinated after damage. ([Narou episode 145](https://ncode.syosetu.com/n2267be/145/))
- In another phase, the quantity of hands pouring out is described as effectively without a visible limit, and the hands attack from multiple directions. ([Narou episode 150](https://ncode.syosetu.com/n2267be/150/))
- A later recollection describes the Unseen Hand as powerful enough to reach where a normal hand cannot reach, touch what cannot normally be touched, and strike enemies that should be out of reach. No hard numerical range is supplied. ([Narou episode 289](https://ncode.syosetu.com/n2267be/289/))
- Petelgeuse can grab and throw a land dragon by the tail, crush a spirit with a combined palm, and deliver fatal tearing or crushing attacks. ([Narou episodes 127](https://ncode.syosetu.com/n2267be/127/), [150](https://ncode.syosetu.com/n2267be/150/))
- Subaru's early Invisible Providence is much weaker: one hand, less force, and utility-level fine manipulation such as wiping tears or touching a head is already difficult. A full strike can still knock Garfiel back, but it exhausts Subaru. ([Narou episodes 282](https://ncode.syosetu.com/n2267be/282/), [286](https://ncode.syosetu.com/n2267be/286/), [414](https://ncode.syosetu.com/n2267be/414/))
- Subaru's later use can restrain limbs, grab a target by the neck, snap bone, catch or break a weapon, and strike with enough force to matter against strong enemies. It remains far below Petelgeuse's multi-hand storm. ([Narou episodes 417](https://ncode.syosetu.com/n2267be/417/), [419](https://ncode.syosetu.com/n2267be/419/))

**Design conclusion:** do not define the power by a "correct" canon number. Define a player baseline as one hand, then let bosses or late-story variants use multiple hands. Keep the player's range deliberately modest so invisibility does not become unavoidable cross-map control.

### Limitations and counterplay

- The hands are invisible but not invincible. A perceiver can dodge or intercept them, and Petelgeuse is shocked when Subaru can see his Unseen Hand. ([Narou episodes 126](https://ncode.syosetu.com/n2267be/126/), [127](https://ncode.syosetu.com/n2267be/127/))
- Subaru can relay his view through the Nekto sense-sharing spell so that another fighter can act against the hand he sees. This is evidence that visibility and coordination are meaningful counters. ([Narou episode 337](https://ncode.syosetu.com/n2267be/337/))
- Petelgeuse's control degrades when he is physically damaged or mentally disrupted; the hands become uncoordinated and can miss. ([Narou episode 145](https://ncode.syosetu.com/n2267be/145/))
- Subaru's hand disappears if he loses the connection, is interrupted, or exceeds his limit. Overuse can turn a controlled strike into an immediate collapse. ([Narou episodes 414](https://ncode.syosetu.com/n2267be/414/), [417](https://ncode.syosetu.com/n2267be/417/), [419](https://ncode.syosetu.com/n2267be/419/))
- The primary text does not provide a universal anti-magic defense that trivially blocks the Authority. Because it is outside normal magic and spirit arts, an ordinary "dispel magic" counter is not a clean canon solution. ([Narou episode 145](https://ncode.syosetu.com/n2267be/145/))
- The exact maximum number, maximum range, and behavior against every kind of barrier are not fully specified. Treat those as design parameters, not settled lore facts.

### Mental and physical costs

- Subaru's Invisible Providence costs something the text calls soul, mind, or sanity; the narrative itself says Subaru cannot cleanly identify which. ([Narou episode 414](https://ncode.syosetu.com/n2267be/414/))
- Early use leaves him vomiting, gasping, losing strength, and feeling that a massive amount of something has been taken from him. ([Narou episode 282](https://ncode.syosetu.com/n2267be/282/))
- Strong use causes full-body pain, soul erosion, and black blood. One desperate strike is described as something he can only perform by paying that price. ([Narou episode 341](https://ncode.syosetu.com/n2267be/341/))
- Overuse causes intense head pain, nosebleeds, loss of consciousness, and the hand collapsing before the intended action can finish. ([Narou episodes 417](https://ncode.syosetu.com/n2267be/417/), [419](https://ncode.syosetu.com/n2267be/419/))
- The aftereffect includes a feeling of loss or emptiness that remains after the hand is gone. ([Narou episode 414](https://ncode.syosetu.com/n2267be/414/))
- For Petelgeuse, the text says an Authority this difficult to control could destabilize mental balance. It does not isolate whether his madness comes from the Authority, the Witch Factor, his own fanaticism, or all three. Mark this as ambiguous rather than treating the Unseen Hand as a simple insanity spell. ([Narou episode 151](https://ncode.syosetu.com/n2267be/151/))

### Relation to the Sloth Authority

- The Unseen Hand is explicitly called the Authority of Sloth. ([Narou episode 115](https://ncode.syosetu.com/n2267be/115/))
- The Sloth Authority is portrayed as more than one attack. The web novel lists the invisible fatal hand, possession through prepared bodies, and battlefield-scale mental contamination as connected threats. ([Narou episode 150](https://ncode.syosetu.com/n2267be/150/), [episode 155](https://ncode.syosetu.com/n2267be/155/))
- Petelgeuse's mental contamination can inflict hallucinations, phantom sounds, phantom pain, foaming, and near-madness on unprotected people. This is not the same mechanic as the physical Unseen Hand. ([Narou episode 150](https://ncode.syosetu.com/n2267be/150/), [episode 155](https://ncode.syosetu.com/n2267be/155/))
- Witch Factors are transferable. Subaru receives the Sloth Witch Factor from Petelgeuse's death, and his Invisible Providence is his own expression of that inherited Authority. ([Narou episode 375](https://ncode.syosetu.com/n2267be/375/))
- The exact rules by which a Witch Factor selects, transforms, or limits a new host remain mysterious. Do not present those rules as a finished canon system.

### Can non-users perceive it?

- Normally, no. Non-users do not see the hand itself. The text emphasizes that the Unseen Hand is invisible to others and that Subaru's own black palm is not visible even to Beatrice or enemies. ([Narou episodes 127](https://ncode.syosetu.com/n2267be/127/), [414](https://ncode.syosetu.com/n2267be/414/))
- Subaru is the major exception: he can see Petelgeuse's Unseen Hand, and Petelgeuse regards this as abnormal and intolerable. ([Narou episodes 126](https://ncode.syosetu.com/n2267be/126/), [127](https://ncode.syosetu.com/n2267be/127/))
- Non-users can still perceive the results: a body is pulled, a neck is crushed, a weapon breaks, a target is thrown, or the user suddenly suffers backlash. Those effects are visible even when the hand is not. ([Narou episodes 150](https://ncode.syosetu.com/n2267be/150/), [417](https://ncode.syosetu.com/n2267be/417/), [419](https://ncode.syosetu.com/n2267be/419/))
- The anime may show black hands to the audience for dramatic clarity. That should not be read as proof that ordinary characters in-world can see them.

## Minecraft-safe adaptation

### What is safe to adapt

Safe mechanical inspiration:

- An invisible force that can strike, pull, lift, or restrain at short range.
- A visible black hand only for the caster's client.
- Strong costs, interruption, and a clear escalation into backlash.
- Multiple hands only for a boss or a late-story transformation.
- A "perception" exception such as Witch Factor sensitivity, a ritual, a special eye item, or shared vision.
- Counterplay through line of sight, wards, damage interruption, cooldowns, boss tags, and protected regions.

Risky or unsafe to adapt directly:

- A fully invisible, no-warning, instant-kill ability.
- Unlimited hands with no per-tick cap or server-side performance budget.
- Permanent insanity, permanent player control, or forced possession without consent.
- Invisible PvP hard-control lasting more than a moment.
- Grabbing through claimed land, protected regions, spawn protection, or boss invulnerability.
- Copying anime/model/text assets, iconic scene compositions, or long translated passages.
- Presenting Witch Scent as canon fuel. The primary text does not establish that Unseen Hand consumes Witch Scent; it is better used as an adaptation-side attention/corruption cost.

For public or commercial distribution, use caution with Re:Zero names, character likenesses, and copied visual designs unless the project has the necessary rights. The mechanical idea of invisible telekinetic force is broadly reusable; the specific expression and branding are the risky part.

## Concrete Forge gameplay proposal

### Item: Sloth Factor Reliquary

Working item name: **Sloth Factor Reliquary** (or an internal debug label such as `unseen_hand_focus`).

- Acquisition: a one-time endgame drop from a Sloth Authority boss or a dangerous Witch Cult ritual. Not craftable from ordinary materials.
- Binding: soulbound or character-bound by config; only one active holder per player.
- Use: right-click to cast the selected mode; sneak-right-click to cycle modes; attack while holding the item to select Strike without casting only if the input system supports it cleanly.
- Presentation: the caster sees a black arm from the chest anchor. Other players see only a brief whoosh, target reaction, dust, and impact particles.

### Core modes

| Mode | Range | Cooldown | Damage/effect | Control | Spirit cost | Witch Scent cost | Main failure |
|---|---:|---:|---|---|---:|---:|---|
| Strike | 10 blocks | 8 s | 6 HP, strong knock-up | 0.5 s stagger | 18 | +2 | No line of sight; protected target |
| Grip | 8 blocks | 12 s | 2 HP over the hold | Lift/root 1.2 s, pull 3 blocks | 30 | +4 | Boss tag or control immunity |
| Catch/Redirect | 6 blocks | 20 s | No damage | Catch a falling ally, pull an ally out of danger, or catch a falling item | 24 | +3 | Protected entity or unloaded chunk |
| Desperate Multi-Hand | 12 blocks | 35 s | 6 HP to up to three targets | 0.8 s stagger only | 45 | +10 | Adds three Overreach stacks; can collapse caster |

Recommended defaults:

- PvP: disabled by default.
- Friendly fire: disabled.
- Bosses: no pull; control duration reduced to 0.4 s.
- Players: no hard control longer than 1.0 s unless the server explicitly enables a PvP variant.
- Protection: no block breaking, container access, claim bypass, or interaction with protected blocks.
- Server safety: maximum two active hand targets and a hard tick budget; do not force-load chunks.

### Spirit cost

Use the mod's existing Spirit meter as psychological and spiritual stability, not as a literal Re:Zero mana pool.

- Require at least 30 Spirit for Strike and Grip.
- Spend the listed Spirit cost on successful cast, not on invalid targeting.
- If Spirit drops below 25 after a cast, apply one Overreach stack.
- If Spirit is already below the required value, the cast fails cleanly but does not kill the player.
- Optional hard-mode rule: a sneak-right-click "desperate cast" can ignore the Spirit requirement, but immediately applies two Overreach stacks and a short collapse effect.

### Witch Scent cost

Witch Scent should represent attention, corruption, and the danger of being noticed, not a canon fuel source.

- Add the listed Witch Scent on each successful cast.
- At 75+ Witch Scent, increase Spirit cost by 25% and increase hostile detection radius.
- At 90+ Witch Scent, a failed cast can create a short burst of darkness, nausea, and hostile mob attraction.
- Provide only partial cleansing through rare wards, rituals, or safe zones. Do not allow a cheap item to erase the cost completely.

### Overreach and failure modes

| Failure mode | Trigger | Result |
|---|---|---|
| Lost grip | Caster takes damage during a channel | Hand disperses; half Spirit cost is paid; cooldown is half-refunded |
| Nullified | Target is in a protected area, behind a ward, or tagged immune | No hand spawns; small cooldown; no Spirit cost |
| Disconnection | Chunk unloads, target despawns, or server cancels the cast | Hand disperses; half Spirit cost; small cooldown |
| Spiritual collapse | Spirit reaches 0 or the caster uses a desperate cast at low Spirit | Nausea, Darkness, Weakness, and Slowness for 5-8 s; item locks for 60 s |
| Overreach I | One stack | Cooldowns +25%; Spirit recovery -25% for 30 s |
| Overreach II | Two stacks | Random cast failure; stronger visual distortion; Witch Scent +5 |
| Overreach III | Three stacks | Immediate collapse, item disabled for 60-90 s, Witch Scent +10; no instant death |
| Public reveal | High Witch Scent plus a witnessed use | Cultists, spirits, or special NPCs may treat the player as a Witch Factor bearer |
| PvP abuse | Repeated use on a player | Escalating cooldown and diminishing control duration |

### Counterplay for non-users

The hand should be invisible, but the mechanic must still be fair.

- Show a brief pressure or distortion cue at the impact point, not the full hand.
- Use sound cues: a low whoosh, a sharp crack, or a muffled pressure thump.
- Let damage interrupts cancel channels.
- Let wards, sealing materials, boss arenas, and protection zones nullify the effect.
- Let Witch Factor perception or shared vision reveal the hand to a specialized counter unit.
- Let high-mobility targets escape Grip if they move out of range before the hold begins.
- Keep the first attack telegraphed enough that a player can learn the sound or cue pattern.

### Boss/Sin Archbishop variant

If Petelgeuse or a Sloth Archbishop is implemented, use a separate boss profile:

- Baseline: 12-18 hands, 18-24 block reach, slower wind-up, deadly damage.
- Phase 2: hand count increases, coordination drops, attacks come from more directions.
- Mental contamination: an area pressure that causes harmless hallucinations before it causes real debuffs.
- Possession: a phase or ritual mechanic involving marked cultists, not an unavoidable instant takeover.
- Perception counter: a quest item, shared vision, or Witch Factor sensitivity lets the raid see or predict the hands.
- Performance cap: the server should still enforce a maximum number of active hand entities or simulated targets.

## Recommended design rule

**Player version:** one hand, short range, control and utility first, severe Spirit/Scent cost, clear interruption and collapse states.

**Boss version:** many hands, longer reach, lethal pressure, but slow phases and an explicit perception counter.

**Lore fidelity:** the hand is invisible to other people, tied to the Sloth Authority and Witch Factor, outside ordinary magic, and paid for through the user's mind, body, and soul.

**Minecraft fairness:** invisibility should remove perfect information from opponents, not remove all counterplay.

## Ambiguities to preserve

- Exact maximum hand count.
- Exact maximum range.
- Whether every Sloth Witch Factor host experiences identical costs.
- Whether Petelgeuse's insanity is caused by the Authority, the Witch Factor, his beliefs, or a combination.
- Whether specialized magic, spirits, or sealing materials can detect or block the hand.
- How much the anime's visible black hands represent audience presentation versus in-world perception.

These should remain configurable or intentionally mysterious rather than being asserted as fixed canon.

## Sources

### Primary: author-hosted web novel

- Series table of contents: [Shousetsuka ni Narou, `n2267be`](https://ncode.syosetu.com/n2267be/)
- Episode 115: first explicit naming of the Sloth Authority / Unseen Hand: [link](https://ncode.syosetu.com/n2267be/115/)
- Episode 126: Subaru sees the hand; Petelgeuse treats that as impossible and forbidden: [link](https://ncode.syosetu.com/n2267be/126/)
- Episode 127: Subaru-only perception; Puck cannot see the hand; crushing strength: [link](https://ncode.syosetu.com/n2267be/127/)
- Episode 144-145: hand count rising beyond fifteen; coordination degrades; not magic or spirit arts: [episode 145](https://ncode.syosetu.com/n2267be/145/)
- Episode 150: no visible limit on hand quantity; multi-directional attacks; battlefield-scale mental contamination: [link](https://ncode.syosetu.com/n2267be/150/)
- Episode 151: difficulty controlling the Authority and possible mental destabilization: [link](https://ncode.syosetu.com/n2267be/151/)
- Episode 155: summary of the threat package: invisible fatal hand, possession, mental contamination: [link](https://ncode.syosetu.com/n2267be/155/)
- Episode 282: Subaru's first desperate manifestation; one hand; severe physical collapse: [link](https://ncode.syosetu.com/n2267be/282/)
- Episode 284: Subaru names Invisible Providence: [link](https://ncode.syosetu.com/n2267be/284/)
- Episode 286: comparison with Petelgeuse; one hand; Witch Factor origin: [link](https://ncode.syosetu.com/n2267be/286/)
- Episode 289: reach and force beyond ordinary physical reach; no hard range given: [link](https://ncode.syosetu.com/n2267be/289/)
- Episode 337: Authority as outside known magical categories; Subaru's acquired Invisible Providence: [link](https://ncode.syosetu.com/n2267be/337/)
- Episode 341: soul erosion and black-blood backlash from a strong strike: [link](https://ncode.syosetu.com/n2267be/341/)
- Episode 375: Subaru identifies the Sloth Witch Factor inherited from Petelgeuse: [link](https://ncode.syosetu.com/n2267be/375/)
- Episode 414: black palm from chest; visible only to Subaru; can pass through an ally; soul/sanity cost: [link](https://ncode.syosetu.com/n2267be/414/)
- Episode 417: restraint, lethal grip, and overuse backlash: [link](https://ncode.syosetu.com/n2267be/417/)
- Episode 419: targeted strike, crushing grip, and extreme migraine collapse: [link](https://ncode.syosetu.com/n2267be/419/)

### Official: Re:Zero original-work portal

- Character page: [official original-work portal](https://re-zero.com/character/)
  - Used for Petelgeuse's Witch Cult affiliation, Sin Archbishop of Sloth identity, and fanatical devotion to the Witch of Envy.

### Official: TV anime portal

- Character page: [official TV anime character page](https://re-zero-anime.jp/tv/character/)
  - Used for the official anime statement that Petelgeuse controls the Unseen Hand and repeatedly opposes Subaru.

### Licensed English publisher

- Series page: [Yen Press, Re:ZERO -Starting Life in Another World-](https://yenpress.com/series/re-starting-life-in-another-world)
- Chapter 3 manga page: [Yen Press, Re:ZERO Chapter 3: Truth of Zero](https://yenpress.com/series/re-starting-life-in-another-world-chapter-3-truth-of-zero-manga)
  - Used only to verify the licensed English title and identify the Chapter 3 antagonist as the Archbishop of Sloth, Petelgeuse.

## Final design recommendation

Build the player ability as **one invisible black hand with a short range, a long cooldown, limited control, and a severe stability cost**. Keep the multi-hand swarm and lethal reach for a boss or a scripted late-game transformation. Let non-users see the consequences but not the hand. Let high Witch Scent attract attention and create failure pressure. Let Spirit loss, interruption, and overreach create the Re:Zero-like feeling that using the Authority is not free, even when it works.