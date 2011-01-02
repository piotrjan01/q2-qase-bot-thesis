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

    public static final boolean stopIfVisitedAllNeighbours = true;
    private double t = 100.0;
    private int step = 0;

    private double currentParamStepSize = NavConfig.initialStepSize;

    public SimulatedAnnealingProcess(int timescale, int iterations, int maxItScore, String mapName, int repetitions) {
        super(timescale, iterations, maxItScore, mapName, repetitions);
    }

    @Override
    protected List<NavConfig> generateNextSet(NavConfig best) {

        decreaseTemperature();
        List<NavConfig> ret = new LinkedList<NavConfig>();

        if (visited.size() == 0) {
            ret.add(best);
            return ret;
        }

        if (visited.size() == 1) {
            ret.add(findPossiblyRandomNeighbour(best));
            return ret;
        }

        NavConfig neew = visited.getLast();
        NavConfig curr = best;
        NavConfig toAdd = null;

        double neewScore = resultsOfVisited.getLast();
        double currScore = bestScore;

        String info = "t=" + t;

        if (neewScore > currScore) {
            //we take the new one
            toAdd = findPossiblyRandomNeighbour(neew);
            info += " found new better config";
        } else {
            double p = getWorseChoiceProbability(neewScore, currScore);
            info += " p=" + p;
            if (rand.nextDouble() < p) {
                //we take the worse one - new
                toAdd = findPossiblyRandomNeighbour(neew);
                info += " went for worse new config";
            } else {
                //we take the better - current
                toAdd = findPossiblyRandomNeighbour(curr);
                info += " went for better current config";
            }
        }

        if (toAdd == null) {
            currentParamStepSize /= 2;
            if (stopIfVisitedAllNeighbours) {
                //if we found no neighbour, we clear visited memory and search again
                visited.clear();
                resultsOfVisited.clear();
                toAdd = findPossiblyRandomNeighbour(best);                
                toAdd.setStepSize(currentParamStepSize);
                info += " clearing visited and setting step size="+currentParamStepSize;
                
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
}
