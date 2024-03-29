/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.common.stats.StatsChartsFactory;
import piotrrr.thesis.common.stats.StatsTools;
import piotrrr.thesis.tools.Timer;
import weka.core.RandomVariates;

/**
 *
 * @author piotrrr
 */
public class FakeConfigEvaluator implements Runnable {

    private static final boolean noisy = true;
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
    double minBetterP = 0.0;
    double maxBetterP = 1.0;
    private double[] configSensitivities = {0.42, 0.61, 0.93, 0.63, 0.36, 0.40, 0.40, 0.52, 0.35, 0.40, 0.98};
    private int[] dependingParam = {4, 5, 6, 7, 5, 4, 7, 6, 8, 10, 10};
//    private static int[] nrOfExtremums = {10, 9, 10, 15, 7, 9, 15, 15, 13, 10, 7}; //7 to 15
    private static int[] nrOfExtremums = {7, 2, 7, 2, 2, 2, 5, 2, 4, 7, 1}; //2 to 15
//    private static int[] nrOfExtremums = {15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15}; //7 to 15

    public FakeConfigEvaluator(String mapName, NavConfig nc1, int gameNr, int itTime) {
        try {
            this.mapName = mapName;
            this.testedConfig = nc1;
            this.gameNr = gameNr;
            this.itTime = itTime;

            //Taken from opt. Do not change!
            optCfg1.weight_health.setValue(0.20);                   //0
            optCfg1.weight_armor.setValue(0.68);                    //1
            optCfg1.weight_weapon.setValue(0.37);                   //2
            optCfg1.weight_ammo.setValue(0.205);                     //3
            optCfg1.weight_health_ben.setValue(0.27);               //4
            optCfg1.weight_armor_ben.setValue(0.65);                //5
            optCfg1.weight_weapon_ben.setValue(0.031);                //6
            optCfg1.weight_ammo_ben.setValue(0.31);                 //7
            optCfg1.weight_distance.setValue(0.422);                 //8
            optCfg1.weight_enemycost.setValue(0.264);                //9
            optCfg1.weight_aggresiveness.setValue(0.061);            //10

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
//        evalLog += "\n" + timer.toString();
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

        int killsNr = 28;

        double killsL = ((minBetterP + (maxBetterP - minBetterP) * p) * killsNr);
        double killsR = ((1.0 - (minBetterP + (maxBetterP - minBetterP) * p)) * killsNr);

        if (noisy == false) {
            if (getRandomBool(p)) {
                killsL += 1;
            }
            if (getRandomBool(p)) {
                killsL -= 1;
            }
            if (getRandomBool(p)) {
                killsR += 1;
            }
            if (getRandomBool(p)) {
                killsR -= 1;
            }
        } else {
            double var = 4.2;
            killsL = getRandomNumber(killsL, var);
            killsR = getRandomNumber(killsR, var);
        }

        int time = 0;

        boolean lastR = false;

        while (true) {

            time += rand.nextInt(800/killsNr*10*2); //mean is 360, that is 22 kills in 800 sec

            int rScore = StatsTools.getBotScore(fakeRefBot, gameStats);
            int lScore = StatsTools.getBotScore(fakeLearnBot, gameStats);

            if (noisy == false) {
                if (lastR) {
                    lastR = false;
                    gameStats.addKill(time, fakeLearnBot, fakeRefBot, "weapon");
                } else {
                    lastR = true;
                    if (rScore < killsR) {
                        gameStats.addKill(time, fakeRefBot, fakeLearnBot, "weapon");
                    }
                }

                if (lScore >= killsL) {
                    ret = lScore - rScore;
                    result = ret;
                    evalLog += ret + " ";
                    return;
                }
            } else {

                if (getRandomBool(minBetterP + (maxBetterP - minBetterP) * p)) {
                    if (time > 8*itTime || lScore < killsL) {
                        gameStats.addKill(time, fakeLearnBot, fakeRefBot, "weapon");
                    }
                } //            if (getRandomBool(minBetterP + (maxBetterP - minBetterP) * (1-p))) {
                else {
                    if (time > 8*itTime || rScore < killsR) {
                        gameStats.addKill(time, fakeRefBot, fakeLearnBot, "weapon");
                    }
                }

                if (time >= 10 * itTime) {
//                    gameStats.addKill(time, fakeLearnBot, fakeRefBot, "fake-gun");
//                    gameStats.addKill(time, fakeRefBot, fakeLearnBot, "fake-gun");

                    int refScore = rScore;
                    ret = lScore - refScore;
                    result = ret;
                    evalLog += ret + " ";
                    return;
                }
            }

        }

    }

    private double getConfigIsBetterProbability() {

        return getNewConfigIsBetterProbability(testedConfig);



    }

    /**
     * @see http://fooplot.com/index.php?&type0=0&type1=0&type2=0&type3=0&type4=0&y0=cos%28abs%282x%29%5E0.4%29*cos%2810*x%29&y1=&y2=&y3=&y4=&r0=&r1=&r2=&r3=&r4=&px0=&px1=&px2=&px3=&px4=&py0=&py1=&py2=&py3=&py4=&smin0=0&smin1=0&smin2=0&smin3=0&smin4=0&smax0=2pi&smax1=2pi&smax2=2pi&smax3=2pi&smax4=2pi&thetamin0=0&thetamin1=0&thetamin2=0&thetamin3=0&thetamin4=0&thetamax0=2pi&thetamax1=2pi&thetamax2=2pi&thetamax3=2pi&thetamax4=2pi&ipw=0&ixmin=-5&ixmax=5&iymin=-3&iymax=3&igx=1&igy=0.1&igl=1&igs=0&iax=1&ila=1&xmin=-1.8225399691290463&xmax=1.4861241613056888&ymin=-0.2651052623806613&ymax=1.2253986099114524
     * @param tested
     * @return
     */
    private double getNewConfigIsBetterProbability(NavConfig tested) {
        double p = 0;
        double maxp = 0;
        double sensitivityConst = 1.1;
        for (double s : configSensitivities) {
            maxp += 1 / (sensitivityConst - s);
        }
        int i = 0;
        for (Field f : tested.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                OptParam test = (OptParam) f.get(tested);
                OptParam p1 = (OptParam) f.get(optCfg1);
                double diff = getValueDistance(test.getValue(), p1.getValue(), i);
                p += diff * (1 / (sensitivityConst - configSensitivities[i]));
                i++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 1 - p / maxp;
    }

    private static double getValueDistance(double x, double x0, int paramNr) {
        double handicap = 0.1;
        double spread = 1 - handicap;
        double bestToMedPow = 0.2;
        double fbig = 2;
        double fmed = nrOfExtremums[paramNr]; //from 7 to 15
        double fsmall = 100;
        double smallNoisePow = 0.05;
        double res = handicap + spread - spread * Math.cos(Math.pow(Math.abs(fbig * (x - x0)), bestToMedPow)) * Math.pow(Math.cos(fmed * (x - x0)), 2);
        res += smallNoisePow * Math.cos(fsmall * x);
        if (res > 1) {
            res = 1;
        }
        if (res < 0) {
            res = 0;
        }
        return res;
    }

    private boolean getRandomBool(double trueProbability) {
        return rand.nextDouble() < trueProbability;
    }

    private boolean getRandomBool(double trueProbability, double variance) {
        return getRandomNumber(0.5, variance) < trueProbability;
    }

    private double getRandomNumber(double mean, double variance) {
        return mean + rand.nextGaussian() * variance;
    }

    public static void main(String[] args) {
        LinkedList<String> seriesNames = new LinkedList<String>();
        HashMap<String, LinkedList<double[]>> data = new HashMap<String, LinkedList<double[]>>();

        double xmin = 0;
        double xmax = 1;

        for (double x0 = 0.2; x0 < 1; x0 += 2) {

            LinkedList<double[]> line1 = new LinkedList<double[]>();

            for (double x = xmin; x < xmax; x += 0.001) {
                line1.add(new double[]{x, getValueDistance(x, x0, 10)});
            }

            data.put("x0 = " + x0, line1);
            seriesNames.add("x0 = " + x0);

        }

        ChartPanel chart = StatsChartsFactory.getXYChart(seriesNames, data, "Fake config test", "x", "dist", true);
        ChartFrame frame = new ChartFrame("Fake config test", chart.getChart());
        frame.pack();
        frame.setVisible(true);
    }
}
