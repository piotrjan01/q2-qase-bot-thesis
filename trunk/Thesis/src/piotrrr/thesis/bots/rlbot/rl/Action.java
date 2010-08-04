/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.bots.rlbot.rl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author piotrrr
 */
public class Action {

    public static final int NO_ACTION = 0;
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

    public static final int NO_FIRE = 0;
    public static final int FIRE = 1;
    public static final int FIRE_PREDICTED = 2;

    public static final int [] prohibitedWpnChngs = {WPN_GRENADES};

    public static final int minWpn = 0;
    public static final int maxWpn = 11;

    public static final int minSht = 0;
    public static final int maxSht = 2;

    public static final int actToInvShift = 6;
    

    int wpnChange = NO_ACTION;

    int shootingMode = 0;

    public Action(int action, int shootingMode) {
        if (action < minWpn || action > maxWpn) action = minWpn;
        this.wpnChange = action;
        this.shootingMode = shootingMode;
    }


    public static boolean isChangeWeaponAction(int action) {
        return (action >= WPN_BLASTER && action <= WPN_BFG10K);
    }

    public int actionToInventoryIndex() {
        return actionToInventoryIndex(this.wpnChange);
    }

    public static int actionToInventoryIndex(int action) {
        if ( ! isChangeWeaponAction(action)) action = WPN_BLASTER;
        return action+actToInvShift;
    }


    @Override
    public int hashCode() {
        return wpnChange % 256;
    }

    public int getWpnChange() {
        return wpnChange;
    }

    @Override
    public String toString() {
        for (Field f : this.getClass().getFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                try {
                    if ((Integer) f.get(this) == this.wpnChange) {
                        return f.getName();
                    }
                } catch (Exception ex) {

                }
            }
        }
        return "unknown action";
    }

    public static Action [] getAllActionsArray() {
        LinkedList<Action> list = new LinkedList<Action>();
        for (int i=minWpn; i<=maxWpn; i++) {
            for (int j=minSht; j<=maxSht; j++) {
                list.add(new Action(i, j));
            }
        }
        Action [] ret = new Action[list.size()];
        for (int i=0; i<ret.length; i++) ret[i] = list.pop();
        return ret;
    }

    public static boolean isProhibited(Action action) {
        for (int a : prohibitedWpnChngs) {
            if (a == action.wpnChange) return true;
        }
        return false;
    }

    public int getShootingMode() {
        return shootingMode;
    }

    

}
