/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.rlbot.rl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.Random;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import soc.qase.state.PlayerGun;

/**
 *
 * @author Piotr Gwizdała
 */
public class Action {

    public static final int WPN_BLASTER = 1;
    public static final int WPN_SHOTGUN = 2;
    public static final int WPN_SUPER_SHOTGUN = 3;
    public static final int WPN_MACHINEGUN = 4;
    public static final int WPN_CHAINGUN = 5;
    public static final int WPN_GRENADES = 6;
    public static final int WPN_GRENADE_LAUNCHER = 7;
    public static final int WPN_ROCKET_LAUNCHER = 8;
    public static final int WPN_HYPERBLASTER = 9;
    public static final int WPN_RAILGUN = 10;
    public static final int WPN_BFG10K = 11;
    public static final int CB_CLOSER = 12;
    public static final int CB_DISTANT = 13;
    public static final int CB_RETREAT = 14;
    public static final int[] prohibitedWpnChngs = {WPN_GRENADES};
    public static final int minWpn = 1;
    public static final int maxWpn = 11;
    public static final int minCmbtMov = 12;
    public static final int maxCmbtMov = 14;
    public static final int actToInvShift = 6;
    int wpnChange = WPN_BLASTER;
    int combatMove = CB_DISTANT;

    public Action(int action, int combatMove) {
        if (action < minWpn || action > maxWpn) {
            action = minWpn;
        }
        this.wpnChange = action;
        this.combatMove = combatMove;
    }

    public static boolean isChangeWeaponAction(int action) {
        return (action >= WPN_BLASTER && action <= WPN_BFG10K);
    }

    public int actionToInventoryIndex() {
        return actionToInventoryIndex(this.wpnChange);
    }

    public static int actionToInventoryIndex(int action) {
        if (!isChangeWeaponAction(action)) {
            action = WPN_BLASTER;
        }
        return action + actToInvShift;
    }

    public int getCombatMove() {
        return combatMove;
    }

    @Override
    public int hashCode() {
        return wpnChange % 256;
    }

    @Override
    public String toString() {
        String wpn = "WPN_UNKNOWN";
        String cmb = "CB_UNKNOWN";
        for (Field f : this.getClass().getFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                try {
                    if (f.getName().startsWith("WPN")) {
                        if ((Integer) f.get(this) == this.wpnChange) {
                            wpn = f.getName();
                        }
                    }
                    if (f.getName().startsWith("CB")) {
                        if ((Integer) f.get(this) == this.combatMove) {
                            cmb = f.getName();
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }
        return cmb + " : " + wpn;
    }

    public static Action[] getAllActionsArray() {
        LinkedList<Action> list = new LinkedList<Action>();
        for (int i = minWpn; i <= maxWpn; i++) {
            for (int j = minCmbtMov; j <= maxCmbtMov; j++) {
                list.add(new Action(i, j));
            }
        }
        Action[] ret = new Action[list.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = list.pop();
        }
        return ret;
    }

    public static boolean isProhibited(Action action) {
        for (int a : prohibitedWpnChngs) {
            if (a == action.wpnChange) {
                return true;
            }
        }
        return false;
    }

//    public int getShootingMode() {
//        return shootingMode;
//    }
    public static boolean[] getActionsAvailability(MapBotBase b) {
        Action[] acts = getAllActionsArray();
        boolean[] ret = new boolean[acts.length];
        for (int i = 0; i < ret.length; i++) {
            int ind = acts[i].actionToInventoryIndex();
            if (ind == 7) {
                ret[i] = true;
            } else {
                ret[i] = b.botHasItem(ind) &&
                        b.botHasItem(PlayerGun.getAmmoInventoryIndexByGun(ind));
            }
        }
        return ret;
    }
}
