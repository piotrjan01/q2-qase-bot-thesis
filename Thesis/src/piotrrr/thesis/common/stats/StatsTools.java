/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.common.stats;

/**
 *
 * @author piotrrr
 */
public class StatsTools {

    public static int getBotScore(String botName, BotStatistic stats) {
        int ret = 0;
        for (BotStatistic.Kill k : stats.kills)
            if (k.killer.equals(botName)) ret++;
        return ret;
    }

    public static int countBotsOfGivenFamilly(String family, BotStatistic stats) {
        int cnt = 0;
        for (String n : stats.getAllKillingBotNames()) {
            if (n.startsWith(family)) {
                cnt++;
            }
        }
        return cnt;
    }

}
