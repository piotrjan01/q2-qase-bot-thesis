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
public class NoChangeProcess extends TuningProcessBase implements OptProcess {

    public NoChangeProcess(int timescale, int iterations, int maxItScore, String mapName, int repetitions) {
        super(timescale, iterations, maxItScore, mapName, repetitions);
    }

    @Override
    protected List<NavConfig> generateNextSet(NavConfig best) {
        LinkedList<NavConfig> ret = new LinkedList<NavConfig>();
        ret.add(best);
        return ret;
    }



}
