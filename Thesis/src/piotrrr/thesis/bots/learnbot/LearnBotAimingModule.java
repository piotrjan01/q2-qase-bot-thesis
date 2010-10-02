/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.learnbot;

import java.io.Serializable;
import java.util.HashMap;
import org.apache.log4j.Logger;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.combat.FiringDecision;
import piotrrr.thesis.common.combat.FiringInstructions;
import piotrrr.thesis.common.combat.SimpleAimingModule;
import soc.qase.tools.vecmath.Vector3f;

/**
 *
 * @author piotrrr
 */
public class LearnBotAimingModule implements Serializable {

    HashMap<Integer, WpnHandler> handlers = new HashMap<Integer, WpnHandler>();
    public static int[] validWeapons = {7, 8, 9, 10, 11, 14, 15, 16, 17};
    static Logger log = Logger.getLogger(LearnBotAimingModule.class);

    /**
     * Returns the firing instructions
     * @param fd firing decision
     * @param bot the bot
     * @return
     */
    public FiringInstructions getFiringInstructions(FiringDecision fd, MapBotBase bot) {
        if (fd == null || fd.enemyInfo == null) {
            return null;
        }

        boolean reloading = bot.getWorld().getPlayer().getPlayerGun().isCoolingDown();
        Vector3f noFiringLook = fd.enemyInfo.getObjectPosition();

        if (reloading) {
            return SimpleAimingModule.getNoFiringInstructions(bot, noFiringLook);
        }

        if (fd.enemyInfo.lastUpdateFrame + bot.cConfig.maxEnemyInfoAge4Firing < bot.getFrameNumber()) {
            return SimpleAimingModule.getNoFiringInstructions(bot, noFiringLook);
        }

        FiringInstructions fi = getLearnersFiringInstructons(bot, fd);
        if (fi == null) {
            return SimpleAimingModule.getNoFiringInstructions(bot, noFiringLook);
        }
        return fi;
    }

    FiringInstructions getLearnersFiringInstructons(MapBotBase bot, FiringDecision fd) {
        WpnHandler wh = handlers.get(fd.gunIndex);
        if (wh == null) {
            return null;
        }

        Vector3f botPos = bot.getBotPosition();
        Vector3f enemPos = fd.enemyInfo.getObjectPosition();
        Vector3f enemPredPos = fd.enemyInfo.predictedPos;

        Vector3f toEnemyDir = CommFun.getNormalizedDirectionVector(botPos, enemPos);

        double d0 = CommFun.getDistanceBetweenPositions(botPos, enemPos);
        double d1 = CommFun.getDistanceBetweenPositions(botPos, enemPredPos);

        //vector from me to the enemy next position, relative to toEnemyDir
        Vector3f enemyMoveDirRelative = CommFun.getNormalizedDirectionVector(botPos, enemPredPos);
        enemyMoveDirRelative.sub(toEnemyDir);
        
        try {
            Vector3f fireDirRelative = wh.classify(fd.gunIndex, d0, d1, enemyMoveDirRelative);
            
            Vector3f fireDir = new Vector3f(toEnemyDir);
            fireDir.add(fireDirRelative);
            FiringInstructions fi = new FiringInstructions(fireDir);
            return fi;
        } catch (Exception ex) {
            log.error("Exception when classifying!", ex);
            return null;
        }

    }

    public static LearnBotAimingModule createAimingModule(WpnExamplesSet exmpls) throws Exception {
        LearnBotAimingModule mod = new LearnBotAimingModule();

        for (int i : exmpls.examples.keySet()) {
            log.info("Getting examples for wpn: " + i);
            WpnExamples we = exmpls.examples.get(i);
            log.info("Creating handler for wpn: " + i);
            WpnHandler wh = new WpnHandler();
            wh.learn(we);
            mod.handlers.put(i, wh);
        }
        return mod;
    }

    @Override
    public String toString() {
        String ret = this.getClass().getSimpleName()+":\n";
        for (int i : handlers.keySet()) {
            ret+="Handler for wpn: "+i+"\n";
            ret+= handlers.get(i).toString()+"\n";
        }
        return ret;
    }

    public String evaluate(WpnExamplesSet exmpls) throws Exception {
        String ret = "Evaluation results:\n";
        for (int i : exmpls.examples.keySet()) {
            ret+="Weapon: "+CommFun.getGunName(i)+"\n";
            WpnExamples we = exmpls.examples.get(i);
            if (handlers.containsKey(i)) {
                log.info("Evaluating for wpn: "+CommFun.getGunName(i));
                WpnHandler wh = handlers.get(i);
                ret+=wh.evaluate(we)+"\n\n";
            }
            else log.info("No handler found for weapon: "+CommFun.getGunName(i));
        }
        return ret;
    }

}
