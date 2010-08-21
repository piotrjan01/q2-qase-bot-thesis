package piotrrr.thesis.common.jobs;

import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.navigation.FuzzyEntityRanking;
import piotrrr.thesis.tools.Dbg;

/**
 * Dummy job, that repeats some debug info with given period.
 * @author Piotr Gwizdała
 */
public class GeneralDebugTalk extends Job {

    public boolean active = true;
    long lastFrame;
    long lastAddingFrame;
    int period;
    String toSay = "";

    public GeneralDebugTalk(BotBase bot, int period) {
        super(bot);
        this.period = period;
        lastFrame = bot.getFrameNumber();
        lastAddingFrame = lastFrame;
    }

    @Override
    public void run() {
//        bot.say("kaka");
        if (!active) {
            return;
        }
        if (bot.botPaused) {
            return;
        }
        if (bot.getFrameNumber() - lastFrame < period) {
            return;
        }
        if (toSay.equals("")) {
            return;
        }

        lastFrame = bot.getFrameNumber();

        float h = bot.getBotHealth();
        float a = bot.getBotArmor();


        String say = toSay + "  H=" + h + " A=" + a;
//                try {
//                    MapBotBase b = (MapBotBase) bot;
////                    say += " wd="+FuzzyEntityRanking.getBotWeaponDeficiency(b, 0);
////                    say += " ammd="+FuzzyEntityRanking.getBotAmmoDeficiency(b, 0);
////                    say += " hd="+FuzzyEntityRanking.getBotHealthDeficiency(b, 0);
////                    say += " armd="+FuzzyEntityRanking.getBotArmorDeficiency(b, 0);
//                    say+= " maxDef="+FuzzyEntityRanking.getMaximalDeficiency(b);
//
//                    say+=" wpns=";
//                    for (int i=7; i<18; i++) {
//                        if (b.botHasItem(i)) say+="1";
//                        else say+="0";
//                    }
//
//                    say+=" ammos=";
//                    for (int i=18; i<23; i++) {
//                        if (b.botHasItem(i)) say+="1";
//                        else say+="0";
//                    }
//
//
//                }
//                catch (Exception e) {
//
//                }
//
//        bot.say(say);
        toSay = "";

    }

    public void addToLog(String s) {
        if (!active) {
            return;
        }
        double frame = (bot.getFrameNumber() - lastAddingFrame) / 10.0;
        lastAddingFrame = bot.getFrameNumber();
        Dbg.prn(bot.getBotName() + ":\t" + frame + ">\tBOT SAYS: " + s);
        toSay += s + " :: ";
    }
}
