/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.gui.AppConfig;
import piotrrr.thesis.tools.Dbg;

/**
 *
 * @author piotrrr
 */
public class TuningProcessBase implements OptProcess {

    public static final boolean debug = AppConfig.debug;

    protected int timescale;
    protected int maxEvals;
    protected int itScore;
    protected String mapName;
    protected boolean running;
    protected int repetitions;
    Random rand = new Random();
    LinkedList<NavConfig> visited;
    LinkedList<Double> resultsOfVisited;


    NavConfig best;
    double bestScore;
    NavConfig lastEvaluated = null;
    double lastEvaluatedScore;
    NavConfig lastBest = null;
    double lastBestScore;
    public double tauThreshold;

    public TuningProcessBase(int timescale, int iterations, int maxItScore, String mapName, int repetitions) {
        this.timescale = timescale;
        this.maxEvals = iterations;
        this.mapName = mapName;
        this.itScore = maxItScore;
        this.repetitions = repetitions;
    }

    public void run() {
        running = true;

        visited = new LinkedList<NavConfig>();
        resultsOfVisited=new LinkedList<Double>();

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
        while (e < maxEvals && running) {

            List<NavConfig> nList = generateNextSet(best);
            i++;

            if (nList.size() == 0) {
                Dbg.prn("Empty set - nothing to explore");
                stopProcess();
                return;
            }

            for (NavConfig conf : nList) {
                e++;

                BotStatistic evalStats;
                double score;
                String evalLog;
                if (debug) {
                    score = FakeConfigEvaluator.sequentialEvaluateConfig(conf, repetitions, itScore, mapName);
                    evalStats = FakeConfigEvaluator.gameStats;
                    evalLog = FakeConfigEvaluator.evalLog;
                }
                else {
                    score = ConfigEvaluator.sequentialEvaluateConfig(conf, repetitions, itScore, mapName);
                    evalStats = ConfigEvaluator.gameStats;
                    evalLog = ConfigEvaluator.evalLog;
                }
                visited.add(conf);
                resultsOfVisited.add(score);

                
                lastEvaluated = conf;
                lastEvaluatedScore = score;

                String differences = " n/a";
                if (visited.size() >= 2) {
                    differences = visited.get(visited.size()-2).getDifferences(conf);
                }

                DuelEvalResults result = new DuelEvalResults(evalStats,
                        "Config-eval-" + e,
                        "Iteration: " + i +
                        "\nConfig-eval-" + e +
                        "\nBest eval nr: " + bestEval +
                        "\nSteepest bot score: " + bestScore +
                        "\nCurrent bot score: " + score +
                        "\n\nTested config: " + conf.toString() +
                        "\nBest config: " + best.toString() +
                        "\nConfig diff from last:\n" + differences +
                        "\nEval log:\n" + evalLog, score, i);

                OptimizationRunner.getInstance().handleEvaluationResults(result);

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
        resultsOfVisited.clear();
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

    protected NavConfig findPossiblyRandomNeighbour(NavConfig c1) {
        int pcount = c1.getParamsCount();
        NavConfig ret;
        for (int i = 0; i < 10; i++) {
            int toChange = rand.nextInt(pcount);
            ret = new NavConfig(c1);
            if (rand.nextBoolean()) {
                ret.incParam(toChange);
            } else {
                ret.decParam(toChange);
            }
            if (visited.contains(ret)) {
                continue;
            } else {
                return ret;
            }
        }
        ret = findNeighbourSystematically(c1);
        return ret;
    }

    protected List<NavConfig> generateNextSet(NavConfig best) {
        return new LinkedList<NavConfig>();
    }

    /**
     * Generate all neighbours that were not visited
     * @param c1
     * @return
     */
    protected List<NavConfig> generateAllNeighboursSystematically(NavConfig c1) {
        return generateAllNeighboursSystematically(c1, true);
    }

    /**
     * Generate all neighbours for each param by first inc and dec each param
     * @param c1
     * @param excludeVisited
     * @return
     */
    protected List<NavConfig> generateAllNeighboursSystematically(NavConfig c1, boolean excludeVisited) {
        LinkedList<NavConfig> res = new LinkedList<NavConfig>();
        int pcount = c1.getParamsCount();
        for (int i = 0; i < pcount; i++) {
            NavConfig c2 = new NavConfig(c1);
            c2.incParam(i);
            c2.additionalInfo = "Neighbour with inc of "+c2.getParamsName(i);
            if (!excludeVisited || !visited.contains(c2)) {
                res.add(c2);
            }

            c2 = new NavConfig(c1);
            c2.decParam(i);
            c2.additionalInfo = "Neighbour with dec of "+c2.getParamsName(i);
            if (!excludeVisited || !visited.contains(c2)) {
                res.add(c2);
            }
        }
        return res;
    }

    public void unblockProcess() {
        //todo
    }
}
