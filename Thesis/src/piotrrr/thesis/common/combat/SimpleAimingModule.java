package piotrrr.thesis.common.combat;

import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.CommFun;
import soc.qase.tools.vecmath.Vector3f;

/**
 * The aiming module
 * @author Piotr Gwizdała
 */
public class SimpleAimingModule {

    /**
     * Returns the firing instructions
     * @param fd firing decision
     * @param bot the bot
     * @return
     */
    public static FiringInstructions getFiringInstructions(FiringDecision fd, MapBotBase bot) {
        if (fd == null || fd.enemyInfo == null) {
            return null;
        }

        boolean reloading = bot.getWorld().getPlayer().getPlayerGun().isCoolingDown();
        Vector3f noFiringLook = fd.enemyInfo.predictedPos == null ? 
            fd.enemyInfo.getObjectPosition() : getHitPoint(bot, fd, bot.cConfig.getBulletSpeedForGivenGun(fd.gunIndex));
        if (reloading) {
            return getNoFiringInstructions(bot, noFiringLook);
        }

        if (fd.enemyInfo.lastUpdateFrame + bot.cConfig.maxEnemyInfoAge4Firing < bot.getFrameNumber()) {
            return getNoFiringInstructions(bot, noFiringLook);
        }


//        return getFastFiringInstructions(fd, bot);

//        if (fd.enemyInfo.lastPredictionError > bot.cConfig.maxPredictionError) {
////			bot.dtalk.addToLog("Too big prediction error. Fast firing.");
//            return getFastFiringInstructions(fd, bot);
//        }
        //we may be predicting well
        return getNewPredictingFiringInstructions(bot, fd, bot.cConfig.getBulletSpeedForGivenGun(fd.gunIndex));
    }

    public static FiringInstructions getNewPredictingFiringInstructions(MapBotBase bot,
            FiringDecision fd, float bulletSpeed) {
        Vector3f playerPos = bot.getBotPosition();

        return new FiringInstructions(CommFun.getNormalizedDirectionVector(playerPos, getHitPoint(bot, fd, bulletSpeed)));
    }

    public static Vector3f getHitPoint(MapBotBase bot, FiringDecision fd, float bulletSpeed) {
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
        return hitPoint;
    }

   
//    static public FiringInstructions getFastFiringInstructions(FiringDecision fd, MapBotBase bot) {
//        Vector3f to = new Vector3f(fd.enemyInfo.getBestVisibleEnemyPart(bot));
//        Vector3f fireDir = CommFun.getNormalizedDirectionVector(bot.getBotPosition(), to);
////		bot.dtalk.addToLog("Fast firing.");
//        return new FiringInstructions(fireDir);
//    }

    /**
     * Don't fire, just look at the enemy.
     * @param bot
     * @param enemyPos
     * @return
     */
    public static FiringInstructions getNoFiringInstructions(MapBotBase bot, Vector3f enemyPos) {
        FiringInstructions ret = new FiringInstructions(CommFun.getNormalizedDirectionVector(bot.getBotPosition(), enemyPos));
        ret.doFire = false;
        return ret;
    }


    public static double getTimeToHit(double bulletSpeed, Vector3f playerPos, Vector3f enemyPos, Vector3f enemyPredictedPos) {

        double d = CommFun.getDistanceBetweenPositions(playerPos, enemyPos);
        double v = bulletSpeed;
        double u = CommFun.getDistanceBetweenPositions(enemyPos, enemyPredictedPos);

        Vector3f vec1 = CommFun.getNormalizedDirectionVector(playerPos, enemyPos);
        Vector3f vec2 = CommFun.getNormalizedDirectionVector(enemyPos, enemyPredictedPos);

        //Shouldnt be 180 - alpha ???
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


}
