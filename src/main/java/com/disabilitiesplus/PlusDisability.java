package com.disabilitiesplus;

public enum PlusDisability {
    TINNITUS("Tinnitus", "§b", "A constant ringing dulls the world around you until you stand still and listen"),
    NIGHT_BLINDNESS("Night Blindness", "§8", "You can barely see anywhere that isn't brightly lit; a torch or lantern in hand pushes the dark back"),
    TUNNEL_VISION("Tunnel Vision", "§6", "Your field of view narrows to a tight tunnel"),
    ANEMIA("Anemia", "§c", "Your blood is thin: only half of anything you heal sticks, unless you eat something iron-rich (red meat, beetroot)"),
    MIGRAINE("Migraine", "§d", "Random migraines pulse your vision, tilt the world and muffle sound"),
    AMNESIA("Amnesia", "§9", "You keep forgetting things: your chat history vanishes"),
    ACROPHOBIA("Acrophobia", "§e", "Near a big drop you freeze into a crouch with your heart pounding"),
    CLAUSTROPHOBIA("Claustrophobia", "§5", "Tight spaces make you hyperventilate until your air runs out"),
    HEMOPHILIA("Hemophilia", "§4", "Any damage makes you bleed for a long time; a sheet of paper makes a bandage"),
    APHASIA("Aphasia", "§a", "The words come out wrong when you speak in chat"),
    DYSCALCULIA("Dyscalculia", "§6", "Numbers look wrong: item counts and other digits are scrambled"),
    PHOTOSENSITIVITY("Photosensitivity", "§f", "Direct sunlight burns you unless you wear a helmet"),
    MOTION_SICKNESS("Motion Sickness", "§2", "Boats, minecarts, mounts and high speeds make you nauseous unless you keep your eyes on the horizon"),
    ATAXIA("Ataxia", "§3", "You stumble sideways and lose control of your movement"),
    HEART_CONDITION("Heart Condition", "§c", "Sustained sprinting strains your heart, hurting you and forcing a slowdown"),
    AUTISM("Autism", "§b", "Sudden loud noises overwhelm you: explosions, thunder and anvils dim the screen and muffle sound"),
    BIPOLAR("Bipolar", "§5", "Your mood swings every few minutes between a fast, driven high and a slow, heavy low"),
    BUTTERFINGERS("Butterfingers", "§e", "You randomly drop whatever you are holding"),
    HANGRY("Hangry", "§6", "Below three shanks of hunger you are weak and slow at mining"),
    TWO_LEFT_FEET("Two Left Feet", "§d", "Your feet tangle on landing: after a jump you sometimes stagger and stop sprinting, unless you land in a crouch"),
    CREEPERPHOBIA("Creeperphobia", "§a", "A creeper nearby freezes you in place while your heart pounds"),
    ARACHNOPHOBIA("Arachnophobia", "§4", "A spider nearby makes your head snap away from it"),
    LEFT_RIGHT_CONFUSION("Left-Right Confusion", "§9", "Now and then your strafing keys swap sides"),
    HICCUPS("Hiccups", "§f", "You hiccup now and then, jolting your view"),
    SNEEZING("Sneezing Fits", "§3", "You sneeze now and then, jerking your camera and waking every monster nearby"),
    PICKY_EATER("Picky Eater", "§a", "You will not eat the same thing again for half a minute"),
    EGG_LAYER("Egg Layer", "§e", "Every so often you lay an egg"),
    MOON_BOOTS("Moon Boots", "§f", "Gravity is weak: floaty jumps, and a hard landing bounces you back up unless you land in a crouch"),
    GIANT("Giant", "§6", "You are half again as tall: long reach and a stomp that hurts and wakes monsters, but you no longer fit through two-block gaps"),
    POGO("Pogo Stick", "§d", "You keep bouncing about once a second"),
    TIPSY("Tipsy", "§5", "The horizon rolls from side to side like a deck at sea"),
    SWEET_TOOTH("Sweet Tooth", "§c", "Sweet foods give a speed rush, followed by a sugar crash"),
    SCAREDY_CAT("Scaredy Cat", "§8", "Monsters send you running, but you cannot bring yourself to hit them"),
    GLASS_CANNON("Glass Cannon", "§b", "You hit twice as hard but only have half the health, and a hit of two hearts or more cracks you for a while"),
    SQUEAKY_SHOES("Squeaky Shoes", "§3", "Every few steps your shoes squeak and draw nearby monsters toward you (sneak to be quiet)"),
    QUADRUPLE_AMPUTEE(
        "Quadruple Amputee", "§4", "You have no arms or legs: you cannot move or use anything unless friends feed and carry you. Rarer than the rest, and dying cures it", 0.5
    ),
    PREGNANCY("Pregnancy", "§d", "You are pregnant: slower, a wider belly and kicks from the baby, which also craves foods that reward you"),
    GINGER("Ginger", "§6", "The whole world looks sepia"),
    OBESITY("Obesity", "§e", "You are heavier: a wide body, a weaker jump and sprinting burns hunger faster, but nothing shoves you around while you sprint"),
    ORPHAN("Orphan", "§7", "No one is waiting for you: beds and respawn points never stick, so you always respawn at world spawn"),
    ALCOHOLISM("Alcoholism", "§a", "You crave a drink: a potion or honey bottle gives a buzz, and going too long without one brings withdrawal blackouts"),
    PARANOIA("Paranoia", "§8", "You keep hearing someone right behind you and flinching"),
    DRUG_ADDICTION("Drug Addiction", "§5", "You crave golden food: a fix gives strength and haste then a crash, and going without brings withdrawal"),
    BLOOD_HUNGRY("Blood Hungry", "§c", "You burn through food faster, and hurting things heals and feeds you"),
    ENDERPHOBIA("Enderphobia", "§5", "An Enderman nearby closes your vision in with dread"),
    APIPHOBIA("Apiphobia", "§e", "A bee nearby buzzes at your ear and sets your hands twitching"),
    AQUAPHOBIA("Aquaphobia", "§9", "Water makes you thrash and flail; rain makes you sprint for cover"),
    PYROPHOBIA("Pyrophobia", "§4", "Fire and lava nearby pull your gaze in and you cannot look away"),
    THRILL_SEEKER("Thrill Seeker", "§6", "Big drops energize you with speed and haste, but cramped spaces bore you into mining fatigue"),
    NIGHT_OWL("Night Owl", "§b", "Groggy and slow in bright light, quick and sharp in the dark"),
    HOARDER("Hoarder", "§7", "The fuller your inventory gets, the slower you move and the better padded you are"),
    HEAD_RUSH("Head Rush", "§d", "Standing up from a crouch makes your vision black out for a second"),
    SLIPPERY_GRIP("Slippery Grip", "§e", "You fumble your tools: every swing and every block broken wears the held tool double"),
    BACKWARDS_BRAIN("Backwards Brain", "§a", "Now and then your mouse looks the wrong way, up and down"),
    HEAVY_EYELIDS("Heavy Eyelids", "§8", "Your eyelids creep shut until a jump, a swing or a crouch jolts you awake"),
    MISOPHONIA("Misophonia", "§c", "Eating and drinking sounds come back shrill and loud, and make you flinch"),
    JUMPY("Jumpy", "§f", "Loud noises make you jump and flinch"),
    BLABBERMOUTH("Blabbermouth", "§6", "Every couple of minutes you blurt out your coordinates in chat"),
    IBS("IBS", "§e", "Eating can set off cramps that double you over"),
    KIDNEY_STONES("Kidney Stones", "§c", "Every few minutes a stone passes: several seconds of locked-up agony, unless you have drunk recently"),
    STONED("Stoned", "§2", "The world breathes, your eyes droop and your swings come slow"),
    LEAD_POISONING("Lead Poisoning", "§7", "Your health is lower, and every Lead in your pack poisons you more; a bucket of milk flushes it for a while"),
    BLACK_LUNG("Black Lung", "§8", "Mining fills your lungs with soot that closes in on your vision until you choke; fresh air clears it"),
    NECROSIS("Necrosis", "§5", "Your tissue dies in stages: hands, then legs, then strength; golden apples push the rot back"),
    PTSD("PTSD", "§9", "Explosions and being hit trigger a flashback: dread, a pounding heart and a muffled world"),
    ARFID("ARFID", "§a", "You can only keep down a few safe foods; everything else comes straight back up"),
    ANOREXIA("Anorexia", "§d", "Food barely nourishes you and you grow frail whenever you are hungry"),
    ALPHA_GAL("Alpha-gal Syndrome", "§c", "Mammal meat sets off a delayed allergic reaction: swollen hands, an empty stomach and a painful hit"),
    B12_DEFICIENCY("B12 Deficiency", "§d", "Without animal foods your legs go numb and you can no longer step up onto a block"),
    COVID_19("COVID-19", "§a", "Coughing fits and fever spikes; it spreads to nearby players until you recover"),
    ERECTILE_DYSFUNCTION("Erectile Dysfunction", "§e", "You cannot sustain it: using items cuts out, and the more eyes are on you the likelier it is"),
    EBOLA("Ebola", "§4", "A deadly fever: you waste away and weaken, and it spreads on contact until you recover"),
    E_COLI("E. coli", "§2", "Raw food and dirty water run straight through you and drain your hunger"),
    MONO("Mononucleosis", "§6", "Exhaustion makes you nap at random, and a hard hit can rupture your spleen"),
    BALDNESS("Baldness", "§7", "No hair: your scalp chills fast in the cold without a helmet"),
    DYSGRAPHIA("Dysgraphia", "§b", "Your writing comes out wrong: letters flip, swap and drop in text boxes, signs and books"),
    EVIL("Evil", "§4", "Everything hostile or neutral nearby is out to get you"),
    SOCIAL_ANXIETY("Social Anxiety", "§9", "Being watched builds panic: shaking hands, a locked tongue, then freezing. A close friend, a helmet or a pumpkin mask calms it"),
    ALIEN_HAND("Alien Hand Syndrome", "§5", "Your offhand has a mind of its own: it slaps whoever is nearest, grabs your items and drags you toward people. Give it something to hold"),
    CATAPLEXY("Cataplexy", "§e", "Strong emotion drops you: laughing in chat, a great find, a kill. Your knees buckle unless you are crouched and braced"),
    PAIN_INSENSITIVITY("Insensitivity to Pain", "§7", "You cannot feel pain: no health bar, and injuries fester unnoticed until a friend (or you, standing still) checks you over"),
    EHLERS_DANLOS("Ehlers-Danlos Syndrome", "§a", "Hard knocks and falls dislocate your joints: a useless arm or leg until you pop it back in, or a friend does");

    public static final PlusDisability[] ALL = values();

    public final String displayName;
    public final String color;
    public final String description;

    public final double weight;

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

    public static PlusDisability find(String name) {
        for (PlusDisability d : ALL) {
            if (d.name().equalsIgnoreCase(name)) {
                return d;
            }
        }

        return null;
    }
}
