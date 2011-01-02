/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.util.List;

/**
 *
 * @author piotrrr
 */
public class HillClimbingWithStepDecrease extends TuningProcessBase {

    int randNeighboursToAdd = 0;

    double currentStepSize = NavConfig.initialStepSize;

    public HillClimbingWithStepDecrease(int timescale, int iterations, int maxItScore, String mapName, int repetitions) {
        super(timescale, iterations, maxItScore, mapName, repetitions);
    }

    @Override
    protected List<NavConfig> generateNextSet(NavConfig best) {
        List<NavConfig> r = super.generateAllNeighboursSystematically(best);
        if (r.size() == 0) {
            currentStepSize /= 2;
            best.setStepSize(currentStepSize);
            visited.clear();
            resultsOfVisited.clear();
            r = super.generateAllNeighboursSystematically(best);
            return r;
        }
        for (int i=0; i<randNeighboursToAdd; i++) {
            NavConfig c = new NavConfig();
            c.randAllParams();
            if (visited.contains(c)) {
                i--;
                continue;
            }
            c.additionalInfo = "Randomly generated "+i;
            r.add(c);
        }
        return r;
    }

    
}
