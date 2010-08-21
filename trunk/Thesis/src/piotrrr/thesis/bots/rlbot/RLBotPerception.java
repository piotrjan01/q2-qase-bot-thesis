/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.rlbot;

import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.DamageMsgsParser;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.common.stats.StatsTools;
import piotrrr.thesis.gui.MyPopUpDialog;
import pl.gdan.elsy.qconf.Perception;

/**
 *
 * @author piotrrr
 */
public class RLBotPerception extends Perception {

    public boolean useDistance = true;

    RLBot bot;
    int lastBotScore = 0;

    public RLBotPerception(RLBot bot) {
        this.bot = bot;
        lastBotScore = StatsTools.getBotScore(bot.getBotName(), BotStatistic.getInstance());
    }

    void resetReward() {
        DamageMsgsParser.reset(bot.getBotName());
        lastBotScore = StatsTools.getBotScore(bot.getBotName(), BotStatistic.getInstance());
    }

    @Override
    public boolean isUnipolar() {
        return true;
    }

    @Override
    public double getReward() {
        double r = 0;
        int score = StatsTools.getBotScore(bot.getBotName(), BotStatistic.getInstance());
        if (score - lastBotScore != 0) {
            r += (score - lastBotScore) * 0.3;
        }
        int[] dmg = DamageMsgsParser.getInflictedDamage(bot.getBotName());
        r += ((dmg[0] + dmg[1]) / 100.0) * 0.3;
        if (r > 1) {
            System.err.println(bot.getBotName()+": excessive reward: "+r);
            r = 1;
        }
        if (r < -1) {
            System.err.println(bot.getBotName()+": excessive reward: "+r);
            r = -1;
        }
        return r;
    }

    @Override
    protected void updateInputValues() {

        double dist;
        //for brain constructor call
        if (bot.fd == null) {
            dist = 0;
        } else {
            dist = CommFun.getDistanceBetweenPositions(bot.getBotPosition(),
                    bot.fd.enemyInfo.getObjectPosition());
        }
        if (useDistance) {
            setNextValue(dist);
//            int close = 300, far = 900;
//            if (dist < close) setNextValue(true);
//            else setNextValue(false);
//
//            if (dist >= close && dist <=far) setNextValue(true);
//            else setNextValue(false);
//
//            if (dist > far) setNextValue(true);
//            else setNextValue(false);
        }
//        float ownedWpnWeights = 0;
        for (int i = 7; i < 18; i++) {
            float ammState = bot.getAmmunitionState(i);
            if (!bot.botHasItem(i)) {
                ammState = 0;
            }
            if (ammState > 1) {
                ammState = 1;
            }
            setNextValue(ammState);
//-----------------------------
//            boolean b = bot.botHasItem(i) && bot.botHasItem(PlayerGun.getAmmoInventoryIndexByGun(i));
//            setNextValue(b);
//------------------------------
//            if (bot.botHasItem(i) && bot.botHasItem(PlayerGun.getAmmoInventoryIndexByGun(i))) {
//                ownedWpnWeights += bot.wConfig.getWeaponWeightByInvIndex(i);
//            }
        }
//        setNextValue(ownedWpnWeights);
    }
}
