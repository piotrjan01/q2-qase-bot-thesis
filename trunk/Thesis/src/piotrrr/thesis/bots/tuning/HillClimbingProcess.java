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
public class HillClimbingProcess extends TuningProcessBase implements OptProcess {

    public HillClimbingProcess(int timescale, int iterations, int maxItScore, String mapName, int repetitions) {
        super(timescale, iterations, maxItScore, mapName, repetitions);
    }

    @Override
    protected List<NavConfig> generateNextSet(NavConfig best) {
        return generateAllNeighboursSystematically(best);
    }



}
