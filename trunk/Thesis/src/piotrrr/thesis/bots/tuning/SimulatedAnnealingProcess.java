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
public class SimulatedAnnealingProcess extends TuningProcessBase {

    private double t = 100;
    private int step = 0;

    public SimulatedAnnealingProcess(int timescale, int iterations, int maxItScore, String mapName, int repetitions) {
        super(timescale, iterations, maxItScore, mapName, repetitions);
    }

    @Override
    protected List<NavConfig> generateNextSet(NavConfig best) {
        decreaseTemperature();
        List<NavConfig> ret = new LinkedList<NavConfig>();

        double worseScore;
        NavConfig worseConfig;

        if (lastEvaluatedScore == bestScore) {
            //last was best, so worse is lastBest
            worseConfig = lastBest;
            worseScore = lastBestScore;
        } else {
            //last was not the best, so the worse is last
            worseConfig = lastEvaluated;
            worseScore = lastEvaluatedScore;
        }

        NavConfig toAdd = null;
        double p = getWorseChoiceProbability(bestScore, worseScore);
        String info = " t="+t+" p="+p;
        if (worseConfig != null && rand.nextDouble() < p) {
            //worse choice
            toAdd = findPossiblyRandomNeighbour(worseConfig);
            info = "neighbour of worse"+info;
        } else {
            //better choice
            toAdd = findPossiblyRandomNeighbour(best);
            info = "neighbour of better"+info;
        }       
        if (toAdd == null) {
            //if we found no neighbour, we clear visited memory and search again
            visited.clear();
            ret = generateNextSet(best);
            for (NavConfig c : ret) {
                c.additionalInfo += "; after clearing visited cache"+info;
            }
            return ret;
        }
        toAdd.additionalInfo += info;
        ret.add(toAdd);
        return ret;
    }

    private void decreaseTemperature() {
        step++;
        t = 100 / step;
    }

    private double getWorseChoiceProbability(double val1, double val2) {
        double diff = Math.abs(val1 - val2);
        return Math.exp(-diff / t);
    }
}
