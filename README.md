# Disabilities Plus

An **add-on** for the Fabric mod [Disabilities](https://modrinth.com/mod/disabilities) (v0.5.0, Minecraft 26.3).
It does not replace the original: install both jars and you get the original's 30 disabilities plus **82 more**,
shown together in one list, handled by the same `/disability` command, and drawn from the same random pool.

> The original mod is a required dependency and is **not** bundled here. This repository contains only the
> add-on's own code. (The original jar in `libs/` is a local compile/run dependency; do not redistribute it.)

## Requirements

Minecraft 26.3, Fabric Loader 0.19.5+, Fabric API, Java 25, and the original **Disabilities 0.5.0+26.3**.
The server and every client need both jars; a client without the add-on is refused with a clear message.

## What it adds

All the add-on's names work with `/disability give|remove <players> <name>` and tab-complete next to the originals.

| Id | Effect |
|---|---|
| `tinnitus` | A high ring plays and all non-voice sound is dulled (x0.65), until you stand still for 2 seconds and can hear |
| `night_blindness` | Poorly lit places are heavily dimmed (up to 80%); a torch, lantern or other light in either hand cuts the dimming to a fifth |
| `tunnel_vision` | Field of view narrowed (x0.62) with a dark vignette |
| `anemia` | Your blood is thin: only half of anything you heal sticks, unless you ate red meat or beetroot in the last five minutes |
| `migraine` | Random 12-20 s episodes: pulsing white-out, camera wobble, sound x0.45 |
| `amnesia` | Your chat history is wiped every 1-2.5 minutes |
| `acrophobia` | Near a 10+ block drop you freeze into a forced crouch with a pounding heart (which also stops you stepping off) |
| `claustrophobia` | Tight spaces make you hyperventilate: air drains in about 10 s, then damage; the view closes in |
| `hemophilia` | Damage starts a long bleed (1 damage every 2 s, up to 30 s); using a sheet of paper as a bandage stops it |
| `aphasia` | Outgoing chat words are replaced with filler or have their letters shuffled |
| `dyscalculia` | Digits in plain text are swapped (64 reads as 70) and item stack counts are unreadable |
| `photosensitivity` | You burn in direct daylight unless wearing a helmet |
| `motion_sickness` | Nausea when riding anything or moving very fast, unless you keep your eyes level on the horizon (within 15 degrees) |
| `ataxia` | Random sideways stumbles while you walk |
| `heart_condition` | About 15 s of sprinting causes 2 damage and slowness III |
| `autism` | Loud sounds within 32 blocks overload you for 6 s: the screen dims and sound is muffled |
| `bipolar` | Every 3-6 min your mood flips between a fast, driven high and a slow low |
| `butterfingers` | Roughly every 2 min you drop one of whatever you hold (not with Kleptomania) |
| `hangry` | Below 3 shanks of hunger mining is 25% slower and you are weak; nothing drains faster |
| `two_left_feet` | After a real jump or drop, 1 landing in 3 staggers you (sprint stops, slowness IV for 1.5 s) unless you land in a crouch |
| `creeperphobia` | A creeper within 8 blocks freezes you in bursts while a heartbeat quickens as it nears |
| `arachnophobia` | A spider within 8 blocks makes your head snap away from it |
| `left_right_confusion` | Random 10-20 s episodes where A and D swap |
| `hiccups` | Roughly every 25 s a hiccup jolts the camera and burps |
| `sneezing` | Roughly every 1-3 minutes a sneeze jerks the camera and sets every monster within 16 blocks on you |
| `picky_eater` | Whatever you just ate goes on a 30 s cooldown: you cannot eat the same thing twice in a row |
| `egg_layer` | Roughly once a minute you lay an egg |
| `moon_boots` | Gravity is 40% of normal; a hard landing bounces you back up like a slime block unless you land in a crouch |
| `giant` | Scale x1.5 and +1 reach; you do not fit through two-block gaps. A hard landing is a stomp: it hurts and shoves hostile mobs beside you, launches friends into the air unhurt, and wakes every monster within 12 blocks |
| `pogo` | You auto-jump about once a second |
| `tipsy` | The horizon rolls about 5 degrees each way like a deck at sea |
| `sweet_tooth` | Sweets give Speed II for 30 s, then a short crash |
| `scaredy_cat` | A hostile mob within 12 blocks sends you bolting (Speed II), but you cannot bring yourself to hit it |
| `glass_cannon` | Attack damage x2, half the health; a hit of 2 hearts or more cracks you (one heart less for 3 minutes, up to three cracks) |
| `squeaky_shoes` | You squeak every 1.5 blocks walked and each squeak draws hostile mobs within 12 blocks toward you; sneaking is silent |
| `quadruple_amputee` | No limbs: you cannot move, jump, drop, use, place, attack or break. **Half as likely** to be picked, and **dying cures it** |
| `pregnancy` | 10% slower, a wider belly, kicks every ~30 s; the baby craves a food: eating it gives 10 s of Regeneration II |
| `ginger` | The world is sepia |
| `obesity` | A wide body and a weaker jump; sprinting burns hunger faster, but nothing shoves you around while you sprint and a sprinting hit sends the target flying |
| `orphan` | Beds, anchors and `/spawnpoint` never stick: you always respawn at world spawn |
| `alcoholism` | A potion or honey bottle gives a buzz; about 4 min without brings withdrawal blackouts (Darkness) |
| `paranoia` | Footsteps right behind you about every 10 s, and an occasional flinch |
| `drug_addiction` | Golden food is the fix: strength and haste, then a weakness and fatigue crash; about 5 min without brings withdrawal |
| `blood_hungry` | Extra hunger drain; hitting a creature heals 2 HP and feeds you |
| `enderphobia` | An Enderman within 12 blocks closes your vision in with dread |
| `apiphobia` | A bee within 8 blocks buzzes at your ear and makes your view twitch |
| `aquaphobia` | In water you thrash upwards; in the rain you bolt for cover |
| `pyrophobia` | Fire or lava within 5 blocks pulls your gaze toward it |
| `thrill_seeker` | Near a drop you get Speed and Haste; in a cramped space, Mining Fatigue |
| `night_owl` | Slow and weak in bright light; fast with Night Vision in the dark |
| `hoarder` | 18+ used inventory slots: 10% slower but +2 armor; 28+: 25% slower but +6 armor |
| `head_rush` | Standing up from a crouch blacks your vision out for a second |
| `slippery_grip` | You fumble your tools: every hit you land and every block you break wears the held tool one extra use |
| `backwards_brain` | Random 10-20 s episodes where your mouse looks the wrong way, up and down |
| `heavy_eyelids` | Your lids creep shut over 30 s (black bars close in) until a jump, a swing or a crouch jolts you awake |
| `misophonia` | Eating and drinking sounds come back three times, loud and shrill, and make you flinch |
| `jumpy` | Loud sounds make you jump and flinch |
| `blabbermouth` | About every 2 min you blurt your coordinates into chat |
| `ibs` | Eating sounds set off cramps that double you over |
| `kidney_stones` | Every 3-6 min a stone passes: 5 s locked up in agony, 1 damage per second (never lethal). Drinking a potion or milk hydrates you for 3 min: the next stone is delayed and passes in 2 s painlessly |
| `stoned` | The field of view breathes, your eyes droop in slow blinks, and your swings are 30% slower |
| `lead_poisoning` | 1 heart less; each Lead in your pack costs another heart and poisons you. A bucket of milk flushes it for 5 minutes |
| `black_lung` | Mining underground fills your lungs; soot creeps in from the screen edges and past the limit you choke (1 damage per 2 s) until you reach open sky |
| `necrosis` | Every 5 min tissue dies: first your hands (mining -50%), then your legs (speed -50%), then your strength. A golden apple pushes it back a stage, an enchanted one cures it |
| `ptsd` | An explosion or being hit starts a 5 s flashback: dread, a pounding heart, a muffled world |
| `arfid` | Only a few safe foods stay down; anything else is handed back |
| `anorexia` | Food restores about a third, and while hungry you are frail |
| `alpha_gal` | Eating mammal meat starts a ~40 s timer, then anaphylaxis: swollen hands (mining fatigue III, weakness II), 4 damage (never below 1 heart), an emptied stomach |
| `b12_deficiency` | Animal foods and milk top up a supply that drains; empty, your legs are numb and cannot step up onto stairs or slabs (you must jump) |
| `covid_19` | An 8-minute illness: coughing fits and flushed fever spikes (weakness) after the first minute; spreads to players within 3 blocks |
| `erectile_dysfunction` | You cannot sustain it: using an item cuts out at random, more often the more living things are within 8 blocks |
| `ebola` | A 12-minute illness: 2 hearts of maximum health are lost every 45 s and you are weak; spreads to players within 2 blocks |
| `e_coli` | A 6-minute illness: raw food costs more hunger than it gives and drains the rest (Hunger II); a few seconds in water does the same |
| `mono` | A 10-minute illness: you nap (black screen, locked controls) every 1-2 minutes, and a hit of 3 hearts or more ruptures your spleen (4 damage and a blackout) |
| `baldness` | No hair layer on your head; without a helmet, cold biomes and rain chill your scalp until you freeze |
| `dysgraphia` | What you write is miswritten in text boxes, signs and books: b/d and p/q flip, letters swap and drop (chat and commands are left alone) |
| `evil` | Every hostile and neutral mob within 40 blocks is set on you, and neutral ones stay angry |
| `social_anxiety` | Being watched builds panic (3 per second per watcher, up to four): at 30 your hands shake (mining -20%, a trembling view), at 60 you are slowed, cannot sprint and **cannot send chat**, at 90 you freeze for 3 s. Watchers are players facing you with a clear line of sight (alone: hostile mobs and villagers). A friend within 2.5 blocks calms you, a helmet halves the count and a carved pumpkin hides you |
| `alien_hand` | Every 20-60 s your offhand acts alone: it slaps the nearest creature for 2 (friends, pets and villagers included, and it is a real hit), drags you toward the nearest person, or swaps what is in your hands. Holding something in the offhand makes it three times rarer and halves the slap |
| `cataplexy` | Strong emotion drops you for 4 seconds, fully awake: your knees buckle, you cannot use your hands. Triggers: laughter in chat (lol, haha, xd...; yours or anyone's), mining diamonds, emeralds or ancient debris, and now and then a monster kill. Crouching (or sitting) braces you: you only wobble |
| `pain_insensitivity` | No health bar. Any hit of 3+ leaves a wound you do not feel, 1 damage every 2 seconds for 10 s (stacking to 30 s), until you are checked over. Friends within 30 blocks are warned when you are badly hurt; a friend's empty-hand right-click checks you, and you can check yourself by crouching still for 3 seconds |
| `ehlers_danlos` | A hit of 2 hearts or more, or a fall of 3 blocks or more, dislocates an arm (no hitting or mining) or a leg (half speed, no jumping). A friend with an empty hand pops it back (1 damage), or crouch still for 6 seconds (2 damage) |

### Notes on the added behaviour

* **Illnesses** (`covid_19`, `ebola`, `e_coli`, `mono`) run their course and then remove themselves; recovering grants ten minutes of immunity from catching the same thing again. Dying cures them. Spreading only happens between players (COVID-19 10%/s, Ebola 15%/s while close).
* **Dyslexia and stack counts**: the original Dyslexia scrambled item counts along with all other text. In this add-on counts stay readable under Dyslexia alone and become unreadable under **Dyscalculia** instead, which inherits that effect. Other text stays Dyslexia's.
* **Dysgraphia** affects what you write (text boxes, signs, books), not what you read. Speaking in chat is **Aphasia**'s.

## What changed in 1.3.0: gimmicks with counterplay (and company)

The plainest stat-nerf disabilities now have a mechanic you can play around, so they are hard rather than just annoying.

| Disability | New gimmick |
|---|---|
| Anemia | Iron-rich food (red meat, beetroot) lifts the halved healing for 5 minutes |
| Glass Cannon | Hits of 2 hearts or more crack you: a heart less for 3 minutes, stacking to three |
| Giant | A hard landing stomps: hurts and shoves hostile mobs beside you, but wakes every monster within 12 blocks |
| Moon Boots | A hard landing bounces you back up unless you land in a crouch |
| Hoarder | Bulk is padding: +2 armor at 18 slots, +6 at 28, in exchange for the speed |
| Obesity | While sprinting nothing shoves you around, and a sprinting hit sends the target flying |
| Squeaky Shoes | Squeaks draw hostile mobs toward you, so sneaking becomes a stealth tool |
| Night Blindness | A torch or lantern in hand pushes the dark back |
| Tinnitus | Stand still for 2 seconds and the ringing stops |
| Pregnancy | The baby craves a food: feed it for Regeneration II |
| Hemophilia | A sheet of paper is a bandage |
| Kidney Stones | Drinking keeps you hydrated: the next stone is delayed, short and painless |
| Lead Poisoning | A bucket of milk flushes the lead for 5 minutes |
| Necrosis | Golden apples push the rot back a stage, enchanted ones cure it |
| Motion Sickness | Keep your eyes on the horizon and you stay well |
| Erectile Dysfunction | The more eyes on you, the more often it cuts out |
| Two Left Feet | Land in a crouch and you roll through it |

## What changed in 1.5.0: five real conditions

These replace the five invented ones in 1.4.0 (Loneliness, Empath, Wanted, Hot Potato, Tattletale). Each is a real condition, works alone, and is much better with other people.

| Condition | What it is | Alone | With others |
|---|---|---|---|
| Social Anxiety | Distress at being observed | Hostile mobs and villagers that look at you count | Players looking at you; a close friend calms you; chat locks up |
| Alien Hand Syndrome | A limb that acts without you | It slaps whatever is nearest | It slaps your friends, and drags you into them |
| Cataplexy | Emotion-triggered muscle weakness | Finds and kills drop you | Laughter in chat drops you: friends can do it to you on purpose |
| Insensitivity to Pain | No pain, so no warning | Wounds go unnoticed; check yourself by crouching still | Friends are your eyes: they are warned and can check you |
| Ehlers-Danlos Syndrome | Joints that dislocate | You pop it back yourself, slowly | A friend does it at once |

### Playing with company

| Disability | With friends |
|---|---|
| Anemia, Pregnancy, Kidney Stones, Lead Poisoning, Necrosis | Right-click the afflicted player with food, a potion or milk to feed them: it counts as them eating it (iron-rich meat, the craved food, water, milk, a golden apple) |
| Quadruple Amputee | A friend can feed you, and carry you: right-click with an empty hand, and you ride on their back until they crouch |
| Social Anxiety | Stand next to them and it eases; crowds and attention are what set it off |
| Alien Hand Syndrome | Keep your distance: it slaps whoever is nearest, and pulls you toward them |
| Cataplexy | Telling a joke is an attack; crouching beside them is protection |
| Insensitivity to Pain | A right-click with an empty hand checks them, and you are warned when they are hurt |
| Ehlers-Danlos Syndrome | A right-click with an empty hand pops their joint back in |
| Hemophilia | Right-click a bleeding player with paper to bandage them |
| Glass Cannon | Right-click a cracked player with a block or pane of glass to patch one crack |
| Giant | The stomp launches nearby friends into the air, unhurt |
| Moon Boots | Your bounce lifts players within 3 blocks too |
| Hoarder | Players within 6 blocks share half of your padding as armor |
| Squeaky Shoes | Everyone nearby hears your squeak (your stealth is theirs) |
| Night Blindness | A friend's torch or lantern within 6 blocks lights you too |
| Tinnitus | The ringing eases when another player is within 3 blocks |
| Two Left Feet | A friend within 2.5 blocks steadies you: no stagger |
| Erectile Dysfunction | People watching count double when it comes to how often it cuts out |

## What changed in 1.2.0: every disability is its own thing

Pairs that did the same thing were given their own mechanics. The one-line rule: no two disabilities share both a
trigger and an effect.

| Disability | Was | Now |
|---|---|---|
| Anemia | slower, weaker, sprint hunger (same as Obesity/Pregnancy) | only half of anything you heal sticks |
| Obesity | slower + weaker jump + sprint hunger | weaker jump, wide body, sprint hunger (no slowdown) |
| Pregnancy | slower + extra hunger + kicks | slower + belly + kicks (no extra hunger) |
| Hangry | extra hunger drain (same as Blood Hungry) | no drain: weak and slow at mining below 3 shanks |
| Stoned | sway, dull sound, munchies drain | slow swings (attack speed), breathing FOV, droopy blinks |
| Autism / Misophonia | both dimmed the screen and muffled sound | Autism keeps that; Misophonia's chewing sounds come back shrill, three times |
| PTSD | flashback used Autism's screen dim | dread, heartbeat, freeze and muffled sound only |
| Sneezing vs Hiccups | both jolted the camera | sneezes are timed by the server and wake nearby monsters |
| Backwards Brain vs Left-Right Confusion | both swapped movement keys | mouse look inverted up/down |
| Slippery Grip vs Butterfingers | both dropped the held item | tools wear one extra use per hit and per block |
| Heavy Eyelids vs Head Rush | both blacked the screen | lids creep shut until you jump, swing or crouch |
| Tipsy vs Ataxia | both made the walking path weave | the horizon rolls (camera roll) |
| Lead Poisoning vs Parkinson's | trembling crosshair | max health and Lead-in-pack poison only |
| Alcoholism vs Drug Addiction | same craving shape and withdrawal | alcohol withdrawal is Darkness blackouts; drugs give strength + haste then weakness and fatigue |
| Sweet Tooth vs Drug Addiction | both: speed rush then slow crash | Sweet Tooth keeps speed rush / slowness crash; drug rush is strength + haste |
| Heart Condition vs Two Left Feet | both: sprint for a while, then slowness | Two Left Feet staggers when you land a jump |
| Picky Eater vs Motion Sickness | both gave nausea | the food you just ate goes on cooldown |
| Scaredy Cat vs Paranoia | both played phantom sounds | monsters send you running (Speed II) but you cannot hit them |
| Black Lung vs COVID-19 | both coughed and froze you | soot creeps in from the edges and you choke past the limit |
| Kidney Stones vs IBS | both doubled you over | kidney stones lock you up in agony (IBS still doubles you over) |
| Anorexia vs COVID-19 | both cut what food gives | COVID-19 has fever spikes instead; anorexia keeps the cut |
| Ebola vs Hemophilia | both bled | Ebola is wasting and weakness only |
| Mono vs Hemophilia | spleen rupture bled | a heavy internal blow and a blackout |
| E. coli vs Alpha-gal vs IBS | all doubled you over | E. coli drains hunger (Hunger II); Alpha-gal swells your hands |
| B12 Deficiency vs the other slow ones | -10% speed plus random freezes | numb legs cannot step up stairs or slabs |
| Necrosis vs Amputated Offhand/Leg | cleared the offhand, slowed a leg | hands (mining), then legs (speed), then strength |
| Dysgraphia vs Aphasia | both garbled outgoing chat | Dysgraphia garbles text boxes, signs and books; Aphasia keeps chat |

## Commands

The add-on extends the original `/disability` command (nothing is replaced):

* `give` / `remove <players> <name|all|random>` - names, `all` and `random` now cover both mods
* `next [players]` and `list [players]` - include the add-on's disabilities
* `deathremoval [0-100]` - how many random disabilities (from either mod) each death lifts; default **0**

## How it extends the original (without changing it)

| Concern | Approach |
|---|---|
| New disabilities | Own `PlusDisability` enum; the original's enum is closed |
| State, save file, sync | Own state (`disabilities-plus.txt` in the world), own packets (`disabilities_plus:state`, `:episode`) |
| Commands | Brigadier nodes are merged into the original's `/disability` tree; the original's executors are delegated to |
| Random pool / timer | A mixin on `RunState.grantRandom` picks across both mods, then hands the original what it expects |
| HUD list | A mixin on `DisabilityHud.render` swaps in one combined, column-wrapping list |
| FOV, camera, input, sound | The add-on's own mixins on vanilla classes, so the original's hooks are never relied on |

## Build and test

```bash
./gradlew build                 # compile, unit tests, static mixin verification
xvfb-run -a ./gradlew runClientGameTest   # headless end-to-end run with the original loaded
```

The original jar must be in `libs/` (it is a dev dependency only and is never bundled into the add-on jar).
