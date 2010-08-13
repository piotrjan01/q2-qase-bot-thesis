package piotrrr.thesis.bots.rlbot;

import piotrrr.thesis.bots.tuning.NavConfig;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.navigation.FuzzyGlobalNav;
import piotrrr.thesis.common.navigation.SimpleLocalNav;
import piotrrr.thesis.common.combat.FiringDecision;
import piotrrr.thesis.common.combat.FiringInstructions;
import piotrrr.thesis.common.jobs.CountMyScoreJob;
import piotrrr.thesis.common.navigation.NavInstructions;
import piotrrr.thesis.tools.Timer;

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

        timers.put("glob-nav", new Timer("glob-nav"));
        timers.put("rank", new Timer("rank"));
        timers.put("aim", new Timer("aim"));
        timers.put("fd", new Timer("fd"));
        timers.put("nav0", new Timer("nav0"));
        timers.put("nav1", new Timer("nav1"));
//        timers.put("nav2", new Timer("nav2"));
//        timers.put("nav3", new Timer("nav3"));
//        timers.put("nav4", new Timer("nav4"));

        globalNav = new FuzzyGlobalNav();
        localNav = new SimpleLocalNav();
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
//                Dbg.prn("plan is null....");
                return;
            }
            assert plan != null;
            ni = localNav.getNavigationInstructions(this);
        }
        FiringDecision fd = null;

        

        if (!noFire) {
            timers.get("fd").resume();
            fd = combatModule.getFiringDecision();
            timers.get("fd").pause();
        }

        timers.get("aim").resume();
        FiringInstructions fi = combatModule.getFiringInstructions(fd);
        timers.get("aim").pause();

        executeInstructions(ni, fi);
    }

    @Override
    public String toDetailedString() {
        String s = super.toDetailedString();
        s += combatModule.toString();
        return s;
    }

    @Override
    public void respawn() {
        super.respawn();
        combatModule.brain.reset();
    }



}
