package piotrrr.thesis.bots.rlbot;

import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import piotrrr.thesis.bots.rlbot.rl.Action;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.combat.*;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.jobs.HitsReporter;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.tools.Dbg;
import pl.gdan.elsy.qconf.Brain;
import pl.gdan.elsy.qconf.Perception;
import soc.qase.tools.vecmath.Vector3f;

/**
 * TODO:
 * - tylko jeden sposób strzelania? - przewidywanie z tw. sinusów.
 */
/**
 * The aiming module
 * @author Piotr Gwizdała
 */
public class RLCombatModule extends Perception {

    RLBot bot;
    double actionReward = 0;
    int actionTime = 20;
    int actionEndFrame = -1;
    int actionFireMode = Action.NO_FIRE;
    public TreeMap<Integer, WeaponScore> weaponRanking = new TreeMap<Integer, WeaponScore>();

    public static class WeaponScore implements Comparable<WeaponScore> {

        private double rewardsTotal;
        private int rewardsCount;

        public WeaponScore(double rewardsTotal, int rewardsCount) {
            this.rewardsTotal = rewardsTotal;
            this.rewardsCount = rewardsCount;
        }

        double getScore() {
            return rewardsTotal / rewardsCount;
        }

        public int compareTo(WeaponScore o) {
            double t = getScore();
            double a = o.getScore();
            if (t > a) {
                return -1;
            }
            if (t < a) {
                return 1;
            }
            return 0;
        }
    }

    class Shooting {

        long shotTime = 0;
        long hitTime = 0;
        String enemyName = null;
        double reward = 0;

        public Shooting(long shotTime, long hitTime, String enemyName) {
            this.shotTime = shotTime;
            this.hitTime = hitTime;
            this.enemyName = enemyName;
        }
    }
    LinkedList<Shooting> shootings = new LinkedList<Shooting>();
    Action[] actions = Action.getAllActionsArray();
    public static final int maxShootingsCount = 100;
    public Brain brain;
    double actionDistance = 0;
    boolean unipolar = true;

    public RLCombatModule(RLBot bot) {
        this.bot = bot;

        int hiddenLayerNeurons = 5;
        int [] hiddenLayers = new int [actions.length];
        for (int i=0; i<hiddenLayers.length; i++) hiddenLayers[i] = hiddenLayerNeurons;
        brain = new Brain(this, actions, hiddenLayers);

        brain.setAlpha(0.8); //learning rate
        brain.setGamma(0.3); //discounting rate
        brain.setLambda(0.6); //trace forgetting
//        b.setUseBoltzmann(true);
//        b.setTemperature(0.001);
        brain.setRandActions(0.1); //exploration

//        brain.setAlpha(getRandParam(0.1, 0.8, 0.1)); //learning rate
//        brain.setGamma(getRandParam(0, 0.9, 0.2)); //discounting rate
//        brain.setLambda(getRandParam(0, 0.9, 0.2)); //trace forgetting
////        b.setUseBoltzmann(true);
////        b.setTemperature(0.001);
//        brain.setRandActions(getRandParam(0.01, 0.4, 0.1)); //exploration
//        unipolar = Rand.b();

        for (int i = 7; i < 18; i++) {
            weaponRanking.put(i, new WeaponScore(0.5, 1));
        }

    }

    double getRandParam(double from, double to, double quant) {
        int count = (int) ((to - from) / quant) + 1;
        Random r = new Random();
        return from + r.nextInt(count) * quant;
    }

    @Override
    public String toString() {
        String s = "alpha=" + brain.getAlpha() + " gamma=" + brain.getGamma() +
                "\nlambda=" + brain.getLambda() + " rand=" + brain.getRandActions() +
                "\nunip=" + isUnipolar() + "\n";

        TreeMap<Integer, WeaponScore> newRanking = new TreeMap<Integer, WeaponScore>();
        for (Map.Entry<Integer, WeaponScore> e : weaponRanking.entrySet()) {
            newRanking.put(e.getKey(), e.getValue());
        }
        weaponRanking = newRanking;

        for (Map.Entry<Integer, WeaponScore> e : weaponRanking.entrySet()) {
            s += "\n" + CommFun.getGunName(e.getKey()) + " : " + e.getValue().getScore();
        }
        return s;
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
        actionDistance = CommFun.getDistanceBetweenPositions(playerPos, chosen.getObjectPosition());
        return new FiringDecision(chosen, -1);
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

        updateRewards();

        boolean reloading = bot.getWorld().getPlayer().getPlayerGun().isCoolingDown();
        Vector3f noFiringLook = fd.enemyInfo.predictedPos == null ? fd.enemyInfo.getObjectPosition() : fd.enemyInfo.predictedPos;
        if (reloading) {
            return getNoFiringInstructions(noFiringLook);
        }

        shootings.add(new Shooting(
                bot.getFrameNumber(),
                bot.getFrameNumber() + (long) getTimeToHit(
                bot.cConfig.getBulletSpeedForGivenGun(bot.getCurrentWeaponIndex()),
                bot.getBotPosition(),
                fd.enemyInfo.getObjectPosition(),
                fd.enemyInfo.predictedPos),
                fd.enemyInfo.ent.getName()));

        if (shootings.size() > maxShootingsCount) {
            shootings.removeFirst();
        }

        if (actionEndFrame == -1) {
            //first time
        }
        if (bot.getFrameNumber() >= actionEndFrame) {
            actionEndFrame = bot.getFrameNumber() + actionTime;
            //count rewards:
            bot.rewardsCount++;
            if (actionReward <= 1 && actionReward >= -1) {
                bot.totalReward += actionReward;
            } else {
                bot.totalReward += (actionReward > 0) ? 1 : -1;
                System.out.println("excessive reward! " + actionReward);
            }

            int rc = 1;
            double rwd = actionReward;
            int cwpi = bot.getCurrentWeaponIndex();
            if (weaponRanking.get(cwpi) != null) {
                rc = weaponRanking.get(cwpi).rewardsCount + 1;
                rwd = weaponRanking.get(cwpi).rewardsTotal + actionReward;
            }
            weaponRanking.put(cwpi, new WeaponScore(rwd, rc));

            BotStatistic.getInstance().addReward(bot.getBotName(), actionReward, bot.getFrameNumber());
//            Dbg.prn(bot.getBotName()+"@"+bot.getFrameNumber()+" r="+actionReward);
            //choose new action and execute it
            perceive();
            brain.count();
            executeAction(actions[brain.getAction()]);
            actionReward = 0;
        }

//        return getFiringInstructionsAtHitpoint(fd, 1);

//        return getNewPredictingFiringInstructions(bot, fd, bot.cConfig.getBulletSpeedForGivenGun(bot.getCurrentWeaponIndex()));

        switch (actionFireMode) {
            case Action.FIRE:
                return getFastFiringInstructions(fd, bot);
            case Action.FIRE_PREDICTED:
                return getNewPredictingFiringInstructions(bot, fd, bot.cConfig.getBulletSpeedForGivenGun(bot.getCurrentWeaponIndex()));
            default:
            case Action.NO_FIRE:
                return getNoFiringInstructions(noFiringLook);
        }

    }

    FiringInstructions getNoFiringInstructions(Vector3f enemyPos) {
        FiringInstructions ret = new FiringInstructions(CommFun.getNormalizedDirectionVector(bot.getBotPosition(), enemyPos));
        ret.doFire = false;
        return ret;
    }

    public static FiringInstructions getNewPredictingFiringInstructions(MapBotBase bot,
            FiringDecision fd, float bulletSpeed) {
        Vector3f playerPos = bot.getBotPosition();
        Vector3f enemyPos = fd.enemyInfo.getBestVisibleEnemyPart(bot);

        //Calculate the time to hit
        double timeToHit = getTimeToHit(bulletSpeed, playerPos, enemyPos, fd.enemyInfo.predictedPos);
        if (timeToHit <= 1.5) {
            timeToHit = 1f;
        }

        //We add to enemy position the movement that the enemy is predicted to do in timeToHit.
        Vector3f hitPoint = CommFun.cloneVector(enemyPos);
        //movement is between bot position, not the visible part of the bot
        Vector3f movement = CommFun.getMovementBetweenVectors(fd.enemyInfo.getObjectPosition(), fd.enemyInfo.predictedPos);
        movement = CommFun.multiplyVectorByScalar(movement, (float) timeToHit);
        hitPoint.add(movement);

        return new FiringInstructions(CommFun.getNormalizedDirectionVector(playerPos, hitPoint));
    }

    /**
     * * Point the enemy and shoot.
     * @param fd
     * @param playerPos
     * @return
     */
    static public FiringInstructions getFastFiringInstructions(FiringDecision fd, MapBotBase bot) {
        Vector3f to = new Vector3f(fd.enemyInfo.getBestVisibleEnemyPart(bot));
        Vector3f fireDir = CommFun.getNormalizedDirectionVector(bot.getBotPosition(), to);
//		bot.dtalk.addToLog("Fast firing.");
        return new FiringInstructions(fireDir);
    }

    public static double getTimeToHit(double bulletSpeed, Vector3f playerPos, Vector3f enemyPos, Vector3f enemyPredictedPos) {

        double d = CommFun.getDistanceBetweenPositions(playerPos, enemyPos);
        double v = bulletSpeed;
        double u = CommFun.getDistanceBetweenPositions(enemyPos, enemyPredictedPos);

        Vector3f vec1 = CommFun.getNormalizedDirectionVector(playerPos, enemyPos);
        Vector3f vec2 = CommFun.getNormalizedDirectionVector(enemyPos, enemyPredictedPos);

        double alpha = Math.toRadians(vec1.angle(vec2));

        double delta = 4 * d * d * u * u * Math.cos(alpha) * Math.cos(alpha) + 4 * (v * v - u * u) * d * d;

        double t = (2 * d * u * Math.cos(alpha) + Math.sqrt(delta)) / (2 * (v * v - u * u));

        double t2 = (2 * d * u * Math.cos(alpha) - Math.sqrt(delta)) / (2 * (v * v - u * u));

        //                System.out.println("t="+t+" v="+v+" d="+d+" u="+u+" t2="+t2);

        if (Double.isNaN(t)) {
            t = 1;
        }

        return t;

    }

    private void executeAction(Action action) {
        actionFireMode = action.getShootingMode();

        if (Action.isProhibited(action)) {
            return;
        }

        int wpind = action.actionToInventoryIndex();
        if (bot.getCurrentWeaponIndex() != wpind) {
            bot.changeWeaponToIndex(wpind);
//            if (bot.botHasItem(wpind)) System.out.println("chng wpn to: "+CommFun.getGunName(wpind));
        }
    }

    public void updateRewards() {
        if (bot.scoreCounter.getBotScore() > bot.lastBotScore) {
            bot.lastBotScore = bot.scoreCounter.getBotScore();
            //TODO: moze dac to do innego shootingu? Np. tego ktory ma hitpoint na teraz
            actionReward += 1;
        }
//            Dbg.prn("");
        LinkedList<Shooting> toDelete = new LinkedList<Shooting>();
        for (Shooting s : shootings) {

            int damage = HitsReporter.wasHitInGivenPeriod(s.shotTime + 1, s.hitTime + 2, s.enemyName);
            if (damage > 0) {
                actionReward += (damage / 100d) * 0.2;
                toDelete.add(s);
//                System.out.println(bot.getBotName()+" got shooting rwd - "+actionReward);
            } else if (s.hitTime + 4 < bot.getFrameNumber()) {
                toDelete.add(s);
//                System.out.println(bot.getBotName()+" missed! - "+actionReward);
//                actionReward -= 0.001;
            }
        }
        shootings.removeAll(toDelete);
        return;
    }

    @Override
    public boolean isUnipolar() {
        return unipolar;
    }

    @Override
    public double getReward() {
        return actionReward;
    }

    @Override
    protected void updateInputValues() {
        setNextValue(actionDistance);
        for (int i = 7; i < 18; i++) {
            setNextValue(bot.botHasItem(i));
        }
    }
}
