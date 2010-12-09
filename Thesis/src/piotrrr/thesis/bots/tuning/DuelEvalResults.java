/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.io.Serializable;
import java.util.TreeSet;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.common.stats.StatsTools;

/**
 *
 * @author piotrrr
 */
public class DuelEvalResults implements Serializable {

    public BotStatistic stats;
    public String comment;
    public String shortName;
    public int score;


    public DuelEvalResults(BotStatistic stats, String shortName, String comment, int score) {
        this.stats = stats;
        this.comment = comment;
        this.shortName = shortName;
        this.score = score;
    }

    public String toShortString() {
        return shortName;
    }

    @Override
    public String toString() {
        return comment;
    }    
}
