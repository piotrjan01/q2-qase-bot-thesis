/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.bots.tuning;

import java.io.Serializable;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.common.stats.StatsTools;

/**
 *
 * @author piotrrr
 */
public class EvalResults implements Serializable {

    public BotStatistic stats;
    public String bot1Name;
    public String bot2Name;
    public NavConfig bot1Conf;
    public NavConfig bot2Conf;

    public EvalResults(BotStatistic stats, String bot1Name, String bot2Name, NavConfig bot1Conf, NavConfig bot2Conf) {
        this.stats = stats;
        this.bot1Name = bot1Name;
        this.bot2Name = bot2Name;
        this.bot1Conf = bot1Conf;
        this.bot2Conf = bot2Conf;
    }

    public String toShortString() {
        return bot1Name+" vs. "+bot2Name;
    }

    @Override
    public String toString() {
        String ret = bot1Name+" vs. "+bot2Name+"\n";
        ret += ""+StatsTools.getBotScore(bot1Name, stats)
                + " : "
                + StatsTools.getBotScore(bot2Name, stats)
                +"\n";
        ret += "bot1 config: "+bot1Conf.toString()+"\n";
        ret += "bot2 config: "+bot2Conf.toString()+"\n";
        StatsTools.getBotScore(bot1Name, stats);
        return ret;
    }



}
