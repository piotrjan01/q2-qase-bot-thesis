/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.common;

import java.util.HashMap;
import java.util.HashSet;
import piotrrr.thesis.gui.MyPopUpDialog;

/**
 *
 * @author piotrrr
 */
public class DeathMsgsParser {

    static HashMap<String, String> suicides = new HashMap<String, String>();
    static HashMap<String, String> kills = new HashMap<String, String>();
    static boolean ready = false;

    public static String[] parseMessage(String msg) {
        init();
        String[] ret = new String[4];

        int matchLength = 0;

        for (String k : suicides.keySet()) {
            if (msg.contains(k) && k.length() > matchLength) {
                ret[1] = msg.substring(0, msg.indexOf(k));
                ret[0] = ret[0];
                ret[2] = suicides.get(k);
                matchLength = k.length();
            }
        }
        matchLength = 0;
        
        if (ret[0] == null) {
            for (String k : kills.keySet()) {
                if (msg.contains(k) && k.length() > matchLength) {
                    ret[1] = msg.substring(0, msg.indexOf(k));
                    ret[0] = msg.substring(msg.indexOf(k) + k.length());
                    if (ret[0].contains(" ")) {
                        ret[0] = ret[0].substring(0, ret[0].indexOf(" "));
                    }
                    if (ret[0].contains("'")) {
                        ret[0] = ret[0].substring(0, ret[0].indexOf("'"));
                    }
                    ret[2] = kills.get(k);
                    matchLength = k.length();
                }
            }
        }

        doDbgOnResult(ret, msg);

        return ret;
    }

    private static void doDbgOnResult(String[] res, String msg) {
        if ((res[0] != null && (res[0].contains("the") || res[0].contains("lead"))) ||
                (res[1] != null && (res[1].contains("the") || res[1].contains("lead")))) {

            MyPopUpDialog.showMyDialogBox("Possible bad kill recognition!",
                    msg + "\n" + CommFun.getArrayAsString(res), MyPopUpDialog.warning);
        }
    }

    public static void init() {
        if (ready) {
            return;
        }

        // ***Gladiator*** suicides

        suicides.put(" commits suicide", "");
        suicides.put(" takes the easy way out", "");
        suicides.put(" has fragged himself", "");

        suicides.put(" has fragged herself", "");
        suicides.put(" has fragged itself", "");

        suicides.put(" took his own life", "");
        suicides.put(" took her own life", "");
        suicides.put(" took it's own life", "");

        suicides.put(" can be scraped off the pavement", "");
        suicides.put(" cratered", "");
        suicides.put(" discovers the effects of gravity", "");
        suicides.put(" was squished", "");
        suicides.put(" was squeezed like a ripe grape", "");
        suicides.put(" turned to juice", "");
        suicides.put(" sank like a rock", "Drowned");
        suicides.put(" tried unsuccesfully to breathe water", "");
        suicides.put(" tried to immitate a fish", "Drowned");
        suicides.put(" must learn when to breathe", "Drowned");
        suicides.put(" needs to learn how to swim", "Drowned");
        suicides.put(" took a long walk of a short pier", "Drowned");
        suicides.put(" might want to use a rebreather next time", "Drowned");

        suicides.put(" thought he didn't need a rebreather", "Drowned");
        suicides.put(" thought she didn't need a rebreather", "Drowned");
        suicides.put(" thought it didn't need a rebreather", "Drowned");

        suicides.put(" was dissolved", "Slime");
        suicides.put(" sucked slime", "Slime");
        suicides.put(" found an alternative way to die", "Slime");
        suicides.put(" needs more slime-resistance", "Slime");
        suicides.put(" might try on an environmental suit next time", "Slime");
        suicides.put(" does a back flip into the lava", "Lava");
        suicides.put(" was fried to a crisp", "Lava");
        suicides.put(" thought that lava was water", "Lava");
        suicides.put(" turned into a real hothead", "Lava");
        suicides.put(" thought lava was 'funny water'", "Lava");
        suicides.put(" tried to hide in the lava", "Lava");

        suicides.put(" thought he was fire resistant", "");
        suicides.put(" thought she was fire resistant", "");
        suicides.put(" thought it was fire resistant", "");

        suicides.put(" tried to emulate the god of hell-fire", "Lava");
        suicides.put(" tried to emulate the goddess Pele", "Lava");
        suicides.put(" tried to emulate the demigod", "Lava");

        suicides.put(" needs to rebind his 'strafe' keys", "Lava");
        suicides.put(" needs to rebind her 'strafe' keys", "Lava");
        suicides.put(" needs to rebind it's 'strafe' keys", "Lava");

        suicides.put(" blew up", "Bomb");
        suicides.put(" found a way out", "");
        suicides.put(" had enough for today", "");
        suicides.put(" has returned to real life(tm)", "");
        suicides.put(" saw the light", "");
        suicides.put(" got blasted", "");
        suicides.put(" was in the wrong place", "");
        suicides.put(" shouldn't play with equipment", "");
        suicides.put(" can't move around moving objects", "");

// XATRIX
        suicides.put(" that's gotta hurt", "");
// XATRIX

        suicides.put(" tried to put the pin back in", "Hand Grenade");
        suicides.put(" got the red and blue wires mixed up", "Hand Grenade");

        suicides.put(" held his grenade too long", "Hand Grenade");
        suicides.put(" held her grenade too long", "Hand Grenade");
        suicides.put(" held it's grenade too long", "Hand Grenade");

        suicides.put(" tried to disassemble his own grenade", "Hand Grenade");
        suicides.put(" tried to disassemble her own grenade", "Hand Grenade");
        suicides.put(" tried to disassemble it's own grenade", "Hand Grenade");

        suicides.put(" tried to grenade-jump unsuccessfully", "Grenade Launcher");
        suicides.put(" tried to play football with a grenade", "Grenade Launcher");
        suicides.put(" shouldn't mess around with explosives", "Grenade Launcher");
        suicides.put(" tripped on his own grenade", "Grenade Launcher");
        suicides.put(" tripped on her own grenade", "Grenade Launcher");
        suicides.put(" tripped on it's own grenade", "Grenade Launcher");

        suicides.put(" stepped on his own pineapple", "Grenade Launcher");
        suicides.put(" stepped on her own pineapple", "Grenade Launcher");
        suicides.put(" stepped on it's own pineapple", "Grenade Launcher");


        suicides.put(" knows didley squatt about rocket launchers", "Rocket Launcher");
        suicides.put(" thought up a novel new way to fly", "Rocket Launcher");

        suicides.put(" blew himself up", "Rocket Launcher");
        suicides.put(" blew herself up", "Rocket Launcher");
        suicides.put(" blew itself up", "Rocket Launcher");

        suicides.put(" thought he was Werner von Braun", "Rocket Launcher");
        suicides.put(" thought she was Werner von Braun", "Rocket Launcher");
        suicides.put(" thought it was Werner von Braun", "Rocket Launcher");

        suicides.put(" thought he had more health", "Rocket Launcher");
        suicides.put(" thought she had more health", "Rocket Launcher");
        suicides.put(" thought it had more health", "Rocket Launcher");

        suicides.put(" found his own rocketlauncher's trigger", "Rocket Launcher");
        suicides.put(" found her own rocketlauncher's trigger", "Rocket Launcher");
        suicides.put(" found it's own rocketlauncher's trigger", "Rocket Launcher");

        suicides.put(" thought he had more armor on", "Rocket Launcher");
        suicides.put(" thought she had more armor on", "Rocket Launcher");
        suicides.put(" thought it had more armor on", "Rocket Launcher");

        suicides.put(" blew himself to kingdom come", "Rocket Launcher");
        suicides.put(" blew herself to kingdom come", "Rocket Launcher");
        suicides.put(" blew itself to kingdom come", "Rocket Launcher");

        suicides.put(" should have used a smaller gun", "BFG10K");
        suicides.put(" shouldn't play with big guns", "BFG10K");
        suicides.put(" doesn't know how to work the BFG", "BFG10K");
        suicides.put(" has trouble using big guns", "BFG10K");
        suicides.put(" can't distinguish which end is which with the BFG", "BFG10K");
        suicides.put(" should try to avoid using the BFG near obstacles", "BFG10K");
        suicides.put(" tried to BFG-jump unsuccesfully", "BFG10K");

// XATRIX
        suicides.put(" sucked into his own trap", "");
        suicides.put(" sucked into her own trap", "");
        suicides.put(" sucked into it's own trap", "");
//XATRIX

// ROGUE
        suicides.put(" got caught in his own trap", "");
        suicides.put(" got caught in her own trap", "");
        suicides.put(" got caught in it's own trap", "");
//ROGUE

        suicides.put(" commited suicide", "");
        suicides.put(" went the way of the dodo", "");
        suicides.put(" thought 'kill' was a funny console command", "");
        suicides.put(" wanted one frag less", "");

        suicides.put(" killed herself", "");
        suicides.put(" killed himself", "");
        suicides.put(" killed itself", "");

        suicides.put(" thought he had one many frags", "");
        suicides.put(" thought she had one many frags", "");
        suicides.put(" thought it had one many frags", "");


//MOD_BLASTER:
        kills.put(" (quakeweenie) was massacred by ", "Blaster");
        kills.put(" was killed with the wimpy blaster by ", "Blaster");
        kills.put(" died a wimp's death by ", "Blaster");
        kills.put(" can't even avoid a blaster from ", "Blaster");
        kills.put(" was blasted by ", "Blaster");

//MOD_SHOTGUN:
        kills.put(" was gunned down by ", "Shotgun");
        kills.put(" found himself on the wrong end of  ", "Shotgun");
        kills.put(" found herself on the wrong end of  ", "Shotgun");
        kills.put(" found itself on the wrong end of  ", "Shotgun");

//MOD_SSHOTGUN:
        kills.put(" was blown away by ", "Super Shotgun");
        kills.put(" had his ears cleaned out by ", "Super Shotgun");
        kills.put(" had her ears cleaned out by ", "Super Shotgun");
        kills.put(" had it ears cleaned out by ", "Super Shotgun");
        kills.put(" was put full of buckshot by ", "Super Shotgun");

//MOD_MACHINEGUN:
        kills.put(" was machinegunned by ", "Machinegun");
        kills.put(" was filled with lead by ", "Machinegun");
        kills.put(" was put full of lead by ", "Machinegun");
        kills.put(" was pumped full of lead by ", "Machinegun");
        kills.put(" ate lead dished out by ", "Machinegun");
        kills.put(" eats lead from ", "Machinegun");
        kills.put(" bites the bullet from ", "Machinegun");

//MOD_CHAINGUN:
        kills.put(" was cut in half by ", "Chaingun");
        kills.put(" was turned into a strainer by ", "Chaingun");
        kills.put(" was put full of holes by ", "Chaingun");
        kills.put(" couldn't avoid death by painless from ", "Chaingun");
        kills.put(" was put so full of lead by ", "Chaingun");
        kills.put(" was put so full of lead by ", "Chaingun");
        kills.put(" was put so full of lead by ", "Chaingun");

//MOD_GRENADE:
        kills.put(" was popped by ", "Grenade Launcher");
        kills.put(" caught ", "Grenade Launcher");
        kills.put(" caught ", "Grenade Launcher");
        kills.put(" tried to headbutt the grenade of ", "Grenade Launcher");
        kills.put(" was shredded by ", "Grenade Launcher");

//MOD_ROCKET:
        kills.put(" ate ", "Rocket Launcher");
        kills.put(" sucked on ", "Rocket Launcher");
        kills.put(" tried to play 'dodge the missile' with ", "Rocket Launcher");
        kills.put(" tried the 'patriot move' on the rocket from ", "Rocket Launcher");
        kills.put(" had a rocket stuffed down the throat by ", "Rocket Launcher");
        kills.put(" got a rocket up the tailpipe by ", "Rocket Launcher");
        kills.put(" tried to headbutt ", "Rocket Launcher");
        kills.put(" almost dodged ", "Rocket Launcher");
        kills.put(" was spread around the place by ", "Rocket Launcher");
        kills.put(" was gibbed by ", "Rocket Launcher");
        kills.put(" has been blown to smithereens by ", "Rocket Launcher");
        kills.put(" was blown to itsie bitsie tiny pieces by ", "Rocket Launcher");

//MOD_HYPERBLASTER:
        kills.put(" was melted by ", "Hyperblaster");
        kills.put(" was used by ", "Hyperblaster");
        kills.put(" was hyperblasted by ", "Hyperblaster");
        kills.put(" was pumped full of cells by ", "Hyperblaster");
        kills.put(" couldn't outrun the hyperblaster from ", "Hyperblaster");

//MOD_RAILGUN:
        kills.put(" was railed by ", "Railgun");
        kills.put(" played 'catch the slug' with ", "Railgun");
        kills.put(" bites the slug from ", "Railgun");
        kills.put(" caught the slug from ", "Railgun");
        kills.put(" got a slug put through him by ", "Railgun");
        kills.put(" got a slug put through her by ", "Railgun");
        kills.put(" got a slug put through it by ", "Railgun");
        kills.put(" was corkscrewed through his head by ", "Railgun");
        kills.put(" was corkscrewed through her head by ", "Railgun");
        kills.put(" was corkscrewed through it's head by ", "Railgun");
        kills.put(" had his body pierced with a slug from ", "Railgun");
        kills.put(" had her body pierced with a slug from ", "Railgun");
        kills.put(" had it's body pierced with a slug from ", "Railgun");
        kills.put(" had his brains blown out by ", "Railgun");
        kills.put(" had her brains blown out by ", "Railgun");
        kills.put(" had it's brains blown out by ", "Railgun");

//MOD_BFG:
        kills.put(" saw the pretty lights from ", "BFG10K");
        kills.put(" was diced by the BFG from ", "BFG10K");
        kills.put(" was disintegrated by ", "BFG10K");
        kills.put(" was flatched with the green light by ", "BFG10K");
        kills.put(" couldn't hide from ", "BFG10K");
        kills.put(" tried to soak up green energy from ", "BFG10K");
        kills.put(" doesn't know when to run from ", "BFG10K");
        kills.put(" 'saw the light' from ", "BFG10K");

//MOD_HANDGRENADE:
        kills.put(" caught ", "Hand Grenade");
        kills.put(" should watch more carefully for handgrenades from ", "Hand Grenade");
        kills.put(" didn't see ", "Hand Grenade");
        kills.put(" feels ", "Hand Grenade");

//MOD_TELEFRAG:
        suicides.put(" tried to invade ", "Telefrag");
        suicides.put(" should appreciate scotty more like ", "Telefrag");


        ready = true;
    }
}
