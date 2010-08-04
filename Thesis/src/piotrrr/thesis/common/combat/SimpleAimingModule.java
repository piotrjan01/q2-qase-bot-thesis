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
        Vector3f noFiringLook = fd.enemyInfo.predictedPos == null ? fd.enemyInfo.getObjectPosition() : fd.enemyInfo.predictedPos;
        if (reloading) {
            return getNoFiringInstructions(bot, noFiringLook);
        }

        if (fd.enemyInfo.lastUpdateFrame + bot.cConfig.maxEnemyInfoAge4Firing < bot.getFrameNumber()) {
            return getNoFiringInstructions(bot, noFiringLook);
        }


//        return getFastFiringInstructions(fd, bot);

        if (fd.enemyInfo.lastPredictionError > bot.cConfig.maxPredictionError) {
//			bot.dtalk.addToLog("Too big prediction error. Fast firing.");
            return getFastFiringInstructions(fd, bot);
        }
        //we may be predicting well
        return getPredictingFiringInstructions(bot, fd, bot.cConfig.getBulletSpeedForGivenGun(fd.gunIndex),
                bot.cConfig.isDangerousToShootWith(fd.gunIndex));
    }

    /**
     * Tries to shoot using the prediction
     * @param bot the bot that shoots
     * @param fd firing decision
     * @param bulletSpeed the speed of the bullets with which the shooting will be performed
     * @param careful whether or not has to be careful while firing with current weapon.
     * @return
     */
    public static FiringInstructions getPredictingFiringInstructions(MapBotBase bot, FiringDecision fd, float bulletSpeed, boolean careful) {
        Vector3f playerPos = bot.getBotPosition();
        Vector3f enemyPos = fd.enemyInfo.getBestVisibleEnemyPart(bot);

        float distance = CommFun.getDistanceBetweenPositions(playerPos, enemyPos);

        if (distance < bot.cConfig.maxShortDistance4Firing) {
//			bot.dtalk.addToLog("Target is very close. Fast firing.");
            return getFastFiringInstructions(fd, bot);
        }

        //Calculate the time to hit
        float timeToHit = distance / bulletSpeed;
        if (timeToHit < 1) {
            timeToHit = 1f;
        }

//		If it is too big - quit
        if (timeToHit > bot.cConfig.maxTimeToHit) {
//			bot.dtalk.addToLog("Target too far. No shooting.");
            return getNoFiringInstructions(bot, enemyPos);
        }

        //We add to enemy position the movement that the enemy is predicted to do in timeToHit.
        Vector3f hitPoint = CommFun.cloneVector(enemyPos);
        //movement is between bot position, not the visible part of the bot
        Vector3f movement = CommFun.getMovementBetweenVectors(fd.enemyInfo.getObjectPosition(), fd.enemyInfo.predictedPos);
        movement = CommFun.multiplyVectorByScalar(movement, timeToHit);
        hitPoint.add(movement);

        if (careful && bot.getBsp().getObstacleDistance(playerPos, hitPoint, 20.0f, bot.cConfig.maxShortDistance4Firing * 2) < bot.cConfig.maxShortDistance4Firing) {
//			bot.dtalk.addToLog("Being careful. No shooting!");
            return getNoFiringInstructions(bot, enemyPos);
        }
//		bot.dtalk.addToLog("Prediction shooting: @"+fd.enemyInfo.ent.getName()+" gun: "+CommFun.getGunName(fd.gunIndex)+"\n pred mov: "+movement+" timeToHit: "+timeToHit+" dist: "+distance+" bspeed: "+bulletSpeed);
        return new FiringInstructions(CommFun.getNormalizedDirectionVector(playerPos, hitPoint));
    }

    

    static public FiringInstructions getFastFiringInstructions(FiringDecision fd, MapBotBase bot) {
        Vector3f to = new Vector3f(fd.enemyInfo.getBestVisibleEnemyPart(bot));
        Vector3f fireDir = CommFun.getNormalizedDirectionVector(bot.getBotPosition(), to);
//		bot.dtalk.addToLog("Fast firing.");
        return new FiringInstructions(fireDir);
    }

   

    /**
     * Don't fire, just look at the enemy.
     * @param bot
     * @param enemyPos
     * @return
     */
    static FiringInstructions getNoFiringInstructions(MapBotBase bot, Vector3f enemyPos) {
        FiringInstructions ret = new FiringInstructions(CommFun.getNormalizedDirectionVector(bot.getBotPosition(), enemyPos));
        ret.doFire = false;
        return ret;
    }
}
