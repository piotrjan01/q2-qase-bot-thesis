/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.rlbot.rl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import piotrrr.thesis.bots.rlbot.RLBot;

/**
 *
 * @author Piotr Gwizdała
 */
public class State {

    public static final int WPN_BLASTER = 0;
    public static final int WPN_SHOTGUN = 1;
    public static final int WPN_SUPER_SHOTGUN = 2;
    public static final int WPN_MACHINEGUN = 3;
    public static final int WPN_CHAINGUN = 4;
    public static final int WPN_GRENADES = 5;
    public static final int WPN_GRENADE_LAUNCHER = 6;
    public static final int WPN_ROCKET_LAUNCHER = 7;
    public static final int WPN_HYPERBLASTER = 8;
    public static final int WPN_RAILGUN = 9;
    public static final int WPN_BFG10K = 10;
    public static final int DIST_CLOSE = 11;
    public static final int DIST_MEDIUM = 12;
    public static final int DIST_FAR = 13;

    public static final int wpnMin = WPN_BLASTER;
    public static final int wpnMax = WPN_BFG10K;

    public static final int invIndexingShift = 7;

    
    private int wpn;
    private int dist;
    private boolean [] ownedGuns = new boolean[wpnMax-wpnMin+1];

    private RLBot bot;


    public State(int wpn, int dist, RLBot bot) {
        this.wpn = wpn;
        this.dist = dist;
        this.bot = bot;
        int j=0;
        for (int i=wpnMin; i<=wpnMax; i++) {
            if (bot.botHasItem(i-invIndexingShift)) ownedGuns[j]=true;
            else ownedGuns[j] = false;
            j++;
        }
    }

    public static int getWpnFromInventoryIndex(int index) {
        return index - invIndexingShift;
    }

    public int getWpnAsInventoryIndex() {
        return wpn + invIndexingShift;
    }

    @Override
    public boolean equals(Object obj) {
        State a = (State) obj;
        if (a.wpn == wpn) {
            if (a.dist == dist) {
                for (int i=0; i<ownedGuns.length; i++) {
                    if (ownedGuns[i] != a.ownedGuns[i]) return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (wpn + dist) % 256;
    }

    @Override
    public String toString() {
        String swpn = "unknown-weapon";
        String sdist = "unknown-distance";
        for (Field f : this.getClass().getFields()) {

            if (Modifier.isStatic(f.getModifiers())) {
                try {
                    if ((Integer) f.get(this) == this.wpn) {
                        swpn = f.getName();
                    }
                    if ((Integer) f.get(this) == this.dist) {
                        sdist = f.getName();
                    }
                } catch (Exception ex) {
                }
            }

        }
        return swpn + ":" + sdist;
    }

    public int getDist() {
        return dist;
    }

    public int getWpn() {
        return wpn;
    }



}
