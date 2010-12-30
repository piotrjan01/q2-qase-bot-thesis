/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.common.stats;

import java.util.HashMap;
import java.util.LinkedList;
import piotrrr.thesis.common.stats.BotStatistic.Kill;

/**
 *
 * @author Piotr Gwizdała
 */
public class StatsTools {

    public static int getBotScore(String botName, BotStatistic stats) {
        if (stats == null) {
            return 0;
        }
        synchronized (stats) {

            int ret = 0;
            for (BotStatistic.Kill k : stats.kills) {
                if (k.killer.equals(botName)) {
                    ret++;
                }
            }
            return ret;
        }
    }

    public static int getBotDeaths(String botName, BotStatistic stats) {
        if (stats == null) {
            return 0;
        }
        synchronized (stats) {

            int ret = 0;
            for (BotStatistic.Kill k : stats.kills) {
                if (k.victim.equals(botName)) {
                    ret++;
                }
            }
            return ret;
        }
    }

    public static int countBotsOfGivenFamilly(String family, BotStatistic stats) {
        synchronized (stats) {
            int cnt = 0;
            for (String n : stats.getAllKillingBotNames()) {
                if (n.startsWith(family)) {
                    cnt++;
                }
            }
            return cnt;
        }
    }

    public static void removeKillsOfBot(String botName, BotStatistic stats) {
        if (stats == null || botName == null) {
            return;
        }
        LinkedList<Kill> newKills = new LinkedList<Kill>();
        for (BotStatistic.Kill k : stats.kills) {
            if (k.killer.equals(botName)) {
                continue;
            }
            if (k.victim.equals(botName)) {
                continue;
            }
            newKills.add(k);
        }
        stats.kills = newKills;
    }

    public static void removeEventsAfter(int time, BotStatistic stats) {

        LinkedList<BotStatistic.Kill> toRemovek = new LinkedList<BotStatistic.Kill>();
        for (Kill k : stats.kills) {
            if (k.time > time) {
                toRemovek.add(k);
            }
        }
        stats.kills.removeAll(toRemovek);

        LinkedList<BotStatistic.Pickup> toRemovep = new LinkedList<BotStatistic.Pickup>();
        for (BotStatistic.Pickup p : stats.pickups) {
            if (p.time > time) {
                toRemovep.add(p);
            }
        }
        stats.pickups.removeAll(toRemovep);

        LinkedList<BotStatistic.Reward> toRemover = new LinkedList<BotStatistic.Reward>();
        for (BotStatistic.Reward r : stats.rewards) {
            if (r.time > time) {
                toRemover.add(r);
            }
        }
        stats.rewards.removeAll(toRemover);

    }

    public static double getBotTypeScoreVariance(String botNamePrefix, BotStatistic stats) {

        HashMap<String, Integer> scores = new HashMap<String, Integer>();

        for (String f : stats.getAllKillingBotNames()) {
            scores.put(f, getBotScore(f, stats));
        }

        int botsCount = 0;
        double avg = 0;
        for (String bn : scores.keySet()) {
            if (bn.startsWith(botNamePrefix)) {
                avg += scores.get(bn);
                botsCount++;
            }
        }
        avg = avg / botsCount;

        double var = 0;
        for (String bn : scores.keySet()) {
            if (bn.startsWith(botNamePrefix)) {
                var += (scores.get(bn) - avg) * (scores.get(bn) - avg);
            }
        }
        if (botsCount == 1) {
            return Double.POSITIVE_INFINITY;
        }
        var = var / (botsCount - 1);
        return var;

    }

    public static double getBotTypeRelativeScoreVariance(String botNamePrefix, BotStatistic stats) {
        HashMap<String, Integer> scores = new HashMap<String, Integer>();

        for (String f : stats.getAllKillingBotNames()) {
            scores.put(f, getBotScore(f, stats) - getBotDeaths(f, stats));
        }

        int botsCount = 0;
        double avg = 0;
        for (String bn : scores.keySet()) {
            if (bn.startsWith(botNamePrefix)) {
                avg += scores.get(bn);
                botsCount++;
            }
        }
        avg = avg / botsCount;

        double var = 0;
        for (String bn : scores.keySet()) {
            if (bn.startsWith(botNamePrefix)) {
                var += (scores.get(bn) - avg) * (scores.get(bn) - avg);
            }
        }
        if (botsCount == 1) {
            return Double.POSITIVE_INFINITY;
        }
        var = var / (botsCount - 1);
        return var;

    }

    public static double getBotTypeScoreAverage(String botNamePrefix, BotStatistic stats) {

        HashMap<String, Integer> scores = new HashMap<String, Integer>();

        for (String f : stats.getAllKillingBotNames()) {
            scores.put(f, getBotScore(f, stats));
        }

        int botsCount = 0;
        double avg = 0;
        for (String bn : scores.keySet()) {
            if (bn.startsWith(botNamePrefix)) {
                avg += scores.get(bn);
                botsCount++;
            }
        }
        avg = avg / botsCount;
        return avg;

    }

    public static double getBotTypeRelativeScoreAverage(String botNamePrefix, BotStatistic stats) {
        HashMap<String, Integer> scores = new HashMap<String, Integer>();

        for (String f : stats.getAllKillingBotNames()) {
            scores.put(f, getBotScore(f, stats) - getBotDeaths(f, stats));
        }

        int botsCount = 0;
        double avg = 0;
        for (String bn : scores.keySet()) {
            if (bn.startsWith(botNamePrefix)) {
                avg += scores.get(bn);
                botsCount++;
            }
        }
        avg = avg / botsCount;

        return avg;

    }
}
