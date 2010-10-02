package piotrrr.thesis.bots.referencebot;

import java.util.HashMap;
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
import soc.qase.tools.vecmath.Vector3f;

public class ReferenceBot extends MapBotBase {

    public static int botsCount = 0;
    private int botNr = 0;
    FileLogger fLog = null;
    String comma = ",";
    public boolean saveExamplesToFile = false;

    public ReferenceBot(String botName, String skinName) {
        super(botName, skinName);

        globalNav = new FuzzyGlobalNav();
        localNav = new SimpleLocalNav();

        botNr = botsCount;
        botsCount++;
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
        FiringInstructions fi = SimpleAimingModule.getFiringInstructions(fd, this);

        if (saveExamplesToFile && fi != null) {

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

            saveExamplesToFile(wpn, d0, d1, enemyMoveDirRelative, fireDirRelative);

        }

        executeInstructions(ni, fi);


    }

    protected void saveExamplesToFile(int wpn, double dist0, double dist1, Vector3f enemyMove, Vector3f fireDir) {
        //"wpn,dist0,dist1,emx,emy,emz,fdx,fdy,fdz"

        if (fLog == null) {
            fLog = new FileLogger("WpnExamples-bot" + botNr + ".csv");
            fLog.addToLog("wpn" + comma + "dist0" + comma + "dist1" + comma +
                    "emx" + comma + "emy" + comma + "emz" + comma +
                    "fdx" + comma + "fdy" + comma + "fdz\n");
        }        
        fLog.addToLog(wpn + comma + dist0 + comma + dist1 + comma +
                enemyMove.x + comma + enemyMove.y + comma + enemyMove.z + comma +
                fireDir.x + comma + fireDir.y + comma + fireDir.z + "\n");
    }
}
