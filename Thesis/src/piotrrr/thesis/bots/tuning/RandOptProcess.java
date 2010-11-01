/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

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
public class RandOptProcess implements OptProcess {

    private int timescale;
    private int iterations;
    private int itScore = 5;
    private String mapName;
    private BotStatistic gameStats;
    private LearnBot bot1;
    private LearnBot bot2;
    private boolean running;

    public RandOptProcess(int timescale, int iterations, int maxItScore, String mapName) {
        this.timescale = timescale;
        this.iterations = iterations;
        this.mapName = mapName;
        this.itScore = maxItScore;
    }

    public void run() {
        running = true;
        AppConfig.timeScale = timescale;
        gameStats = BotStatistic.createNewInstance();

        OptimizationRunner.getInstance().runLocalQ2Server(AppConfig.serverPort, mapName);
        sleep(5000);
        bot1 = OptimizationRunner.getInstance().connectLearnBot(1, AppConfig.serverPort);
        bot1.nConfig.randAllParams();
        bot1.addBotJob(new GlobalKillsStatsJob(bot1));
        bot2 = OptimizationRunner.getInstance().connectLearnBot(2, AppConfig.serverPort);
        bot2.nConfig.randAllParams();
        int botNum = 3;
        int i = 0;
        while (i < iterations && running) {
            sleep(1000);
            int s1 = StatsTools.getBotScore(bot1.getBotName(), gameStats);
            int s2 = StatsTools.getBotScore(bot2.getBotName(), gameStats);
            if (s1 >= itScore) {

                EvalResults res = new EvalResults(gameStats,
                        bot1.getBotName(), bot2.getBotName(),
                        bot1.nConfig, bot2.nConfig);
                OptimizationRunner.getInstance().handleIteratinResults(res);

                bot2.disconnect();
                sleep(5000);
                gameStats = BotStatistic.createNewInstance();

                bot2 = OptimizationRunner.getInstance().connectLearnBot(botNum, AppConfig.serverPort);
                bot2.nConfig.randAllParams();

                bot1.resetFramesCount();

                botNum++;
                i++;

                Dbg.prn("-------------------> New iteration!");
            } else if (s2 >= itScore) {

                EvalResults res = new EvalResults(gameStats,
                        bot1.getBotName(), bot2.getBotName(),
                        bot1.nConfig, bot2.nConfig);
                OptimizationRunner.getInstance().handleIteratinResults(res);

                bot1.disconnect();
                sleep(5000);
                gameStats = BotStatistic.createNewInstance();

                bot1 = OptimizationRunner.getInstance().connectLearnBot(botNum, AppConfig.serverPort);
                bot1.nConfig.randAllParams();
                bot1.addBotJob(new GlobalKillsStatsJob(bot1));

                bot2.resetFramesCount();

                botNum++;
                i++;

                Dbg.prn("-------------------> New iteration!");
            }
            Dbg.prn("" + s1 + " : " + s2);
        }
        stopProcess();

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
    }
}
