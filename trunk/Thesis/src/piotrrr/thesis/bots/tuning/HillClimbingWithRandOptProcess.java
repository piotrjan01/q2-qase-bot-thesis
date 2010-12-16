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
public class HillClimbingWithRandOptProcess extends TuningProcessBase {

    int randNeighboursToAdd = 3;

    public HillClimbingWithRandOptProcess(int timescale, int iterations, int maxItScore, String mapName, int repetitions) {
        super(timescale, iterations, maxItScore, mapName, repetitions);
    }

    @Override
    protected List<NavConfig> generateNextSet(NavConfig best) {
        List<NavConfig> r = super.generateAllNeighbours(best);
        if (r.size() == 0) return r;
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
