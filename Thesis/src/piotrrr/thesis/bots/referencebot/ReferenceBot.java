package piotrrr.thesis.bots.referencebot;

import piotrrr.thesis.common.navigation.FuzzyGlobalNav;
import piotrrr.thesis.common.navigation.SimpleLocalNav;
import piotrrr.thesis.bots.tuning.NavConfig;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.combat.FiringDecision;
import piotrrr.thesis.common.combat.FiringInstructions;
import piotrrr.thesis.common.combat.SimpleAimingModule;
import piotrrr.thesis.common.combat.SimpleCombatModule;
import piotrrr.thesis.common.navigation.NavInstructions;
import piotrrr.thesis.tools.Dbg;
import piotrrr.thesis.tools.Timer;

public class ReferenceBot extends MapBotBase {

    public ReferenceBot(String botName, String skinName) {
        super(botName, skinName);
        globalNav = new FuzzyGlobalNav();
        localNav = new SimpleLocalNav();
        timers.put("glob-nav", new Timer("glob-nav"));
        timers.put("rank", new Timer("rank"));
        timers.put("aim", new Timer("aim"));
        timers.put("fd", new Timer("fd"));
        timers.put("nav0", new Timer("nav0"));
        timers.put("nav1", new Timer("nav1"));
//        timers.put("nav2", new Timer("nav2"));
//        timers.put("nav3", new Timer("nav3"));
//        timers.put("nav4", new Timer("nav4"));

    }

    @Override
    protected void botLogic() {
        super.botLogic();

        NavInstructions ni = null;
        if (!noMove) {

            timers.get("glob-nav").resume();
            plan = globalNav.establishNewPlan(this, plan);
            timers.get("glob-nav").pause();
            
            if (plan == null) {
                Dbg.prn("plan is null....");
                return;
            }
            assert plan != null;
            ni = localNav.getNavigationInstructions(this);
        }

        FiringDecision fd = null;
        if (!noFire) {
            timers.get("fd").resume();
            fd = SimpleCombatModule.getFiringDecision(this);
            timers.get("fd").pause();
            if (fd != null && getWeaponIndex() != fd.gunIndex) {
                changeWeaponByInventoryIndex(fd.gunIndex);
            }
//			else {
//				int justInCaseWeaponIndex = SimpleCombatModule.chooseWeapon(this, cConfig.maxShortDistance4WpChoice+0.1f);
//				if (getWeaponIndex() != justInCaseWeaponIndex)
//					changeWeaponByInventoryIndex(justInCaseWeaponIndex);
//			}
        }
        timers.get("aim").resume();
        FiringInstructions fi = SimpleAimingModule.getFiringInstructions(fd, this);
        timers.get("aim").pause();

        executeInstructions(ni, fi);
    }
}
