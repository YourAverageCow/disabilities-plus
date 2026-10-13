package com.disabilitiesplus;

/**
 * The disabilities this add-on contributes. They live in their own enum because the original mod's
 * {@code Disability} enum is closed; state, sync and persistence for these are handled by {@link PlusState}.
 */
public enum PlusDisability {
   TINNITUS("Tinnitus", "\u00a7b", "A constant ringing dulls the world around you until you stand still and listen"),
   NIGHT_BLINDNESS("Night Blindness", "\u00a78", "You can barely see anywhere that isn't brightly lit; a torch or lantern in hand pushes the dark back"),
   TUNNEL_VISION("Tunnel Vision", "\u00a76", "Your field of view narrows to a tight tunnel"),
   ANEMIA("Anemia", "\u00a7c", "Your blood is thin: only half of anything you heal sticks, unless you eat something iron-rich (red meat, beetroot)"),
   MIGRAINE("Migraine", "\u00a7d", "Random migraines pulse your vision, tilt the world and muffle sound"),
   AMNESIA("Amnesia", "\u00a79", "You keep forgetting things: your chat history vanishes"),
   ACROPHOBIA("Acrophobia", "\u00a7e", "Near a big drop you freeze into a crouch with your heart pounding"),
   CLAUSTROPHOBIA("Claustrophobia", "\u00a75", "Tight spaces make you hyperventilate until your air runs out"),
   HEMOPHILIA("Hemophilia", "\u00a74", "Any damage makes you bleed for a long time; a sheet of paper makes a bandage"),
   APHASIA("Aphasia", "\u00a7a", "The words come out wrong when you speak in chat"),
   DYSCALCULIA("Dyscalculia", "\u00a76", "Numbers look wrong: item counts and other digits are scrambled"),
   PHOTOSENSITIVITY("Photosensitivity", "\u00a7f", "Direct sunlight burns you unless you wear a helmet"),
   MOTION_SICKNESS("Motion Sickness", "\u00a72", "Boats, minecarts, mounts and high speeds make you nauseous unless you keep your eyes on the horizon"),
   ATAXIA("Ataxia", "\u00a73", "You stumble sideways and lose control of your movement"),
   HEART_CONDITION("Heart Condition", "\u00a7c", "Sustained sprinting strains your heart, hurting you and forcing a slowdown"),
   AUTISM("Autism", "\u00a7b", "Sudden loud noises overwhelm you: explosions, thunder and anvils dim the screen and muffle sound"),
   BIPOLAR("Bipolar", "\u00a75", "Your mood swings every few minutes between a fast, driven high and a slow, heavy low"),
   BUTTERFINGERS("Butterfingers", "\u00a7e", "You randomly drop whatever you are holding"),
   HANGRY("Hangry", "\u00a76", "Below three shanks of hunger you are weak and slow at mining"),
   TWO_LEFT_FEET("Two Left Feet", "\u00a7d", "Your feet tangle on landing: after a jump you sometimes stagger and stop sprinting, unless you land in a crouch"),
   CREEPERPHOBIA("Creeperphobia", "\u00a7a", "A creeper nearby freezes you in place while your heart pounds"),
   ARACHNOPHOBIA("Arachnophobia", "\u00a74", "A spider nearby makes your head snap away from it"),
   LEFT_RIGHT_CONFUSION("Left-Right Confusion", "\u00a79", "Now and then your strafing keys swap sides"),
   HICCUPS("Hiccups", "\u00a7f", "You hiccup now and then, jolting your view"),
   SNEEZING("Sneezing Fits", "\u00a73", "You sneeze now and then, jerking your camera and waking every monster nearby"),
   PICKY_EATER("Picky Eater", "\u00a7a", "You will not eat the same thing again for half a minute"),
   EGG_LAYER("Egg Layer", "\u00a7e", "Every so often you lay an egg"),
   MOON_BOOTS("Moon Boots", "\u00a7f", "Gravity is weak: floaty jumps, and a hard landing bounces you back up unless you land in a crouch"),
   GIANT("Giant", "\u00a76", "You are half again as tall: long reach and a stomp that hurts and wakes monsters, but you no longer fit through two-block gaps"),
   POGO("Pogo Stick", "\u00a7d", "You keep bouncing about once a second"),
   TIPSY("Tipsy", "\u00a75", "The horizon rolls from side to side like a deck at sea"),
   SWEET_TOOTH("Sweet Tooth", "\u00a7c", "Sweet foods give a speed rush, followed by a sugar crash"),
   SCAREDY_CAT("Scaredy Cat", "\u00a78", "Monsters send you running, but you cannot bring yourself to hit them"),
   GLASS_CANNON("Glass Cannon", "\u00a7b", "You hit twice as hard but only have half the health, and a hit of two hearts or more cracks you for a while"),
   SQUEAKY_SHOES("Squeaky Shoes", "\u00a73", "Every few steps your shoes squeak and draw nearby monsters toward you (sneak to be quiet)"),
   QUADRUPLE_AMPUTEE(
      "Quadruple Amputee", "\u00a74", "You have no arms or legs: you cannot move or use anything unless friends feed and carry you. Rarer than the rest, and dying cures it", 0.5
   ),
   PREGNANCY("Pregnancy", "\u00a7d", "You are pregnant: slower, a wider belly and kicks from the baby, which also craves foods that reward you"),
   GINGER("Ginger", "\u00a76", "The whole world looks sepia"),
   OBESITY("Obesity", "\u00a7e", "You are heavier: a wide body, a weaker jump and sprinting burns hunger faster, but nothing shoves you around while you sprint"),
   ORPHAN("Orphan", "\u00a77", "No one is waiting for you: beds and respawn points never stick, so you always respawn at world spawn"),
   ALCOHOLISM("Alcoholism", "\u00a7a", "You crave a drink: a potion or honey bottle gives a buzz, and going too long without one brings withdrawal blackouts"),
   PARANOIA("Paranoia", "\u00a78", "You keep hearing someone right behind you and flinching"),
   DRUG_ADDICTION("Drug Addiction", "\u00a75", "You crave golden food: a fix gives strength and haste then a crash, and going without brings withdrawal"),
   BLOOD_HUNGRY("Blood Hungry", "\u00a7c", "You burn through food faster, and hurting things heals and feeds you"),
   ENDERPHOBIA("Enderphobia", "\u00a75", "An Enderman nearby closes your vision in with dread"),
   APIPHOBIA("Apiphobia", "\u00a7e", "A bee nearby buzzes at your ear and sets your hands twitching"),
   AQUAPHOBIA("Aquaphobia", "\u00a79", "Water makes you thrash and flail; rain makes you sprint for cover"),
   PYROPHOBIA("Pyrophobia", "\u00a74", "Fire and lava nearby pull your gaze in and you cannot look away"),
   THRILL_SEEKER("Thrill Seeker", "\u00a76", "Big drops energize you with speed and haste, but cramped spaces bore you into mining fatigue"),
   NIGHT_OWL("Night Owl", "\u00a7b", "Groggy and slow in bright light, quick and sharp in the dark"),
   HOARDER("Hoarder", "\u00a77", "The fuller your inventory gets, the slower you move and the better padded you are"),
   HEAD_RUSH("Head Rush", "\u00a7d", "Standing up from a crouch makes your vision black out for a second"),
   SLIPPERY_GRIP("Slippery Grip", "\u00a7e", "You fumble your tools: every swing and every block broken wears the held tool double"),
   BACKWARDS_BRAIN("Backwards Brain", "\u00a7a", "Now and then your mouse looks the wrong way, up and down"),
   HEAVY_EYELIDS("Heavy Eyelids", "\u00a78", "Your eyelids creep shut until a jump, a swing or a crouch jolts you awake"),
   MISOPHONIA("Misophonia", "\u00a7c", "Eating and drinking sounds come back shrill and loud, and make you flinch"),
   JUMPY("Jumpy", "\u00a7f", "Loud noises make you jump and flinch"),
   BLABBERMOUTH("Blabbermouth", "\u00a76", "Every couple of minutes you blurt out your coordinates in chat"),
   IBS("IBS", "\u00a7e", "Eating can set off cramps that double you over"),
   KIDNEY_STONES("Kidney Stones", "\u00a7c", "Every few minutes a stone passes: several seconds of locked-up agony, unless you have drunk recently"),
   STONED("Stoned", "\u00a72", "The world breathes, your eyes droop and your swings come slow"),
   LEAD_POISONING("Lead Poisoning", "\u00a77", "Your health is lower, and every Lead in your pack poisons you more; a bucket of milk flushes it for a while"),
   BLACK_LUNG("Black Lung", "\u00a78", "Mining fills your lungs with soot that closes in on your vision until you choke; fresh air clears it"),
   NECROSIS("Necrosis", "\u00a75", "Your tissue dies in stages: hands, then legs, then strength; golden apples push the rot back"),
   PTSD("PTSD", "\u00a79", "Explosions and being hit trigger a flashback: dread, a pounding heart and a muffled world"),
   ARFID("ARFID", "\u00a7a", "You can only keep down a few safe foods; everything else comes straight back up"),
   ANOREXIA("Anorexia", "\u00a7d", "Food barely nourishes you and you grow frail whenever you are hungry"),
   ALPHA_GAL("Alpha-gal Syndrome", "\u00a7c", "Mammal meat sets off a delayed allergic reaction: swollen hands, an empty stomach and a painful hit"),
   B12_DEFICIENCY("B12 Deficiency", "\u00a7d", "Without animal foods your legs go numb and you can no longer step up onto a block"),
   COVID_19("COVID-19", "\u00a7a", "Coughing fits and fever spikes; it spreads to nearby players until you recover"),
   ERECTILE_DYSFUNCTION("Erectile Dysfunction", "\u00a7e", "You cannot sustain it: using items cuts out, and the more eyes are on you the likelier it is"),
   EBOLA("Ebola", "\u00a74", "A deadly fever: you waste away and weaken, and it spreads on contact until you recover"),
   E_COLI("E. coli", "\u00a72", "Raw food and dirty water run straight through you and drain your hunger"),
   MONO("Mononucleosis", "\u00a76", "Exhaustion makes you nap at random, and a hard hit can rupture your spleen"),
   BALDNESS("Baldness", "\u00a77", "No hair: your scalp chills fast in the cold without a helmet"),
   DYSGRAPHIA("Dysgraphia", "\u00a7b", "Your writing comes out wrong: letters flip, swap and drop in text boxes, signs and books"),
   EVIL("Evil", "\u00a74", "Everything hostile or neutral nearby is out to get you"),
   SOCIAL_ANXIETY("Social Anxiety", "\u00a79", "Being watched builds panic: shaking hands, a locked tongue, then freezing. A close friend, a helmet or a pumpkin mask calms it"),
   ALIEN_HAND("Alien Hand Syndrome", "\u00a75", "Your offhand has a mind of its own: it slaps whoever is nearest, grabs your items and drags you toward people. Give it something to hold"),
   CATAPLEXY("Cataplexy", "\u00a7e", "Strong emotion drops you: laughing in chat, a great find, a kill. Your knees buckle unless you are crouched and braced"),
   PAIN_INSENSITIVITY("Insensitivity to Pain", "\u00a77", "You cannot feel pain: no health bar, and injuries fester unnoticed until a friend (or you, standing still) checks you over"),
   EHLERS_DANLOS("Ehlers-Danlos Syndrome", "\u00a7a", "Hard knocks and falls dislocate your joints: a useless arm or leg until you pop it back in, or a friend does");

   /** Cached copy of {@link #values()}, which clones the array on every call. Do not modify. */
   public static final PlusDisability[] ALL = values();

   public final String displayName;
   public final String color;
   public final String description;
   /** Scales the chance of being picked at random (1.0 = normal). */
   public final double weight;
   /** The coloured bullet line shown in the HUD list, built once. */
   public final String listLine;

   private PlusDisability(String displayName, String color, String description) {
      this(displayName, color, description, 1.0);
   }

   private PlusDisability(String displayName, String color, String description, double weight) {
      this.displayName = displayName;
      this.color = color;
      this.description = description;
      this.weight = weight;
      this.listLine = color + "\u2022 " + displayName;
   }

   /** Finds a disability by its command name (the lower-case enum name), or null. */
   public static PlusDisability find(String name) {
      for (PlusDisability d : ALL) {
         if (d.name().equalsIgnoreCase(name)) {
            return d;
         }
      }

      return null;
   }
}
