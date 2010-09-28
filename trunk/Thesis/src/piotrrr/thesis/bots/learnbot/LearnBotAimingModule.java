/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.bots.learnbot;

import java.io.Serializable;
import java.util.HashMap;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
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

    HashMap<Integer, WpnExamples> examples = new HashMap<Integer, WpnExamples>();

    public static int [] validWeapons = {7, 8, 9, 10, 11, 14, 15, 16, 17};

    public LearnBotAimingModule() {
        
        

    }





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

        

        return null;
    }


}
