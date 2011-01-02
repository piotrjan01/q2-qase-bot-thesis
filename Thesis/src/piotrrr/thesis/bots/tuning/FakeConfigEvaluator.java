/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.lang.reflect.Field;
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
    NavConfig testedConfig;
    int gameNr;
    int itTime;
    int result = Integer.MIN_VALUE;
    public static Random rand = new Random();
    public static String evalLog = "";
    NavConfig optCfg1 = new NavConfig();
    NavConfig optCfg2 = new NavConfig();
    NavConfig optCfg3 = new NavConfig();
    public static RandomVariates randVars = new RandomVariates();

    private double [] configSensitivities = { 0.42, 0.61, 0.93, 0.63, 0.36, 0.40, 0.40, 0.52, 0.35, 0.40, 0.98 };

    public FakeConfigEvaluator(String mapName, NavConfig nc1, int gameNr, int itTime) {
        try {
            this.mapName = mapName;
            this.testedConfig = nc1;
            this.gameNr = gameNr;
            this.itTime = itTime;
            
            optCfg1.weight_health.setValue(0.9);
            optCfg1.weight_armor.setValue(0.1);
            optCfg1.weight_weapon.setValue(0.16);
            optCfg1.weight_ammo.setValue(0.77);
            optCfg1.weight_health_ben.setValue(0.97);
            optCfg1.weight_armor_ben.setValue(0.45);
            optCfg1.weight_weapon_ben.setValue(0.0);
            optCfg1.weight_ammo_ben.setValue(0.78);
            optCfg1.weight_distance.setValue(0.16);
            optCfg1.weight_enemycost.setValue(0.34);
            optCfg1.weight_aggresiveness.setValue(0.51);

            optCfg2.weight_health.setValue(0.64);
            optCfg2.weight_armor.setValue(0.24);
            optCfg2.weight_weapon.setValue(0.56);
            optCfg2.weight_ammo.setValue(0.47);
            optCfg2.weight_health_ben.setValue(0.47);
            optCfg2.weight_armor_ben.setValue(0.65);
            optCfg2.weight_weapon_ben.setValue(0.40);
            optCfg2.weight_ammo_ben.setValue(0.48);
            optCfg2.weight_distance.setValue(0.16);
            optCfg2.weight_enemycost.setValue(0.54);
            optCfg2.weight_aggresiveness.setValue(0);

            optCfg3.weight_health.setValue(0.39);
            optCfg3.weight_armor.setValue(0.21);
            optCfg3.weight_weapon.setValue(0.36);
            optCfg3.weight_ammo.setValue(0.477);
            optCfg3.weight_health_ben.setValue(0.197);
            optCfg3.weight_armor_ben.setValue(0.445);
            optCfg3.weight_weapon_ben.setValue(0.60);
            optCfg3.weight_ammo_ben.setValue(0.178);
            optCfg3.weight_distance.setValue(0.116);
            optCfg3.weight_enemycost.setValue(0.134);
            optCfg3.weight_aggresiveness.setValue(0.241);

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

            if (getRandomBool(0.25+0.5*p, 0.5, 0.3)) {
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

        double c1 =getConfigIsBetterProbability(optCfg1, testedConfig);
        double c2 =getConfigIsBetterProbability(optCfg2, testedConfig);
        double c3 = getConfigIsBetterProbability(optCfg3, testedConfig);

        if (c1>c2 && c1 > c3) {
            return c1;
        }
        else if (c2>c1 && c2>c3) {
            return c2*0.8;
        }
        else {
            return c3*0.6;
        }
        
    }

    private double getConfigIsBetterProbability(NavConfig opt, NavConfig tested) {
        double p = 0;
        double maxp = 0;
        for (double s : configSensitivities) {
            maxp += s;
        }
        int i=0;
        for (Field f : testedConfig.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                OptParam p1 = (OptParam) f.get(opt);
                OptParam p2 = (OptParam) f.get(tested);
                double diff = Math.abs(p1.getValue() - p2.getValue());
                p += diff*diff*Math.sqrt(Math.sqrt(configSensitivities[i]));
                i++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return p/maxp;
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
   
}
