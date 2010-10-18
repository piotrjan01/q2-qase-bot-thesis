package piotrrr.thesis.bots.mapbotbase;

import java.util.Random;
import piotrrr.thesis.gui.AppConfig;
import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.bots.tuning.CombatConfig;
import piotrrr.thesis.bots.tuning.NavConfig;
import piotrrr.thesis.bots.tuning.WeaponConfig;
import piotrrr.thesis.common.combat.FiringInstructions;
import piotrrr.thesis.common.jobs.BasicCommands;
import piotrrr.thesis.common.jobs.GeneralDebugTalk;
import piotrrr.thesis.common.jobs.StuckDetector;
import piotrrr.thesis.common.navigation.FuzzyEntityRanking;
import piotrrr.thesis.common.navigation.GlobalNav;
import piotrrr.thesis.common.navigation.LocalNav;
import piotrrr.thesis.common.navigation.NavInstructions;
import piotrrr.thesis.common.navigation.NavPlan;
import piotrrr.thesis.common.navigation.WorldKB;
import soc.qase.state.Action;
import soc.qase.state.Entity;
import soc.qase.state.PlayerMove;
import soc.qase.tools.vecmath.Vector3f;

/**
 * This is a basic bot that uses WaypointMap and WorldKB to store knowledge.
 * @author Piotr Gwizdała
 */
public class MapBotBase extends BotBase {

    /**
     * Bot's Knowledge Base about the environment and items it can pick up.
     */
    public WorldKB kb = null;
    /**
     * Bot's current navigation plan
     */
    public NavPlan plan = null;
    /**
     * The job that detects when the bot is stuck.
     */
    public StuckDetector stuckDetector;
    /**
     * The job that handles basic commands.
     */
    BasicCommands basicCommands;
    /**
     * Bot's job that is used to periodically say
     * some debug information in the game.
     */
    public GeneralDebugTalk dtalk;
    /**
     * For debug purposes. We can force the bot to use the specified weapon only.
     */
    public int forcedweapon = 0;
    /**
     * Combat modules configuration
     */
    public CombatConfig cConfig = new CombatConfig();
    /**
     * Weapon preference configuration
     */
    public WeaponConfig wConfig = new WeaponConfig();
    /**
     * The global navigation module of the bot
     */
    protected GlobalNav globalNav = null;
    /**
     * The local navigation module of the bot
     */
    protected LocalNav localNav = null;
    /**
     * The navigation config of the bot.
     */
    public NavConfig nConfig = new NavConfig();



    /**
     * Basic constructor.
     * @param botName the name of the bot to be created
     * @param skinName the name of the skin to be used
     */
    public MapBotBase(String botName, String skinName) {
        super(botName, skinName);
        dtalk = new GeneralDebugTalk(this, 30);
//		dtalk.active = false;

        stuckDetector = new StuckDetector(this, 5);
        basicCommands = new BasicCommands(this, "Player");

        addBotJob(dtalk);
        addBotJob(basicCommands);
        addBotJob(stuckDetector);

    }

    @Override
    protected void botLogic() {

        if (botPaused) {
            return;
        }

        if (kb == null) {
            kb = WorldKB.createKB(AppConfig.botMapsDir + getMapName(), this);
            assert kb != null;
            dtalk.addToLog("KB loaded!");
        }

        kb.updateEnemyInformation();

        kb.updateSeenEntities();

        //MapBotBase doesn't do anything

    }

    /**
     * Executes the instructions got from the plan.
     * @param ni - navigation instructions.
     */
    protected void executeInstructions(NavInstructions ni, FiringInstructions fi) {
        //Do the navigation and look ad good direction
        Vector3f aimDir;
        Vector3f moveDir;
        int walk = PlayerMove.WALK_STOPPED;
        int posture = PlayerMove.POSTURE_NORMAL;

        if (fi != null) {
            aimDir = fi.fireDir;
            //aimDir = randAiming(aimDir);
        } else if (ni != null) {
            aimDir = ni.moveDir;
        } else {
            aimDir = pausedLookDir;
        }

        if (ni != null) {
            moveDir = ni.moveDir;
            walk = ni.walkState;
            posture = ni.postureState;
        } else {
            moveDir = new Vector3f(0, 0, 0);
        }

        if (fi != null) {
            if (fi.doFire) {
                setAction(Action.ATTACK, true);
            } else {
                setAction(Action.ATTACK, false);
            }
        } else {
            setAction(Action.ATTACK, false);
        }

        setBotMovement(moveDir, aimDir, walk, posture);
    }

    protected float angleToMove(int angle) {
        return (float) (Math.sin(Math.toRadians(angle)) / Math.cos(Math.toRadians(angle)));
    }

    protected Vector3f randAiming(Vector3f aim) {
        Vector3f ret = new Vector3f();
        Random r = new Random();
        int error = 5;
        int change1 = -error + (int) (2 * error * r.nextDouble());
        int change2 = -error + (int) (2 * error * r.nextDouble());
        int change3 = -error + (int) (2 * error * r.nextDouble());
        ret.x = aim.x+angleToMove(change1);
        ret.y = aim.y+angleToMove(change2);
        ret.z = aim.z+angleToMove(change3);
        return ret;

    }

    @Override
    public void respawn() {
        super.respawn();
        plan = null;
    }

    @Override
    public void handleCommand(String cmd) {
        basicCommands.handleCommand(cmd);
    }

    @Override
    public String toDetailedString() {

        synchronized (this) {
            if (kb == null) {
                return "";
            }

            String fuzzyState = "";
            fuzzyState += " wd=" + FuzzyEntityRanking.getBotWeaponDeficiency(this, 0);
//                    say += " ammd="+FuzzyEntityRanking.getBotAmmoDeficiency(b, 0);
//                    say += " hd="+FuzzyEntityRanking.getBotHealthDeficiency(b, 0);
//                    say += " armd="+FuzzyEntityRanking.getBotArmorDeficiency(b, 0);
            fuzzyState += " maxDef=" + FuzzyEntityRanking.getMaximalDeficiency(this);

            fuzzyState += " wpns=";
            for (int i = 7; i < 18; i++) {
                if (botHasItem(i)) {
                    fuzzyState += "1";
                } else {
                    fuzzyState += "0";
                }
            }

            fuzzyState += " ammos=";
            for (int i = 18; i < 23; i++) {
                if (botHasItem(i)) {
                    fuzzyState += "1";
                } else {
                    fuzzyState += "0";
                }
            }

            return "Bot name: " + getBotName() + "\n" +
                    "Thread name: " + getName() + "\n" +
                    "health: " + getBotHealth() + "\n" +
                    "armor: " + getBotArmor() + "\n" +
                    "frame nr: " + getFrameNumber() + "\n" +
                    "position: " + getBotPosition() + "\n" +
                    "alive: " + isAlive() + "\n" +
                    "connected: " + isConnected() + "\n" +
                    "state: " + fuzzyState + "\n\n" +
                    kb.toString();
        }
    }

    /**
     * Returns true if some opponent is visible
     * @return
     */
    public boolean isOpponentVisible() {
        for (Object o : world.getOpponents(true)) {
            Entity e = (Entity) o;
            if (getBsp().isVisible(getBotPosition(), e.getObjectPosition())) {
                return true;
            }
        }
        return false;
    }
}
