/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.bots.tuning;

import piotrrr.thesis.common.stats.StatsTools;

/**
 *
 * @author piotrrr
 */
public class OptResultsTools {

    public static String getOptTextDesctiption(OptResults res) {
        String ret = "";

        double [] bestRes = getMaxResultEvalAndIter(res);
        ret+="Best eval score: "+bestRes[0];
        ret+="\nBest eval nr: "+bestRes[2];
        ret+="\nBest eval's iteration: "+bestRes[1];

        ret+="\n\nResults mean: "+getResultsAvg(res);
        ret+="\nResults variance estimation: "+getResultsVarianceEst(res);

        ret+="\n\nEach evaluation relative score variance estimation: "+getEvalRelativeScoreAverageVariance(res);
        ret+="\n\nEach evaluation score variance estimation: "+getEvalScoreAverageVariance(res);


        return ret;
    }

    public static double getEvalRelativeScoreAverageVariance(OptResults res) {
        double ret = 0;
        for (DuelEvalResults r : res.iterResults) {
            ret += StatsTools.getBotTypeRelativeScoreVariance("LearnBot", r.stats);
        }
        return ret / res.iterResults.size();
    }

    public static double getEvalScoreAverageVariance(OptResults res) {
        double ret = 0;
        for (DuelEvalResults r : res.iterResults) {
            ret += StatsTools.getBotTypeScoreVariance("LearnBot", r.stats);
        }
        return ret / res.iterResults.size();
    }

    public static double getResultsAvg(OptResults res) {
        double avg=0;
        for (DuelEvalResults r : res.iterResults) {
            avg+=r.score;
        }
        return avg/res.iterResults.size();
    }

    public static double getResultsVarianceEst(OptResults res) {
        double avg = getResultsAvg(res);
        double var = 0;
        for (DuelEvalResults r : res.iterResults) {
            var += (r.score-avg)*(r.score-avg);
        }
        var /= res.iterResults.size()-1; //in estimation we divide by n-1
        return var;
    }

    public static double getMaxResult(OptResults res) {
        double maxRes = Double.NEGATIVE_INFINITY;
        for (DuelEvalResults r : res.iterResults) {
            if (r.score > maxRes) {
                maxRes = r.score;
            }
        }
        return maxRes;
    }

    public static double[] getMaxResultEvalAndIter(OptResults res) {
        double maxRes = Double.NEGATIVE_INFINITY;
        DuelEvalResults max = null;
        int bestInd = -1;
        for (DuelEvalResults r : res.iterResults) {
            if (r.score > maxRes) {
                maxRes = r.score;
                max = r;
                bestInd = res.iterResults.indexOf(r);
            }
        }
        if (max == null) return null;
        return new double [] {maxRes, max.iterNr, bestInd+1};
    }

}
