/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.common.stats;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.TreeSet;
import soc.qase.tools.vecmath.Vector3f;

/**
 *
 * @author Piotr Gwizdała
 */
public class BotStatistic implements Serializable {
    
    public static class Kill implements Serializable {
        public String killer;
        public String victim;
        public String gunUsed;
        public int time;

        public Kill(String killer, String victim, String gunUsed, int time) {
            this.killer = killer;
            this.victim = victim;
            this.gunUsed = gunUsed;
            this.time = time;
        }
    }

    public static class Pickup implements Serializable {
        public String what;
        public Vector3f where;
        public int time;

        public Pickup(String what, Vector3f where, int time) {
            this.what = what;
            this.where = where;
            this.time = time;
        }
    }

    public static class Reward implements Serializable {
        String botName;
        double reward;
        public int time;

        public Reward(String botName, double reward, int time) {
            this.botName = botName;
            this.reward = reward;
            this.time = time;
        }

    }

    public LinkedList<Kill> kills = new LinkedList<Kill>();

    public LinkedList<Pickup> pickups = new LinkedList<BotStatistic.Pickup>();

    public LinkedList<Reward> rewards = new LinkedList<BotStatistic.Reward>();

    public String statsInfo = "no-info";

    private static BotStatistic instance = null;



    synchronized public static BotStatistic getInstance() {
        return instance;
    }

    synchronized public static BotStatistic createNewInstance() {
        instance = new BotStatistic();
        return instance;
    }

    synchronized public void addKill(int time, String killer, String victim, String gun) {
        kills.add(new Kill(killer, victim, gun, time));
    }

    synchronized public void addReward(String botName, double reward, int time) {
        rewards.add(new Reward(botName, reward, time));
    }

    synchronized public TreeSet<String> getAllKillingBotNames() {
        TreeSet<String> ret = new TreeSet<String>();
        for (Kill k : kills) {
            ret.add(k.killer);
            ret.add(k.victim);
        }
        return ret;
    }

     synchronized public TreeSet<String> getAllRewardedBotNames() {
        TreeSet<String> ret = new TreeSet<String>();
        for (Reward r : rewards)
            ret.add(r.botName);
        return ret;
    }

    synchronized public TreeSet<String> getAllBotFamilies() {
        TreeSet<String> ret = new TreeSet<String>();
        for (Kill k : kills) {
            if (k.killer.contains("-"))
                ret.add(k.killer.substring(0, k.killer.indexOf("-")));
            else ret.add(k.killer);
            if (k.victim.contains("-"))
                ret.add(k.victim.substring(0, k.victim.indexOf("-")));
            else ret.add(k.victim);
        }
        return ret;
    }

    synchronized public LinkedList<Kill> getAllKillsForGivenBot(String name) {
        LinkedList<Kill> ret = new LinkedList<BotStatistic.Kill>();
        for (Kill k : kills) {
            if (k.killer.equals(name)) ret.add(k);
        }
        return ret;
    }


    synchronized public void saveToFile(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static BotStatistic readFromFile(String fileName) {
        BotStatistic r = null;
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            r = (BotStatistic)ois.readObject();
        }
        catch (Exception e) {
            System.err.println("Error reading statisics: "+fileName);
        }
        return r;
    }


}
