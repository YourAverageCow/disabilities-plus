<img src="logo.png" alt="Disabilities Plus logo" width="140" align="right">

# Disabilities Plus

An add-on for the Fabric mod [Disabilities](https://modrinth.com/mod/disabilities) (0.5.0, Minecraft 26.3).

It adds 82 more disabilities to the original's 30. They show up in the same HUD list, use the same `/disability`
command and come out of the same random pool, so the two mods feel like one.

Every disability has its own mechanic, and almost all of them have something you can do about it: a torch for night
blindness, paper for a bleed, a crouch to brace against a collapse. A good number are also much better with other
people around, because friends can feed, bandage, carry and fix each other (see [Playing with other people](#playing-with-other-people)).

> The original mod is required and is **not** included here or in the add-on jar. This repository holds only the
> add-on's own code.

## Installing

You need Minecraft 26.3, Fabric Loader 0.19.5 or newer, Fabric API, Java 25, and the original
**Disabilities 0.5.0+26.3**. Put both jars in `mods`. A server and every player on it need both; someone without the
add-on is turned away with a message saying why.

## Commands

The add-on extends the original `/disability` command and leaves the rest of it alone:

* `/disability give|remove <players> <name|all|random>`: the new names, `all` and `random` now cover both mods, and
  the names tab-complete next to the originals.
* `/disability next [players]` and `/disability list [players]`: include the new disabilities.
* `/disability deathremoval [0-100]`: how many random disabilities (from either mod) each death lifts. The default
  is 0, so nothing changes unless you set it.

## What's in it

Names are the ids you use in commands.

### Senses and perception

| Id | What it does |
|---|---|
| `tinnitus` | A high ring plays and everything but voices is dulled (x0.65) until you stand still for two seconds. |
| `night_blindness` | Dark places are heavily dimmed (up to 80%). A torch or lantern in either hand cuts that to a fifth. |
| `tunnel_vision` | Your field of view narrows (x0.62) inside a dark vignette. |
| `migraine` | Random 12 to 20 second episodes: a pulsing white-out, a wobbling camera, sound at x0.45. |
| `autism` | Loud sounds within 32 blocks overload you for 6 seconds: the screen dims and sound is muffled. |
| `misophonia` | Eating and drinking sounds come back three times, loud and shrill, and make you flinch. |
| `jumpy` | Loud sounds make you jump and flinch. |
| `heavy_eyelids` | Your lids creep shut over 30 seconds (black bars close in) until a jump, a swing or a crouch wakes you. |
| `head_rush` | Standing up from a crouch blacks your vision out for a second. |
| `stoned` | The field of view breathes, your eyes droop in slow blinks and your swings are 30% slower. |
| `tipsy` | The horizon rolls about 5 degrees each way, like a deck at sea. |
| `ginger` | The whole world is sepia. |

### Fears and nerves

| Id | What it does |
|---|---|
| `acrophobia` | Near a drop of 10+ blocks you freeze into a forced crouch with a pounding heart, which also stops you stepping off. |
| `claustrophobia` | Tight spaces make you hyperventilate: air runs out in about 10 seconds, then it hurts, and the view closes in. |
| `creeperphobia` | A creeper within 8 blocks freezes you in bursts while a heartbeat quickens as it gets closer. |
| `arachnophobia` | A spider within 8 blocks makes your head snap away from it. |
| `enderphobia` | An Enderman within 12 blocks closes your vision in with dread. |
| `apiphobia` | A bee within 8 blocks buzzes at your ear and makes your view twitch. |
| `aquaphobia` | In water you thrash upwards. In the rain you bolt for cover. |
| `pyrophobia` | Fire or lava within 5 blocks drags your gaze toward it. |
| `scaredy_cat` | A hostile mob within 12 blocks sends you bolting (Speed II), but you can't bring yourself to hit it. |
| `paranoia` | Footsteps right behind you about every 10 seconds, and the odd flinch. |
| `ptsd` | An explosion, or being hit, starts a 5 second flashback: dread, a pounding heart, a muffled world. |
| `thrill_seeker` | Near a drop you get Speed and Haste. In a cramped space you get Mining Fatigue. |
| `social_anxiety` | Being watched builds panic. At 30 your hands shake (mining -20%), at 60 you slow down, can't sprint and **can't send chat**, at 90 you freeze for 3 seconds. See below for who counts as watching. |

### Mind and behaviour

| Id | What it does |
|---|---|
| `amnesia` | Your chat history is wiped every 1 to 2.5 minutes. |
| `aphasia` | The words you send in chat are replaced with filler or have their letters shuffled. |
| `dyscalculia` | Digits in ordinary text are swapped (64 reads as 70) and item stack counts are unreadable. |
| `dysgraphia` | What you write in text boxes, signs and books comes out wrong: b/d and p/q flip, letters swap and drop. Chat and commands are left alone. |
| `bipolar` | Every 3 to 6 minutes your mood flips between a fast, driven high and a slow low. |
| `left_right_confusion` | Random 10 to 20 second episodes where A and D swap. |
| `backwards_brain` | Random 10 to 20 second episodes where looking up and down is inverted. |
| `blabbermouth` | About every 2 minutes you blurt your coordinates into chat. |
| `evil` | Every hostile and neutral mob within 40 blocks is set on you, and neutral ones stay angry. |
| `alien_hand` | Every 20 to 60 seconds your offhand acts on its own: it slaps the nearest creature for 2 (friends, pets and villagers included), drags you toward the nearest person, or swaps what's in your hands. Holding something in the offhand makes it three times rarer and halves the slap. |
| `cataplexy` | Strong emotion drops you for 4 seconds, fully awake, hands useless. Triggers: laughter in chat (lol, haha, xd...), mining diamonds, emeralds or ancient debris, and now and then a kill. Crouching or sitting braces you, and you only wobble. |
| `night_owl` | Slow and weak in bright light; fast, with Night Vision, in the dark. |

### Body and movement

| Id | What it does |
|---|---|
| `ataxia` | You stumble sideways at random while walking. |
| `hiccups` | About every 25 seconds a hiccup jolts the camera, with a burp. |
| `sneezing` | Every 1 to 3 minutes a sneeze jerks the camera and sets every monster within 16 blocks on you. |
| `butterfingers` | About every 2 minutes you drop one of whatever you're holding (not with Kleptomania). |
| `slippery_grip` | Every hit you land and every block you break wears the held tool by one extra use. |
| `two_left_feet` | After a real jump or drop, one landing in three staggers you (sprint stops, Slowness IV for 1.5 s) unless you land in a crouch. |
| `pogo` | You auto-jump about once a second. |
| `moon_boots` | Gravity is 40% of normal. A hard landing bounces you back up like a slime block unless you land crouched. |
| `giant` | Scale x1.5 and +1 reach, but you don't fit through two-block gaps. A hard landing is a stomp: it hurts and shoves hostile mobs beside you, launches friends into the air unhurt, and wakes every monster within 12 blocks. |
| `obesity` | A wide body and a weaker jump. Sprinting burns hunger faster, but nothing shoves you around while you sprint and a sprinting hit sends the target flying. |
| `hoarder` | With 18+ used inventory slots you're 10% slower but gain +2 armor. At 28+, 25% slower and +6 armor. |
| `glass_cannon` | Double damage, half the health. A hit of 2 hearts or more cracks you: one heart less for 3 minutes, up to three cracks. |
| `squeaky_shoes` | You squeak every 1.5 blocks walked, and each squeak draws hostile mobs within 12 blocks. Sneaking is silent. |
| `quadruple_amputee` | No limbs: you can't move, jump, drop, use, place, attack or break. **Half as likely** to be picked, and **dying cures it**. |
| `ehlers_danlos` | A hit of 2 hearts or more, or a fall of 3+ blocks, dislocates an arm (no hitting or mining) or a leg (half speed, no jumping). A friend pops it back in with an empty hand (1 damage), or crouch still for 6 seconds (2 damage). |
| `pain_insensitivity` | No health bar. A hit of 3 or more leaves a wound you can't feel: 1 damage every 2 seconds for 10 s, stacking to 30 s, until someone checks you. Crouch still for 3 seconds to check yourself. |
| `pregnancy` | 10% slower, a wider belly, a kick every 30 seconds or so. The baby craves a food: eating it gives 10 seconds of Regeneration II. |
| `hemophilia` | Damage starts a long bleed (1 damage every 2 s, up to 30 s). A sheet of paper is a bandage. |
| `anemia` | Your blood is thin: only half of anything you heal sticks, unless you ate red meat or beetroot in the last five minutes. |
| `heart_condition` | About 15 seconds of sprinting does 2 damage and gives Slowness III. |
| `photosensitivity` | You burn in direct daylight unless you wear a helmet. |
| `motion_sickness` | Riding anything, or moving very fast, makes you nauseous, unless you keep your eyes level with the horizon (within 15 degrees). |
| `egg_layer` | About once a minute you lay an egg. |
| `baldness` | No hair layer on your head. Without a helmet, cold biomes and rain chill your scalp until you freeze. |
| `orphan` | Beds, anchors and `/spawnpoint` never stick: you always respawn at world spawn. |

### Food, drink and cravings

| Id | What it does |
|---|---|
| `hangry` | Below 3 shanks of hunger, mining is 25% slower and you're weak. Nothing drains faster. |
| `picky_eater` | Whatever you just ate goes on a 30 second cooldown, so you can't eat the same thing twice in a row. |
| `sweet_tooth` | Sweets give Speed II for 30 seconds, then a short crash. |
| `blood_hungry` | Extra hunger drain. Hitting a creature heals 2 HP and feeds you. |
| `alcoholism` | A potion or honey bottle gives a buzz. About 4 minutes without brings withdrawal blackouts (Darkness). |
| `drug_addiction` | Golden food is the fix: strength and haste, then a weakness and fatigue crash. About 5 minutes without brings withdrawal. |
| `arfid` | Only a few safe foods stay down. Anything else is handed back. |
| `anorexia` | Food restores about a third, and while hungry you're frail. |
| `alpha_gal` | Eating mammal meat starts a 40 second timer, then anaphylaxis: swollen hands (Mining Fatigue III, Weakness II), 4 damage (never below one heart) and an emptied stomach. |
| `b12_deficiency` | Animal foods and milk top up a supply that drains. When it's empty your legs go numb and can't step up stairs or slabs; you have to jump. |
| `ibs` | Eating sounds set off cramps that double you over. |
| `kidney_stones` | Every 3 to 6 minutes a stone passes: 5 seconds locked in agony, 1 damage a second (never lethal). Drinking a potion or milk hydrates you for 3 minutes, so the next one passes in 2 seconds, painlessly. |
| `lead_poisoning` | A heart less, and each Lead in your pack costs another heart and poisons you. A bucket of milk flushes it for 5 minutes. |

### Illness

| Id | What it does |
|---|---|
| `covid_19` | An 8 minute illness: coughing fits, and flushed fever spikes (weakness) after the first minute. Spreads to players within 3 blocks. |
| `ebola` | A 12 minute illness: you lose 2 hearts of maximum health every 45 seconds and you're weak. Spreads to players within 2 blocks. |
| `e_coli` | A 6 minute illness: raw food costs more hunger than it gives (Hunger II), and a few seconds in water does the same. |
| `mono` | A 10 minute illness: you nap (black screen, locked controls) every 1 to 2 minutes, and a hit of 3 hearts or more ruptures your spleen (4 damage and a blackout). |
| `erectile_dysfunction` | You can't sustain it: using an item cuts out at random, more often the more living things are within 8 blocks, and people count double. |
| `black_lung` | Mining underground fills your lungs. Soot creeps in from the screen edges, and past the limit you choke (1 damage every 2 s) until you reach open sky. |
| `necrosis` | Every 5 minutes more tissue dies: first your hands (mining -50%), then your legs (speed -50%), then your strength. A golden apple pushes it back a stage, an enchanted one cures it. |

## Playing with other people

Everything works alone, but a lot of it is better with company. All of these are right-clicks on another player, or
simply being nearby.

| Condition | What friends can do |
|---|---|
| Anemia, Pregnancy, Kidney Stones, Lead Poisoning, Necrosis | Feed them: right-click with food, a potion or milk. It counts as them eating it (iron-rich meat, the craved food, water, milk, a golden apple). Anything that would hurt them, wipe their effects or teleport them can't be fed this way. |
| Quadruple Amputee | Feed them, or carry them: right-click with an empty hand and they ride on your back until you crouch. |
| Social Anxiety | Players facing you with a clear line of sight (within 30 blocks) count as watching, up to four at once. A friend within 2.5 blocks calms you, a helmet halves the count, and a carved pumpkin hides you. Alone, hostile mobs and villagers looking at you count instead. |
| Alien Hand | Keep your distance: it slaps whoever is nearest and pulls you toward them. |
| Cataplexy | Telling a joke is an attack. Crouching next to them is protection. |
| Pain Insensitivity | A right-click with an empty hand checks them over and dresses any wound. You're warned when they're badly hurt. |
| Ehlers-Danlos | A right-click with an empty hand pops their joint back in. |
| Hemophilia | Right-click a bleeding player with paper to bandage them. |
| Glass Cannon | Right-click a cracked player with a glass block or pane to patch one crack. |
| Giant | The stomp launches nearby friends into the air, unhurt. |
| Moon Boots | Your bounce lifts players within 3 blocks too. |
| Hoarder | Players within 6 blocks share half your padding as armor. |
| Squeaky Shoes | Everyone nearby hears the squeak, so your stealth is theirs. |
| Night Blindness | A friend's torch or lantern within 6 blocks lights you too. |
| Tinnitus | The ringing eases when another player is within 3 blocks. |
| Two Left Feet | A friend within 2.5 blocks steadies you, so no stagger. |

## A few details

* **Illnesses** (`covid_19`, `ebola`, `e_coli`, `mono`) run their course and then remove themselves. Recovering gives
  ten minutes of immunity to catching the same one again, and dying cures them. They only spread between players
  (COVID-19 at 10% a second, Ebola at 15% a second while close).
* **Dyslexia and stack counts:** in the original, Dyslexia scrambles item counts along with every other bit of text.
  With this add-on counts stay readable under Dyslexia and become unreadable under `dyscalculia` instead. Other text
  is still Dyslexia's.
* **Dysgraphia** is about what you write (text boxes, signs, books), not what you read. Speaking in chat is Aphasia's.
* No two disabilities share both a trigger and an effect, so each one plays differently.

## How it works with the original

The add-on never edits the original. It keeps its own list of disabilities, its own save file
(`disabilities-plus.txt` in the world) and its own network packets. It merges its command nodes into the original's
`/disability` tree, adds a mixin so `random` picks across both mods and hands the original what it expects, and
swaps in one combined HUD list. The field-of-view, camera, input and sound effects use the add-on's own mixins on
vanilla classes.

## Building

```bash
./gradlew build                            # compile, unit tests, static check of the mixins
xvfb-run -a ./gradlew runClientGameTest    # headless end-to-end run with the original loaded
```

Put the original jar in `libs/` first (`libs/disabilities-0.5.0+26.3.jar`). It's a compile and run dependency only
and is never bundled into the add-on jar.

## License

MIT, see [LICENSE](LICENSE). That covers this add-on's own code only. The original Disabilities mod is a separate
work under its own terms and isn't part of this repository.
