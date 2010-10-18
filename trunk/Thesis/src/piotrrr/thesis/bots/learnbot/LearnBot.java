package piotrrr.thesis.bots.learnbot;

import org.apache.log4j.Logger;
import piotrrr.thesis.bots.referencebot.ReferenceBot;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.combat.FiringDecision;
import piotrrr.thesis.common.combat.FiringInstructions;
import piotrrr.thesis.common.combat.SimpleAimingModule;
import piotrrr.thesis.common.combat.SimpleCombatModule;
import piotrrr.thesis.common.navigation.FuzzyGlobalNavContra;
import piotrrr.thesis.common.navigation.NavInstructions;
import piotrrr.thesis.tools.Dbg;

public class LearnBot extends ReferenceBot {

    Logger log = Logger.getLogger(LearnBot.class);
    LearnBotAimingModule aimModule = null;

    public LearnBot(String botName, String skinName) {
        super(botName, skinName);

        globalNav = new FuzzyGlobalNavContra();

        try {
            aimModule = (LearnBotAimingModule) CommFun.readFromFile("LearnBot-aimModule");
        } catch (Exception ex) {
            log.error("couldnt read aim module!", ex);
        }

    }

    @Override
    protected void botLogic() {
        super.botLogic();

        NavInstructions ni = null;
        if (!noMove) {

            plan = globalNav.establishNewPlan(this, plan);

            if (plan == null) {
                Dbg.prn("plan is null....");
                return;
            }
            assert plan != null;
            ni = localNav.getNavigationInstructions(this);
        }

        FiringDecision fd = null;
        if (!noFire) {
            fd = SimpleCombatModule.getFiringDecision(this);
            if (fd != null && getWeaponIndex() != fd.gunIndex) {
                changeWeaponByInventoryIndex(fd.gunIndex);
            }
        }

//        FiringInstructions fi = aimModule.getFiringInstructions(fd, this);
        FiringInstructions fi = SimpleAimingModule.getFiringInstructions(fd, this);

        executeInstructions(ni, fi);


    }
}
