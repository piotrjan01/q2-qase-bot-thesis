/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.common.stats.StatsTools;
import piotrrr.thesis.tools.Timer;
import weka.core.RandomVariates;

/**
 *
 * @author piotrrr
 */
public class FakeConfigEvaluator implements Runnable {

    static BotStatistic gameStats = null;
    static Timer timer;
    String mapName;
    NavConfig nc1;
    int gameNr;
    int itTime;
    int result = Integer.MIN_VALUE;
    public static Random rand = new Random();
    public static String evalLog = "";
    NavConfig optimalCfg = new NavConfig();
    public static RandomVariates randVars = new RandomVariates();

    public FakeConfigEvaluator(String mapName, NavConfig nc1, int gameNr, int itTime) {
        try {
            this.mapName = mapName;
            this.nc1 = nc1;
            this.gameNr = gameNr;
            this.itTime = itTime;
            optimalCfg.weight_health.setValue(0.9);
            optimalCfg.weight_armor.setValue(0.1);
            optimalCfg.weight_weapon.setValue(0.16);
            optimalCfg.weight_ammo.setValue(0.77);
            optimalCfg.weight_health_ben.setValue(0.97);
            optimalCfg.weight_armor_ben.setValue(0.45);
            optimalCfg.weight_weapon_ben.setValue(0.0);
            optimalCfg.weight_ammo_ben.setValue(0.78);
            optimalCfg.weight_distance.setValue(0.16);
            optimalCfg.weight_enemycost.setValue(0.34);
            optimalCfg.weight_aggresiveness.setValue(0.51);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static double sequentialEvaluateConfig(NavConfig nc1, int repetitions, int itScore, String mapName) {
        LinkedList<FakeConfigEvaluator> ces = new LinkedList<FakeConfigEvaluator>();
        evalLog = "";
        FakeConfigEvaluator.gameStats = null;
        timer = new Timer("Sequential evaluation [reps=" + repetitions + " itTime=" + itScore + " map=" + mapName + "]");
        timer.startFromZero();

        for (int i = 0; i < repetitions; i++) {
            ces.add(new FakeConfigEvaluator(mapName, nc1, i, itScore));
            ces.getLast().run();
        }

        double res = 0;
        for (FakeConfigEvaluator c : ces) {
            res += c.result;
        }
        res /= ces.size();

        timer.pause();
        evalLog += "\n" + timer.toString();
        evalLog += "\nConfig is better probability: " + ces.getFirst().getConfigIsBetterProbability();
        return res;

    }

    public void run() {

        int ret = 0;

        String fakeLearnBot = "LearnBot-" + gameNr;
        String fakeRefBot = "ReferenceBot-" + gameNr;

        double p = getConfigIsBetterProbability(); //probability of winning

        if (gameStats == null) {
            gameStats = BotStatistic.createNewInstance();
        }

        int time = 0;
        while (true) {
            time++;

            if (getRandomBool(0.997)) {
                continue;
            }

            if (getRandomBool(p, 0.5, 0.2)) {
                gameStats.addKill(time, fakeLearnBot, fakeRefBot, "fake-gun");
            } else {
                gameStats.addKill(time, fakeRefBot, fakeLearnBot, "fake-gun");
            }

            if (time >= 10 * itTime) {
                int refScore = StatsTools.getBotScore(fakeRefBot, gameStats);
                ret = StatsTools.getBotScore(fakeLearnBot, gameStats) - refScore;
                result = ret;
                evalLog += ret + " ";
                return;
            }

        }

    }

    private double getConfigIsBetterProbability() {
        double dist = getConfigDistanceFactor();
        return (1 - dist)*(1 - dist);
    }

    private boolean getRandomBool(double trueProbability) {
        return rand.nextDouble() < trueProbability;
    }

    private boolean getRandomBool(double trueProbability, double mean, double variance) {
        return getRandomNumber(mean, variance) < trueProbability;
    }

    private double getRandomNumber(double mean, double variance) {
        return mean + rand.nextGaussian()*variance;
    }

    private double getConfigDistanceFactor() {
        double dist = optimalCfg.getConfigFakingDistance(nc1);
        int count = optimalCfg.getParamsCount();
        return (dist / count);
    }
}
