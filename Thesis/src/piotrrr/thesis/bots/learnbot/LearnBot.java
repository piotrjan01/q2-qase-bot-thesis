package piotrrr.thesis.bots.learnbot;

import org.apache.log4j.Logger;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.combat.FiringDecision;
import piotrrr.thesis.common.combat.FiringInstructions;
import piotrrr.thesis.common.combat.SimpleAimingModule;
import piotrrr.thesis.common.combat.SimpleCombatModule;
import piotrrr.thesis.common.navigation.TuningGlobalNav;
import piotrrr.thesis.common.navigation.NavInstructions;
import piotrrr.thesis.common.navigation.SimpleLocalNav;
import piotrrr.thesis.tools.Dbg;

public class LearnBot extends MapBotBase {

    public LearnBot(String botName, String skinName) {
        super(botName, skinName);         
        localNav = new SimpleLocalNav();
        globalNav = new TuningGlobalNav();
    }

    @Override
    protected void botLogic() {
        super.botLogic();

        //TODO: remove it
        if (getFrameNumber() > 18000) {
            disconnect();
            return;
        }

        NavInstructions ni = null;
        if (!noMove) {

            plan = globalNav.establishNewPlan(this, plan);

            if (plan == null) {
                Dbg.prn("plan is null....");
                return;
            }            
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
