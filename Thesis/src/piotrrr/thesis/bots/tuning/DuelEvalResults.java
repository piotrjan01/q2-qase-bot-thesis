/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.io.Serializable;
import piotrrr.thesis.common.stats.BotStatistic;

/**
 *
 * @author piotrrr
 */
public class DuelEvalResults implements Serializable {

    public BotStatistic stats;
    public String comment;
    public String shortName;
    public double score;
    public int iterNr;


    public DuelEvalResults(BotStatistic stats, String shortName, String comment, double score, int iterNr) {
        this.stats = stats;
        this.comment = comment;
        this.shortName = shortName;
        this.score = score;
        this.iterNr = iterNr;
    }

    public String toShortString() {
        return shortName;
    }

    @Override
    public String toString() {
        return comment;
    }    
}
