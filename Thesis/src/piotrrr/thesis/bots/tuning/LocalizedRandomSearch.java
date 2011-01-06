/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author piotrrr
 */
public class LocalizedRandomSearch extends TuningProcessBase {

    public static final boolean continueAfterVisitedAllNeighbours = true;
    private double t = 100.0;
    private int step = 0;

    private double currentParamStepSize = 0.05;

    public LocalizedRandomSearch(int timescale, int iterations, int maxItScore, String mapName, int repetitions) {
        super(timescale, iterations, maxItScore, mapName, repetitions);
    }

    @Override
    protected List<NavConfig> generateNextSet(NavConfig best) {

//        decreaseTemperature();
        List<NavConfig> ret = new LinkedList<NavConfig>();

        if (visited.size() == 0) {
            ret.add(best);
            return ret;
        }

        if (visited.size() == 1) {
            ret.add(generateLocallyRandomNeighbur(best));
            return ret;
        }

        NavConfig neew = visited.getLast();
        NavConfig curr = best;
        NavConfig toAdd = null;

        double neewScore = resultsOfVisited.getLast();
        double currScore = bestScore;

        String info = ""; // "t=" + t;

        if (neewScore > currScore) {
            //we take the new one
            toAdd = generateLocallyRandomNeighbur(neew);
            info += " found new better config";
        } else {
            double p = 0; //getWorseChoiceProbability(neewScore, currScore);
            info += " p=" + p;
            if (rand.nextDouble() < p) {
                //we take the worse one - new
                toAdd = findPossiblyRandomNeighbour(neew);
                info += " went for worse new config";
            } else {
                //we take the better - current
                toAdd = generateLocallyRandomNeighbur(curr);
                info += " went for better current config";
            }
        }     

        toAdd.additionalInfo += info;
        ret.add(toAdd);
        return ret;

    }

    private void decreaseTemperature() {
        step++;
        t = 100.0 / step;
    }

    private double getWorseChoiceProbability(double val1, double val2) {
        double diff = Math.abs(val1 - val2);
        return Math.exp(-diff / t);
    }


    private NavConfig generateLocallyRandomNeighbur(NavConfig from) {
        NavConfig ret = new NavConfig(from);
        int n = ret.getParamsCount();       
        for (int i=0; i<n; i++) {
            ret.addToParam(i, getRandomNumber(0, 0.22)*currentParamStepSize);
        }
        return ret;
    }

    private double getRandomNumber(double mean, double variance) {
        return mean + rand.nextGaussian() * variance;
    }


}
