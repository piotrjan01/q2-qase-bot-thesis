/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.util.HashSet;
import java.util.Random;
import piotrrr.thesis.bots.learnbot.LearnBot;
import piotrrr.thesis.common.jobs.GlobalKillsStatsJob;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.common.stats.StatsTools;
import piotrrr.thesis.gui.AppConfig;
import piotrrr.thesis.tools.Dbg;

/**
 *
 * @author piotrrr
 */
public class HillClimbingProcessWithoutReferenceBot implements OptProcess {

    private int timescale;
    private int iterations;
    private int itScore;
    private String mapName;
    private BotStatistic gameStats;
    private LearnBot bot1;
    private LearnBot bot2;
    private boolean running;
    private boolean unblock = false;
    Random rand = new Random();
    HashSet<NavConfig> visited;

    public HillClimbingProcessWithoutReferenceBot(int timescale, int iterations, int maxItScore, String mapName) {
        this.timescale = timescale;
        this.iterations = iterations;
        this.mapName = mapName;
        this.itScore = maxItScore;
    }

    public void run() {
        running = true;
        visited = new HashSet<NavConfig>();
        AppConfig.timeScale = timescale;
        gameStats = BotStatistic.createNewInstance();

        OptimizationRunner.getInstance().runLocalQ2Server(AppConfig.serverPort, mapName);
        sleep(5000);

        NavConfig c1 = new NavConfig();
        c1.randAllParams();

        int i = 0;
        int botNr = 0;
        while (i < iterations && running) {
            NavConfig c2 = findNeighbour(c1);

            if (c2 == null) {
                Dbg.prn("==========> No neighbours to explore! ");
                break;
            }

            int[] res = evaluateConfigs(c1, c2, botNr, botNr + 1);

            DuelEvalResults result = new DuelEvalResults(gameStats,
                    bot1.getBotName() + "vs. " + bot2.getBotName(),
                    "Config 1:\n" + bot1.nConfig.toString() +
                    "Config 2:\n" + bot2.nConfig + "\nConfig diff:\n" + c1.getDifferences(c2), res[1] - res[0]);
            OptimizationRunner.getInstance().handleIterationResults(result);

            bot1 = null;
            bot2 = null;

            if (res[1] > res[0]) {
                NavConfig tmp = c1;
                c1 = c2;
                c2 = tmp;
            }


            botNr += 2;
            i++;
        }
        stopProcess();

    }

    private int[] evaluateConfigs(NavConfig nc1, NavConfig nc2, int bn1, int bn2) {
        int[] ret = new int[2];
        boolean evalRuns = true;
        bot1 = OptimizationRunner.getInstance().connectLearnBot(bn1, AppConfig.serverPort);
        bot1.nConfig = nc1;
        bot1.addBotJob(new GlobalKillsStatsJob(bot1));
        bot2 = OptimizationRunner.getInstance().connectLearnBot(bn2, AppConfig.serverPort);
        bot2.nConfig = nc2;
        gameStats = BotStatistic.createNewInstance();
        while (evalRuns && running) {
            sleep(1000);
            int s1 = StatsTools.getBotScore(bot1.getBotName(), gameStats);
            int s2 = StatsTools.getBotScore(bot2.getBotName(), gameStats);
            if (s1 >= itScore || s2 >= itScore) {
                ret[0] = s1;
                ret[1] = s2;
                bot1.disconnect();
                bot2.disconnect();
                sleep(2000);
                return ret;
            }
            if (unblock) {
                unblock = false;
                return evaluateConfigs(nc1, nc2, bn1, bn2);
            }
        }
        return ret;
    }

    private void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void stopProcess() {
        if (bot1 != null) {
            bot1.disconnect();
        }
        if (bot2 != null) {
            bot2.disconnect();
        }
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

    public void unblockProcess() {
        unblock = true;
    }
}
