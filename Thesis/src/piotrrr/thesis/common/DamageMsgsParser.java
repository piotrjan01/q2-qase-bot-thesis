/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.common;

import java.util.HashMap;

/**
 *
 * @author piotrrr
 */
public class DamageMsgsParser {

    public static class Damage {

        String attacker;
        String victim;
        int lostHealth;
        int lostArmor;
        int frameNumberOfLastUpdate;

        public Damage(String attacker, String victim, int lostHealth, int lostArmor, int frameNumber) {
            this.attacker = attacker;
            this.victim = victim;
            this.lostHealth = lostHealth;
            this.lostArmor = lostArmor;
            this.frameNumberOfLastUpdate = frameNumber;
        }

        @Override
        public boolean equals(Object obj) {
            Damage d = (Damage) obj;
            return attacker.equals(d.attacker) && victim.equals(d.victim) &&
                    lostHealth == d.lostHealth && lostArmor == d.lostArmor &&
                    frameNumberOfLastUpdate == d.frameNumberOfLastUpdate;
        }

        @Override
        public int hashCode() {
            return attacker.hashCode();
        }
    }
    public static HashMap<String, Damage> dmgs = new HashMap<String, DamageMsgsParser.Damage>();

    /**
     * Remembers a damage inflicted by given bot. It is remembered until the reset() method is called
     * @param msg
     * @param frameNumber
     * @return
     */
    public static boolean parseMessage(String msg, int frameNumber) {
        if (!msg.contains(" hurt ") || !msg.contains(", h=") || !msg.contains(" a=")) {
            return false;
        }
        String at = msg.substring(0, msg.indexOf(" hurt "));
        String vi = msg.substring(msg.indexOf(" hurt ") + 6, msg.indexOf(", h="));
        int h = Integer.parseInt(msg.substring(msg.indexOf(", h=") + 4, msg.indexOf(" a=")));
        int a = Integer.parseInt(msg.substring(msg.indexOf(" a=") + 3));
        Damage nd = new Damage(at, vi, h, a, frameNumber);
        Damage od = dmgs.get(at);
        if (od != null) {
            od.lostHealth += nd.lostHealth;
            od.lostArmor += nd.lostArmor;
            od.frameNumberOfLastUpdate = nd.frameNumberOfLastUpdate;
        }
        else dmgs.put(at, nd);
        
        return true;
    }

    /**
     * Returns the known damage inflicted by given bot since last reset() method call.
     * @param attacker
     * @return
     */
    public static int[] getInflictedDamage(String attacker) {
        int[] r = new int[2];
        Damage d = dmgs.get(attacker);
        if (d != null) {
            r[0] = d.lostHealth;
            r[1] = d.lostArmor;
        }
        return r;
    }

    public static void reset(String attacker) {
        dmgs.remove(attacker);
    }

    /**
     * Test
     * @param args
     */
    public static void main(String [] args) {
        String m1 = "Tom hurt Jerry, h=10 a=5";
        String m2 = "Jerry hurt Tom, h=5 a=2";

        parseMessage(m1, 10);

        int [] r = getInflictedDamage("Tom");
        for (int i=0; i< r.length;i++)System.out.print(r[i]+" ");
        System.out.println();

        r = getInflictedDamage("Jerry");
        for (int i=0; i< r.length;i++)System.out.print(r[i]+" ");
        System.out.println();

        reset("Tom"); reset("Jerry");

        parseMessage(m2, 12);
        parseMessage(m2, 12);
        parseMessage(m2, 12);
        parseMessage(m2, 12);
        
        r = getInflictedDamage("Tom");
        for (int i=0; i< r.length;i++)System.out.print(r[i]+" ");
        System.out.println();

        r = getInflictedDamage("Jerry");
        for (int i=0; i< r.length;i++)System.out.print(r[i]+" ");
        System.out.println();

        r = getInflictedDamage("Tom");
        for (int i=0; i< r.length;i++)System.out.print(r[i]+" ");
        System.out.println();

        r = getInflictedDamage("Jerry");
        for (int i=0; i< r.length;i++)System.out.print(r[i]+" ");
        System.out.println();

        parseMessage(m1, 10);
        parseMessage(m1, 10);

        r = getInflictedDamage("Tom");
        for (int i=0; i< r.length;i++)System.out.print(r[i]+" ");
        System.out.println();

        r = getInflictedDamage("Jerry");
        for (int i=0; i< r.length;i++)System.out.print(r[i]+" ");
        System.out.println();

    }

}
