/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.rlbot;

import piotrrr.thesis.bots.tuning.WeaponConfig;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.DamageMsgsParser;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.common.stats.StatsTools;
import piotrrr.thesis.gui.MyPopUpDialog;
import piotrrr.thesis.tools.Dbg;
import pl.gdan.elsy.qconf.Perception;
import soc.qase.state.PlayerGun;

/**
 *
 * @author piotrrr
 */
public class RLBotPerception extends Perception {

    public boolean useAccumulatedGuns = true;
    RLBot bot;
    int lastBotScore = 0;
    int lastBotDeaths = 0;

    public RLBotPerception(RLBot bot) {
        this.bot = bot;
        lastBotScore = StatsTools.getBotScore(bot.getBotName(), BotStatistic.getInstance());
        lastBotDeaths = StatsTools.getBotDeaths(bot.getBotName(), BotStatistic.getInstance());
    }

    void resetReward() {
        DamageMsgsParser.reset(bot.getBotName());
        lastBotScore = StatsTools.getBotScore(bot.getBotName(), BotStatistic.getInstance());
        lastBotDeaths = StatsTools.getBotDeaths(bot.getBotName(), BotStatistic.getInstance());
    }

    @Override
    public boolean isUnipolar() {
        return true;
    }

    @Override
    public double getReward() {
        double r = 0;

        //kills
        int score = StatsTools.getBotScore(bot.getBotName(), BotStatistic.getInstance());
        if (score - lastBotScore != 0) {
            r += (score - lastBotScore) * 0.4;
            int bd = StatsTools.getBotDeaths(bot.getBotName(), BotStatistic.getInstance());
            Dbg.prn("-----------------------------> Got score! RL "+score+" : "+bd+" - "+bot.combatModule.currentAction.toString());
        }

        //deaths
        int deaths = StatsTools.getBotDeaths(bot.getBotName(), BotStatistic.getInstance());
        if (deaths - lastBotDeaths != 0) {
            r -= (deaths - lastBotDeaths) * 0.05;
        }

//        //received dmg
//        int[] rdmg = DamageMsgsParser.getReceivedDamage(bot.getBotName());
//        r -= ((rdmg[0] + rdmg[1]) / 100.0) * 0.05;

        //inflicted damage
        int[] dmg = DamageMsgsParser.getInflictedDamage(bot.getBotName());
        r += ((dmg[0] + dmg[1]) / 100.0) * 0.1;

//        System.out.println("given="+(dmg[0]+dmg[1])+" received="+(rdmg[0]+rdmg[1]));

        //normalize
        if (r > 1) {
            System.err.println(bot.getBotName() + ": excessive reward: " + r);
            r = 1;
        }
        if (r < -1) {
            System.err.println(bot.getBotName() + ": excessive reward: " + r);
            r = -1;
        }
        return r;
    }

    @Override
    protected void updateInputValues() {

        //DIST TO ENEMY
        double dist;
        //for brain constructor call
        if (bot.fd == null) {
            dist = 0;
        } else {
            dist = CommFun.getDistanceBetweenPositions(bot.getBotPosition(),
                    bot.fd.enemyInfo.getObjectPosition());
        }
//------------------------------------
//            setNextValue(dist);
//------------------------------------
//            int close = 300, far = 900;
//            if (dist < close) setNextValue(true);
//            else setNextValue(false);
//
//            if (dist >= close && dist <=far) setNextValue(true);
//            else setNextValue(false);
//
//            if (dist > far) setNextValue(true);
//            else setNextValue(false);
//------------------------------------
        dist = dist / 2500;
        dist = (dist > 1) ? 1 : dist;
        setNextValue(dist);
//------------------------------------      
        //OWNED GUNS:
//        float ownedWpnWeights = 0;
//        float allWpnWeights = 0;
        for (int i = 7; i < 18; i++) {
//------------------------------------
//            if (useAccumulatedGuns) { //PROVED TO BE BETTER:
                float ammState = bot.getAmmunitionState(i);
                if (!bot.botHasItem(i)) {
                    ammState = 0;
                }
                if (ammState > 1) {
                    ammState = 1;
                }
                setNextValue(ammState);
//            } else {
//-----------------------------
//                boolean b = bot.botHasItem(i) && bot.botHasItem(PlayerGun.getAmmoInventoryIndexByGun(i));
//                setNextValue(b);
//            }
//------------------------------
//            allWpnWeights += bot.wConfig.getWeaponWeightByInvIndex(i);
//            if (bot.botHasItem(i) && bot.botHasItem(PlayerGun.getAmmoInventoryIndexByGun(i))) {
//                ownedWpnWeights += bot.wConfig.getWeaponWeightByInvIndex(i);
//            }
//------------------------------------
        }
//        setNextValue(ownedWpnWeights/allWpnWeights);

        //ENEMY GUN
        if (bot.fd == null) {
            setNextValue(0);
        } else {
            int eg = bot.fd.enemyInfo.ent.getWeaponInventoryIndex();
            setNextValue(eg);
//            setNextValue((float) bot.wConfig.getWeaponWeightByInvIndex(eg) / 100f);
        }
        //MY HLTH
        float h = (bot.getBotArmor() + bot.getBotHealth()) / 120f;
        if (h > 1) {
            h = 1;
        }
        if (h < 0) {
            h = 0;
        }
        setNextValue(h);

    }
}
