package piotrrr.thesis.bots.referencebot;

import piotrrr.thesis.common.navigation.FuzzyGlobalNav;
import piotrrr.thesis.common.navigation.SimpleLocalNav;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.combat.FiringDecision;
import piotrrr.thesis.common.combat.FiringInstructions;
import piotrrr.thesis.common.combat.SimpleAimingModule;
import piotrrr.thesis.common.combat.SimpleCombatModule;
import piotrrr.thesis.common.navigation.NavInstructions;
import piotrrr.thesis.tools.Dbg;
import piotrrr.thesis.tools.FileLogger;
import piotrrr.thesis.tools.Timer;
import soc.qase.tools.vecmath.Vector3f;

public class ReferenceBot extends MapBotBase {

    FileLogger fLog = null;

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

        fLog = new FileLogger(botName + "-aim.csv");
        fLog.addToLog("wpn;d0;d1;bx;by;bz;hx;hy;hz\n");

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

        if (fi != null && fi.doFire) {
            Vector3f bc = CommFun.getNormalizedDirectionVector(getBotPosition(), fd.enemyInfo.getObjectPosition());
            Vector3f bp = CommFun.getNormalizedDirectionVector(getBotPosition(), fd.enemyInfo.predictedPos);
            bp.sub(bc);
            Vector3f hp = CommFun.getNormalizedDirectionVector(getBotPosition(), SimpleAimingModule.getHitPoint(this, fd, cConfig.getBulletSpeedForGivenGun(fd.gunIndex)));
            hp.sub(bc);
            saveDataToFile(bp, hp, CommFun.getGunName(fd.gunIndex),
                    CommFun.getDistanceBetweenPositions(getBotPosition(), fd.enemyInfo.getObjectPosition()),
                    CommFun.getDistanceBetweenPositions(getBotPosition(), fd.enemyInfo.predictedPos));
        }

        executeInstructions(ni, fi);


    }

    protected void saveDataToFile(Vector3f bv, Vector3f hv, String wpn, float d0, float d1) {
        //"dist;wpn;mx;my;mz;hx;hy;hz\n"
        //wpn;d0;d1;bx;by;bz;hx;hy;hz\n
        fLog.addToLog(wpn + ";" + d0 + ";" + d1 + ";" + bv.x + ";" + bv.y + ";" + bv.z + ";" + hv.x + ";" + hv.y + ";" + hv.z + "\n");
    }
}
