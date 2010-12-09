/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.common.stats;

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
        if (stats == null || botName == null) return;
        LinkedList<Kill> newKills = new LinkedList<Kill>();
        for (BotStatistic.Kill k : stats.kills) {
            if (k.killer.equals(botName)) continue;
            if (k.victim.equals(botName)) continue;
            newKills.add(k);
        }
        stats.kills = newKills;
    }
}
