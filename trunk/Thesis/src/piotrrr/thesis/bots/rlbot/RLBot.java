package piotrrr.thesis.bots.rlbot;

import piotrrr.thesis.bots.tuning.NavConfig;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.navigation.FuzzyGlobalNav;
import piotrrr.thesis.common.navigation.SimpleLocalNav;
import piotrrr.thesis.common.combat.FiringDecision;
import piotrrr.thesis.common.combat.FiringInstructions;

import piotrrr.thesis.common.navigation.NavInstructions;

public class RLBot extends MapBotBase {

    public NavConfig nConfig = new NavConfig();
    public RLCombatModule combatModule = new RLCombatModule(this);
    public int lastBotScore = 0;
    public double totalReward = 0;
    public double rewardsCount = 0;
    FiringDecision fd = null;

    public RLBot(String botName, String skinName) {
        super(botName, skinName);
        globalNav = new FuzzyGlobalNav();
        localNav = new SimpleLocalNav();
    }

    @Override
    protected void botLogic() {
        super.botLogic();

        FiringInstructions fi = null;
        fd = null;
        if (!noFire) {
            fd = combatModule.getFiringDecision();
            fi = combatModule.getFiringInstructions(fd);
        }

        NavInstructions ni = null;
        if (!noMove) {
            plan = globalNav.establishNewPlan(this, plan);
            if (plan == null) {
//                Dbg.prn("plan is null....");
                return;
            }
            ni = localNav.getNavigationInstructions(this);
//            assert plan != null;            
        }
        executeInstructions(ni, fi);
    }

    @Override
    public String toDetailedString() {
        String s = super.toDetailedString();
        s += combatModule.toString();
        return s;
    }
}
