package piotrrr.thesis.bots.learnbot;

import org.apache.log4j.Logger;
import piotrrr.thesis.bots.referencebot.ReferenceBot;
import piotrrr.thesis.common.combat.FiringDecision;
import piotrrr.thesis.common.combat.FiringInstructions;
import piotrrr.thesis.common.combat.SimpleAimingModule;
import piotrrr.thesis.common.combat.SimpleCombatModule;
import piotrrr.thesis.common.navigation.TuningGlobalNav;
import piotrrr.thesis.common.navigation.NavInstructions;
import piotrrr.thesis.tools.Dbg;

public class LearnBot extends ReferenceBot {

    Logger log = Logger.getLogger(LearnBot.class);

    public LearnBot(String botName, String skinName) {
        super(botName, skinName);

        globalNav = new TuningGlobalNav();
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
        FiringInstructions fi = SimpleAimingModule.getFiringInstructions(fd, this);

        executeInstructions(ni, fi);


    }
}
