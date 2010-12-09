/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.util.LinkedList;
import piotrrr.thesis.bots.learnbot.LearnBot;
import piotrrr.thesis.bots.referencebot.ReferenceBot;
import piotrrr.thesis.common.jobs.GlobalKillsStatsJob;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.common.stats.StatsTools;
import piotrrr.thesis.gui.AppConfig;
import piotrrr.thesis.gui.MyPopUpDialog;
import soc.qase.tools.vecmath.Vector3f;

/**
 *
 * @author piotrrr
 */
public class ConfigEvaluator implements Runnable {

    String mapName;
    LearnBot bot = null;
    ReferenceBot refBot = null;
    NavConfig nc1;
    int gameNr;
    static BotStatistic gameStats = null;
    boolean running = true;
    boolean unblock = false;
    int itTime;
    int result = Integer.MIN_VALUE;
    public static final int MAX_SAME_SCORE = 1000;
    public static final int ONE_SECOND = 1000;
    private Vector3f refBotLastPos = new Vector3f();
    private Vector3f botLastPos = new Vector3f();

    public ConfigEvaluator(String mapName, NavConfig nc1, int gameNr, int itTime) {
        this.mapName = mapName;
        this.nc1 = nc1;
        this.gameNr = gameNr;
        this.itTime = itTime;
    }

    public static int sequentialEvaluateConfig(NavConfig nc1, int bn1, int repetitions, int itScore, String mapName) {
        LinkedList<ConfigEvaluator> ces = new LinkedList<ConfigEvaluator>();

        ConfigEvaluator.gameStats = null;

        for (int i = 0; i < repetitions; i++) {
            ces.add(new ConfigEvaluator(mapName, nc1, i, itScore));
            ces.getLast().run();
        }

        int res = 0;
        for (ConfigEvaluator c : ces) {
            res += c.result;
        }
        res /= ces.size();

        return res;

    }

    public static int parallelEvaluateConfig(NavConfig nc1, int bn1, int repetitions, int itScore, String mapName) {
        LinkedList<ConfigEvaluator> ces = new LinkedList<ConfigEvaluator>();
        LinkedList<Thread> ts = new LinkedList<Thread>();

        ConfigEvaluator.gameStats = null;

        for (int i = 0; i < repetitions; i++) {
            ces.add(new ConfigEvaluator(mapName, nc1, i, itScore));
            Thread t = new Thread(ces.getLast());
            ts.add(t);
            t.setName("parallelEval-" + i);
            t.start();
        }

        boolean working = true;
        while (working) {
            int dead = 0;
            for (Thread t : ts) {
                if (!t.isAlive()) {
                    dead++;
                }
            }
            if (dead == ts.size()) {
                working = false;
            }
        }

        int res = 0;
        for (ConfigEvaluator c : ces) {
            res += c.result;
        }
        res /= ces.size();

        return res;

    }

    public void run() {
        int servPort = AppConfig.serverPort + gameNr;
        int sameScoreCount = 0;
        try {
            int ret = 0;
            boolean evalRuns = true;

            OptimizationRunner.getInstance().runLocalQ2Server(servPort, mapName);
            sleep(3 * ONE_SECOND);

            if (gameStats == null) {
                gameStats = BotStatistic.createNewInstance();
            }

            bot = OptimizationRunner.getInstance().connectLearnBot(gameNr, servPort);
            refBot = OptimizationRunner.getInstance().connectReferenceBot(gameNr, servPort);

            if (bot == null || refBot == null) {
                tryAgain(servPort, "Bots didnt connect to server");
                return;
            }
            bot.nConfig = nc1;
            bot.addBotJob(new GlobalKillsStatsJob(bot));


            int lastMaxScore = -1;
            int time = 0;
            while (true) {
                sleep(ONE_SECOND);
                time++;
                int score = StatsTools.getBotScore(bot.getBotName(), gameStats);
                int refScore = StatsTools.getBotScore(refBot.getBotName(), gameStats);
                if (time >= (itTime / AppConfig.timeScale)) {
                    ret = score - refScore;
                    stopAndNullBots();
                    OptimizationRunner.getInstance().clearProcess(servPort);
                    result = ret;
                    return;
                }
                if (unblock) {
                    unblock = false;
                    tryAgain(servPort, "Unblock was set to true.");
                    return;
                }

                if (areBotsStuck()) {
                    tryAgain(servPort, "Bots did not move");
                    return;
                }


                int maxScore = Math.max(score, refScore);
                if (maxScore != lastMaxScore) {
                    lastMaxScore = maxScore;
                    sameScoreCount = 0;
                } else {
                    sameScoreCount++;
                }

                if (sameScoreCount > (MAX_SAME_SCORE / AppConfig.timeScale)) {
                    tryAgain(servPort, "No kills in too long time");
                    return;
                }

            }
//            stopAndNullBots();
//            OptimizationRunner.getInstance().clearProcess(servPort);
//            result = ret;
//            return;
        } catch (Exception e) {
            tryAgain(servPort, "Exception in run(): "+e.toString());
            return;
        }
    }

    private boolean areBotsStuck() {
        try {
            if (refBot.getWorld() != null && refBotLastPos.equals(refBot.getBotPosition())) {
                return true;
            }
            if (bot.getWorld() != null && botLastPos.equals(bot.getBotPosition())) {
                return true;
            }
            refBotLastPos = refBot.getBotPosition();
            botLastPos = bot.getBotPosition();
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    private void sleep(int milis) {
        try {
            Thread.yield();
            Thread.sleep(milis);
            Thread.yield();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void stopAndNullBots() {
        if (bot != null) {
            bot.disconnect();
            bot.stopAndDestroy();
            bot = null;
        }
        if (refBot != null) {
            refBot.disconnect();
            refBot.stopAndDestroy();
            refBot = null;
        }
    }

    private void tryAgain(int servPort, String reason) {
        if (bot != null) {
            StatsTools.removeKillsOfBot(bot.getBotName(), BotStatistic.getInstance());
            bot.disconnect();
            bot.stopAndDestroy();
            bot = null;
        }
        if (refBot != null) {
            StatsTools.removeKillsOfBot(refBot.getBotName(), BotStatistic.getInstance());
            refBot.disconnect();
            refBot.stopAndDestroy();
            refBot = null;
        }
        //System.gc();
        MyPopUpDialog.showMyDialogBox("Eval unblocked", "Eval was automatically unblocked: " + reason, MyPopUpDialog.info);
        sleep(2 * ONE_SECOND);
        OptimizationRunner.getInstance().clearProcess(servPort);
        ConfigEvaluator ce = new ConfigEvaluator(mapName, nc1, gameNr, itTime);
        Thread t = new Thread(ce);
        t.start();
        while (t.isAlive()) {
            sleep(ONE_SECOND);
        }
        result = ce.result;
    }
}
