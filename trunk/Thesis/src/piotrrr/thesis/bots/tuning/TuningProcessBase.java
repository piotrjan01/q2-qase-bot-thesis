/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import piotrrr.thesis.gui.AppConfig;
import piotrrr.thesis.tools.Dbg;

/**
 *
 * @author piotrrr
 */
public class TuningProcessBase implements OptProcess {

    protected int timescale;
    protected int iterations;
    protected int itScore;
    protected String mapName;
    protected boolean running;
    protected int repetitions = 5;
    Random rand = new Random();
    HashSet<NavConfig> visited;

    public TuningProcessBase(int timescale, int iterations, int maxItScore, String mapName, int repetitions) {
        this.timescale = timescale;
        this.iterations = iterations;
        this.mapName = mapName;
        this.itScore = maxItScore;
        this.repetitions = repetitions;
    }

    public void run() {
        running = true;
        visited = new HashSet<NavConfig>();
        AppConfig.timeScale = timescale;

        int i = 0;

        NavConfig best = new NavConfig();
        best.setInitialParams();
        best.additionalInfo = "Initial config";
        double bestScore = Double.NEGATIVE_INFINITY;
        int bestIter = -1;
        while (i < iterations && running) {

            List<NavConfig> nList = generateNextSet(best);
            
            if (nList.size() == 0) {
                Dbg.prn("Empty set - nothing to explore");
                stopProcess();
                return;
            }

            for (NavConfig conf : nList) {

                double score = ConfigEvaluator.sequentialEvaluateConfig(conf, i, repetitions, itScore, mapName);
                visited.add(conf);

                DuelEvalResults result = new DuelEvalResults(ConfigEvaluator.gameStats,
                        "Config-eval-"+i,
                        "Config-eval-"+i+
                        "\nBest eval nr: " + bestIter+
                        "\nSteepest bot score: " + bestScore +
                        "\nCurrent bot score: " + score +
                        "\n\nTested config: " + conf.toString() +
                        "\nBest config: " + best.toString() +
                        "\nConfig diff from best:\n" + best.getDifferences(conf) +
                        "\nEval log:\n" + ConfigEvaluator.evalLog, score);
                OptimizationRunner.getInstance().handleIterationResults(result);

                if (score > bestScore) {
                    bestScore = score;
                    best = conf;
                    bestIter = i;
                }
                i++;
            }
        }
        stopProcess();

    }

    public void stopProcess() {
        running = false;
        visited.clear();
    }

    protected NavConfig findNeighbourSystematically(NavConfig c1) {
        int pcount = c1.getParamsCount();
        for (int i = 0; i < pcount; i++) {
            NavConfig c2 = new NavConfig(c1);
            c2.incParam(i);
            if (!visited.contains(c2)) {
                return c2;
            }

            c2 = new NavConfig(c1);
            c2.decParam(i);
            if (!visited.contains(c2)) {
                return c2;
            }
        }
        return null;
    }

    protected List<NavConfig> generateNextSet(NavConfig best) {
        return new LinkedList<NavConfig>();
    }

    protected List<NavConfig> generateAllNeighbours(NavConfig c1) {
        LinkedList<NavConfig> res = new LinkedList<NavConfig>();
        int pcount = c1.getParamsCount();
        for (int i = 0; i < pcount; i++) {
            NavConfig c2 = new NavConfig(c1);
            c2.incParam(i);
            c2.additionalInfo = "Neighbour";
            if (!visited.contains(c2)) {
                res.add(c2);
            }

            c2 = new NavConfig(c1);
            c2.decParam(i);
            c2.additionalInfo = "Neighbour";
            if (!visited.contains(c2)) {
                res.add(c2);
            }
        }
        return res;
    }

    public void unblockProcess() {
        //todo
    }
}
