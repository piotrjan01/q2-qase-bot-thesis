/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.util.LinkedList;
import java.util.List;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.gui.AppConfig;
import piotrrr.thesis.tools.Dbg;

/**
 *
 * @author piotrrr
 */
public class HillClimbingWithGradient extends TuningProcessBase {

    int randNeighboursToAdd = 0;

    double currentStepSize = NavConfig.initialStepSize;

    public HillClimbingWithGradient(int timescale, int iterations, int maxItScore, String mapName, int repetitions) {
        super(timescale, iterations, maxItScore, mapName, repetitions);
    }

    private double[] gradientEstimate = null;

    private double akGain;

    private double ckGain;

    private double c = 0.4;

    private double a = 0.01;

    private double alpha = 0.25;

    private double gamma = 0.7;

    @Override
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
        NavConfig initial = null;
        while (e < maxEvals && running) {

            
            if (initial == null) {
                initial = best;
            }
            else {
                initial = getNewConfigAccordingToGradient(initial);
            }

            ckGain = c/Math.pow(i+1, gamma);
            akGain = a/Math.pow(i+1, alpha);

            List<NavConfig> nList = generateAllNeighboursSystematically(initial, false, ckGain);
            gradientEstimate = new double [nList.size()/2]; 
            i++;     

            int param=0;
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


                param++;

                //after each 2 evaluations
                if (param % 2 == 0) {
                    gradientEstimate[param/2-1] = (lastEvaluatedScore-score)/(2*currentStepSize);
                    evalLog += "\nAdded gradient estimate for param "+best.getParamsName(param/2-1)+": "+gradientEstimate[param/2-1];
                    evalLog += "\nakGain="+akGain+" ckGain="+ckGain;
                }


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

              
                lastEvaluated = conf;
                lastEvaluatedScore = score;

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


    NavConfig getNewConfigAccordingToGradient(NavConfig orig) {
        NavConfig ret = new NavConfig(orig);

        int i=0;
        for (double d : gradientEstimate) {
            ret.addToParam(i, d*akGain);
            i++;
        }

        return ret;

    }

    protected List<NavConfig> generateAllNeighboursSystematically(NavConfig c1, boolean excludeVisited, double stepSize) {
        LinkedList<NavConfig> res = new LinkedList<NavConfig>();
        int pcount = c1.getParamsCount();
        for (int i = 0; i < pcount; i++) {
            NavConfig c2 = new NavConfig(c1);
            c2.addToParam(i, stepSize);
            c2.additionalInfo = "Neighbour with inc of "+c2.getParamsName(i)+" by "+stepSize;
            if (!excludeVisited || !visited.contains(c2)) {
                res.add(c2);
            }

            c2 = new NavConfig(c1);
            c2.addToParam(i, -stepSize);
            c2.additionalInfo = "Neighbour with dec of "+c2.getParamsName(i)+" by "+stepSize;
            if (!excludeVisited || !visited.contains(c2)) {
                res.add(c2);
            }
        }
        return res;
    }


   
    
}
