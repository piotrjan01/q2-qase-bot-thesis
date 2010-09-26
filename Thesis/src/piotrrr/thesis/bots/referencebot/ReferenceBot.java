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

    public static int botsCount = 0;
    FileLogger fLog = null;
    String comma = ",";
    double lastPitch = 0;
    double lastYaw = 0;

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
        botsCount++;
        fLog = new FileLogger("ReferenceBot-" + botsCount + "-aim.csv");
        fLog.addToLog("wpn,dist0,dist1,emx,emy,emz,fdx,fdy,fdz\n");

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

        if (fi != null) {

            Vector3f botPos = getBotPosition();
            Vector3f enemPos = fd.enemyInfo.getObjectPosition();
            Vector3f enemPredPos = fd.enemyInfo.predictedPos;            

            int wpn = fd.gunIndex;

            //vector from me to enemy
            Vector3f toEnemyDir = CommFun.getNormalizedDirectionVector(botPos, enemPos);

            //vector from me to the enemy next position, relative to toEnemyDir
            Vector3f enemyMoveDirRelative = CommFun.getNormalizedDirectionVector(botPos, enemPredPos);
            enemyMoveDirRelative.sub(toEnemyDir);

            //vector of fire direction, relative to toEnemyDir.
            Vector3f fireDirRelative = new Vector3f(fi.fireDir);
            fireDirRelative.sub(toEnemyDir);

            double d0 = CommFun.getDistanceBetweenPositions(botPos, enemPos);
            double d1 = CommFun.getDistanceBetweenPositions(botPos, enemPredPos);

            saveDataToFile(wpn, d0, d1, enemyMoveDirRelative, fireDirRelative);

        }

        executeInstructions(ni, fi);


    }

//    protected double getPitch(Vector3f from, Vector3f to) {
//        Vector3f v = CommFun.getNormalizedDirectionVector(from, to);
//        Angles ang = new Angles(v.x, v.y, v.z);
//
//    }
    protected void saveDataToFile(int wpn, double dist0, double dist1, Vector3f enemyMove, Vector3f fireDir) {
        //"wpn,dist0,dist1,emx,emy,emz,fdx,fdy,fdz"
        fLog.addToLog(wpn + comma + dist0 + comma + dist1 + comma +
                enemyMove.x + comma + enemyMove.y + comma + enemyMove.z + comma +
                fireDir.x + comma + fireDir.y + comma + fireDir.z + "\n");
    }
}
