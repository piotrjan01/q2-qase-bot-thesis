/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import piotrrr.thesis.bots.learnbot.LearnBot;
import piotrrr.thesis.bots.referencebot.ReferenceBot;
import piotrrr.thesis.common.jobs.GlobalKillsStatsJob;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.common.stats.StatsTools;
import piotrrr.thesis.gui.AppConfig;
import piotrrr.thesis.gui.MyPopUpDialog;
import piotrrr.thesis.tools.Dbg;

/**
 *
 * @author piotrrr
 */
public class HillClimbingProcess implements OptProcess {

    private int timescale;
    private int iterations;
    private int itScore;
    private String mapName;
    private boolean running;
    private int repetitions = 5;
    Random rand = new Random();
    HashSet<NavConfig> visited;

    public HillClimbingProcess(int timescale, int iterations, int maxItScore, String mapName, int repetitions) {
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
        int botNr = 0;
        NavConfig steepest = new NavConfig();
        steepest.randAllParams();
        int steepestScore = Integer.MIN_VALUE;
        while (i < iterations && running) {

            List<NavConfig> neigh = generateNeighbours(steepest);
            NavConfig initial = steepest;

            if (neigh.size() == 0) {
                Dbg.prn("==========> No neighbours to explore! ");
                MyPopUpDialog.showMyDialogBox("Optimization finished",
                        "Optimization finished: There are no more neighbours to explore.",
                        MyPopUpDialog.info);
                break;
            }

            

            for (NavConfig nc : neigh) {
                int score = ConfigEvaluator.sequentialEvaluateConfig(nc, i, repetitions, itScore, mapName);
                visited.add(nc);

                DuelEvalResults result = new DuelEvalResults(ConfigEvaluator.gameStats,
                        /*bot.getBotName() + */ " vs. RefBot",
                        /*bot.getBotName() + */ " vs. RefBot" +
                        "\nBest bot: " + /*bestBotName +*/
                        "\nSteepest bot score: " + steepestScore +
                        "\nCurrent bot score: " + score +
                        "\nTested config: " + nc.toString() +
                        "\nConfig diff from initial:\n" + initial.getDifferences(nc), score);
                OptimizationRunner.getInstance().handleIterationResults(result);

                if (score > steepestScore) {
                    steepestScore = score;
                    steepest = nc;
                    /* bestBotName = bot.getBotName();*/
                }

            }

            botNr += 1;
            i++;
        }
        stopProcess();

    }

   

    public static void main(String[] args) {
        HillClimbingProcess pr = new HillClimbingProcess(100, 10, 20, "map1", 5);
        pr.run();
    }

    public void stopProcess() {
//        if (bot != null) {
//            bot.disconnect();
//        }
//        if (refBot != null) {
//            refBot.disconnect();
//        }
        running = false;
        visited.clear();
    }

    private NavConfig findNeighbour(NavConfig c1) {
        int pcount = c1.getParamsCount();

        NavConfig c2 = null;

        //We try 10 times to find a neighbour randomly
        for (int i = 0; i < 10; i++) {
            int pi = rand.nextInt(pcount);
            c2 = new NavConfig(c1);
            if (rand.nextBoolean()) {
                c2.incParam(pi);
            } else {
                c2.decParam(pi);
            }
            if (!visited.contains(c2)) {
                break;
            }
        }
        if (c2 != null) {
            visited.add(c2);
            return c2;
        } else {
            c2 = findNeighbourSystematically(c1);
            return c2;
        }
    }

    private NavConfig findNeighbourSystematically(NavConfig c1) {
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

    private List<NavConfig> generateNeighbours(NavConfig c1) {
        LinkedList<NavConfig> res = new LinkedList<NavConfig>();
        int pcount = c1.getParamsCount();
        for (int i = 0; i < pcount; i++) {
            NavConfig c2 = new NavConfig(c1);
            c2.incParam(i);
            if (!visited.contains(c2)) {
                res.add(c2);
            }

            c2 = new NavConfig(c1);
            c2.decParam(i);
            if (!visited.contains(c2)) {
                res.add(c2);
            }
        }
        return res;
    }

    public void unblockProcess() {
        //unblock = true;
    }
}
