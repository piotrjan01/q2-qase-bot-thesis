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
    protected int repetitions;
    Random rand = new Random();
    HashSet<NavConfig> visited;
    LinkedList<DuelEvalResults> lastIterResults;
    LinkedList<DuelEvalResults> allResults;

    NavConfig best;
    double bestScore;

    NavConfig lastEvaluated = null;
    double lastEvaluatedScore;

    NavConfig lastBest = null;
    double lastBestScore;
    
    

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
        allResults = new LinkedList<DuelEvalResults>();
        lastIterResults = new LinkedList<DuelEvalResults>();
        AppConfig.timeScale = timescale;

        int i = 0;
        int e = 0;

        best = new NavConfig();
        best.setInitialParams();
        best.additionalInfo = "Initial config";
        bestScore = Double.NEGATIVE_INFINITY;
        lastBest = best;
        lastBestScore = bestScore;
        int bestEval = -1;
        while (i < iterations && running) {

            List<NavConfig> nList = generateNextSet(best);
            lastIterResults.clear();
            i++;
            
            if (nList.size() == 0) {
                Dbg.prn("Empty set - nothing to explore");
                stopProcess();
                return;
            }

            for (NavConfig conf : nList) {
                e++;

                double score = ConfigEvaluator.sequentialEvaluateConfig(conf, repetitions, itScore, mapName);
                visited.add(conf);
                lastEvaluated = conf;
                lastEvaluatedScore = score;

                DuelEvalResults result = new DuelEvalResults(ConfigEvaluator.gameStats,
                        "Config-eval-"+e,
                        "Iteration: "+i+
                        "\nConfig-eval-"+e+
                        "\nBest eval nr: " + bestEval+
                        "\nSteepest bot score: " + bestScore +
                        "\nCurrent bot score: " + score +
                        "\n\nTested config: " + conf.toString() +
                        "\nBest config: " + best.toString() +
                        "\nConfig diff from best:\n" + best.getDifferences(conf) +
                        "\nEval log:\n" + ConfigEvaluator.evalLog, score, i);

                allResults.add(result);
                lastIterResults.add(result);
                OptimizationRunner.getInstance().handleIterationResults(result);

                if (score > bestScore) {
                    lastBestScore = bestScore;
                    lastBest = best;
                    bestScore = score;                    
                    best = conf;
                    bestEval = e;
                }

            }
        }
        stopProcess();

    }

    public void stopProcess() {
        running = false;
        visited.clear();
        lastIterResults.clear();
        allResults.clear();
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
