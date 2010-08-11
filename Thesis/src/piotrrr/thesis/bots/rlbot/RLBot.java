package piotrrr.thesis.bots.rlbot;

import piotrrr.thesis.bots.tuning.NavConfig;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.navigation.ReferenceBotGlobalNav;
import piotrrr.thesis.common.navigation.ReferenceBotLocalNav;
import piotrrr.thesis.common.combat.FiringDecision;
import piotrrr.thesis.common.combat.FiringInstructions;
import piotrrr.thesis.common.jobs.CountMyScoreJob;
import piotrrr.thesis.common.navigation.NavInstructions;

public class RLBot extends MapBotBase {

    public NavConfig nConfig = new NavConfig();
    public CountMyScoreJob scoreCounter;
    public RLCombatModule combatModule = new RLCombatModule(this);
    public int lastBotScore = 0;
    public double totalReward = 0;
    public double rewardsCount = 0;

    public RLBot(String botName, String skinName) {
        super(botName, skinName);
        scoreCounter = new CountMyScoreJob(this);
        addBotJob(scoreCounter);

        globalNav = new ReferenceBotGlobalNav();
        localNav = new ReferenceBotLocalNav();
    }

    @Override
    protected void botLogic() {
        super.botLogic();

        NavInstructions ni = null;
        if (!noMove) {
            plan = globalNav.establishNewPlan(this, plan);
            if (plan == null) {
//                Dbg.prn("plan is null....");
                return;
            }
            assert plan != null;
            ni = localNav.getNavigationInstructions(this);
        }
        FiringDecision fd = null;
        if (!noFire) {
            fd = combatModule.getFiringDecision();
        }

        FiringInstructions fi = combatModule.getFiringInstructions(fd);
        if (fi != null && fi.doFire) {
        }

        executeInstructions(ni, fi);
    }

    @Override
    public String toDetailedString() {
        String s = super.toDetailedString();
        s+=combatModule.toString();
        return s;
    }



}
