package piotrrr.thesis.bots.learnbot;

import java.util.logging.Level;
import java.util.logging.Logger;
import piotrrr.thesis.bots.referencebot.ReferenceBot;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.combat.FiringDecision;
import piotrrr.thesis.common.combat.FiringInstructions;
import piotrrr.thesis.common.combat.SimpleAimingModule;
import piotrrr.thesis.common.combat.SimpleCombatModule;
import piotrrr.thesis.common.navigation.NavInstructions;
import piotrrr.thesis.tools.Dbg;

public class LearnBot extends ReferenceBot {


    LearnBotAimingModule aimModule = null;

    public LearnBot(String botName, String skinName) {
        super(botName, skinName);
        
        try {
            aimModule = (LearnBotAimingModule) CommFun.readFromFile("LearnBot-aimModule");
        } catch (Exception ex) {
            Logger.getLogger(LearnBot.class.getName()).log(Level.SEVERE, "couldnt read aim module!", ex);
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
//			else {
//				int justInCaseWeaponIndex = SimpleCombatModule.chooseWeapon(this, cConfig.maxShortDistance4WpChoice+0.1f);
//				if (getWeaponIndex() != justInCaseWeaponIndex)
//					changeWeaponByInventoryIndex(justInCaseWeaponIndex);
//			}
        }
        
        FiringInstructions fi = aimModule.getFiringInstructions(fd, this);

        executeInstructions(ni, fi);


    }


}
