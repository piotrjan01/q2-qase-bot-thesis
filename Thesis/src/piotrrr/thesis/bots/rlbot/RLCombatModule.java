package piotrrr.thesis.bots.rlbot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import piotrrr.thesis.common.combat.*;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.tools.Dbg;
import soc.qase.tools.vecmath.Vector3f;
import weka.classifiers.meta.AdditiveRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * TODO:
 * - tylko jeden sposób strzelania? - przewidywanie z tw. sinusów.
 */
/**
 * The aiming module
 * @author Piotr Gwizdała
 */
public class RLCombatModule {

    RLBot bot;
    AdditiveRegression classifier = null;
    Instances instances = null;

    public RLCombatModule(RLBot bot) {
        this.bot = bot;
        classifier = (AdditiveRegression) readFromFile("additive-regression-weka-model.model");
        try {
            instances = DataSource.read("instances-t1.csv");
        } catch (Exception ex) {
            Logger.getLogger(RLCombatModule.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private double predictX(String wpn, double d0, double d1, Vector3f bv) throws Exception {
        //wpn,d0,d1,bx,by,bz,hx,hy,hz
        double[] vals = new double[7];
        vals[0] = instances.attribute(0).indexOfValue(wpn);
        if (vals[0] == -1) return Double.NaN;
        vals[1] = d0;
        vals[2] = d1;
        vals[3] = bv.x;
        vals[4] = bv.y;
        vals[5] = bv.z;

        Instance ni = new Instance(1, vals);
        instances.add(ni);
        try {
            double pred = classifier.classifyInstance(instances.lastInstance());
            return pred;
        } catch (Exception ex) {
            for (int i=0; i<vals.length; i++) {
                System.out.println("vals["+i+"] = "+vals[i]);
            }
            System.out.println("wpn = "+wpn);
            ex.printStackTrace();
            return Double.NaN;
        }
    }

    public FiringDecision getFiringDecision() {
        Vector3f playerPos = bot.getBotPosition();
        EnemyInfo chosen = null;
        float chosenRisk = Float.MAX_VALUE;
        float maxDist = -1;
        float maxError = 0;
        LinkedList<EnemyInfo> eligible = new LinkedList<EnemyInfo>();
        for (EnemyInfo ei : bot.kb.enemyInformation.values()) {

            if (ei.getBestVisibleEnemyPart(bot) == null) {
                continue;
            }

            if (ei.lastUpdateFrame + bot.cConfig.maxEnemyInfoAge4Firing < bot.getFrameNumber()) {
                continue;
            }

            float dist = CommFun.getDistanceBetweenPositions(playerPos, ei.getObjectPosition());
            float error = ei.lastPredictionError;

            if (dist > maxDist) {
                maxDist = dist;
            }
            if (error > maxError) {
                maxError = error;
            }

            eligible.add(ei);
        }

        for (EnemyInfo ei : eligible) {
            float dist = CommFun.getDistanceBetweenPositions(playerPos, ei.getObjectPosition());
            float error = ei.lastPredictionError;
            float risk = dist / maxDist + error / maxError;
            if (chosenRisk > risk) {
                chosenRisk = risk;
                chosen = ei;
            }
        }

        if (chosen == null) {
            return null;
        }
        float dist = CommFun.getDistanceBetweenPositions(playerPos, chosen.getObjectPosition());
        int wpn = SimpleCombatModule.chooseWeapon(bot, dist);
        return new FiringDecision(chosen, wpn);
    }

    /**
     * Returns the firing instructions
     * @param fd firing decision
     * @param bot the bot
     * @return
     */
    public FiringInstructions getFiringInstructions(FiringDecision fd) {
        if (fd == null || fd.enemyInfo == null) {
            return null;
        }

        boolean reloading = bot.getWorld().getPlayer().getPlayerGun().isCoolingDown();
        Vector3f noFiringLook = fd.enemyInfo.predictedPos == null ? fd.enemyInfo.getObjectPosition() : fd.enemyInfo.predictedPos;
        if (reloading) {
            return SimpleAimingModule.getNoFiringInstructions(bot, noFiringLook);
        }

        if (fd.enemyInfo.lastUpdateFrame + bot.cConfig.maxEnemyInfoAge4Firing < bot.getFrameNumber()) {
            return SimpleAimingModule.getNoFiringInstructions(bot, noFiringLook);
        }

        FiringInstructions fi = SimpleAimingModule.getNewPredictingFiringInstructions(bot, fd, bot.cConfig.getBulletSpeedForGivenGun(fd.gunIndex));
        double d0 = CommFun.getDistanceBetweenPositions(bot.getBotPosition(), fd.enemyInfo.getObjectPosition());
        double d1 = CommFun.getDistanceBetweenPositions(bot.getBotPosition(), fd.enemyInfo.predictedPos);
        Vector3f bv = CommFun.getNormalizedDirectionVector(bot.getBotPosition(), fd.enemyInfo.getObjectPosition());
        try {
            double pred = predictX(CommFun.getGunName(fd.gunIndex), d0, d1, bv);
            if (pred != Double.NaN) {
                Dbg.prn("x error = "+(pred - fi.fireDir.x));
                fi.fireDir.x = (float) pred;
            }
        } catch (Exception ex) {
            Logger.getLogger(RLCombatModule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fi;
    }

    public static void saveToFile(String filename, Object o) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(o);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Object readFromFile(String fileName) {
        Object r = null;
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            r = ois.readObject();
        } catch (Exception e) {
            System.err.println("Error reading object: " + fileName);
        }
        return r;
    }
}
