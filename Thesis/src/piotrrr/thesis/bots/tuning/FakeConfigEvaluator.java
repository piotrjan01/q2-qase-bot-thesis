/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Random;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.common.stats.StatsTools;
import piotrrr.thesis.tools.Dbg;
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

    double minBetterP = 0.1;

    double maxBetterP = 0.7;

    private double [] configSensitivities = { 0.42, 0.61, 0.93, 0.63, 0.36, 0.40, 0.40, 0.52, 0.35, 0.40, 0.98 };

    public FakeConfigEvaluator(String mapName, NavConfig nc1, int gameNr, int itTime) {
        try {
            this.mapName = mapName;
            this.testedConfig = nc1;
            this.gameNr = gameNr;
            this.itTime = itTime;
            
            optCfg1.weight_health.setValue(0.12);
            optCfg1.weight_armor.setValue(0.33);
            optCfg1.weight_weapon.setValue(0.87);
            optCfg1.weight_ammo.setValue(0.65);
            optCfg1.weight_health_ben.setValue(0.67);
            optCfg1.weight_armor_ben.setValue(0.65);
            optCfg1.weight_weapon_ben.setValue(0.0);
            optCfg1.weight_ammo_ben.setValue(0.08);
            optCfg1.weight_distance.setValue(0.32);
            optCfg1.weight_enemycost.setValue(0.84);
            optCfg1.weight_aggresiveness.setValue(0.21);

            optCfg2.weight_health.setValue(0.3);
            optCfg2.weight_armor.setValue(0.64);
            optCfg2.weight_weapon.setValue(0.2656);
            optCfg2.weight_ammo.setValue(0.1947);
            optCfg2.weight_health_ben.setValue(0.4547);
            optCfg2.weight_armor_ben.setValue(0.6365);
            optCfg2.weight_weapon_ben.setValue(0.0540);
            optCfg2.weight_ammo_ben.setValue(0.3948);
            optCfg2.weight_distance.setValue(0.4116);
            optCfg2.weight_enemycost.setValue(0.6654);
            optCfg2.weight_aggresiveness.setValue(0.0);

            optCfg3.weight_health.setValue(0.039);
            optCfg3.weight_armor.setValue(0.821);
            optCfg3.weight_weapon.setValue(0.436);
            optCfg3.weight_ammo.setValue(0.9477);
            optCfg3.weight_health_ben.setValue(0.0197);
            optCfg3.weight_armor_ben.setValue(0.445);
            optCfg3.weight_weapon_ben.setValue(0.460);
            optCfg3.weight_ammo_ben.setValue(0.0178);
            optCfg3.weight_distance.setValue(0.4116);
            optCfg3.weight_enemycost.setValue(0.4134);
            optCfg3.weight_aggresiveness.setValue(0.4241);

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
            time+=rand.nextInt(720); //mena is 360, that is 22 kills in 800 sec

            if (getRandomBool(minBetterP+(maxBetterP-minBetterP)*p, 0.1)) {
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

        return getConfigIsBetterProbability(testedConfig);


        
    }

    private double getConfigIsBetterProbability(NavConfig opt, NavConfig tested) {
        double p = 0;
        double maxp = 0;
        for (double s : configSensitivities) {
            maxp += s;
        }
        int i=0;
        for (Field f : tested.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                OptParam p1 = (OptParam) f.get(opt);
                OptParam p2 = (OptParam) f.get(tested);
                double diff = Math.abs(p1.getValue() - p2.getValue());
                p += diff*configSensitivities[i];
                i++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 1-p/maxp;
    }

    private double getConfigIsBetterProbability(NavConfig tested) {
        double p = 0;
        double maxp = 0;
        double sensitivityConst = 1.05;
        for (double s : configSensitivities) {
            maxp += 1/(sensitivityConst-s);
        }
        int i=0;
        for (Field f : tested.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                OptParam test = (OptParam) f.get(tested);
                OptParam p1 = (OptParam) f.get(optCfg1);
                OptParam p2 = (OptParam) f.get(optCfg2);
                OptParam p3 = (OptParam) f.get(optCfg3);
                double diff = 0.4*(p1.getValue() - test.getValue())+0.4*(p2.getValue() - test.getValue())+0.2*(p3.getValue() - test.getValue());
//                double diff = (p1.getValue() - test.getValue());
                diff = Math.sqrt(Math.abs(diff));
                p += diff*(1/(sensitivityConst-configSensitivities[i]));
                i++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 1-p/maxp;
    }

    private boolean getRandomBool(double trueProbability) {
        return rand.nextDouble() < trueProbability;
    }

    private boolean getRandomBool(double trueProbability, double variance) {
        return getRandomNumber(0.5, variance) < trueProbability;
    }

    private double getRandomNumber(double mean, double variance) {
        return mean + rand.nextGaussian()*variance;
    }
   
}
