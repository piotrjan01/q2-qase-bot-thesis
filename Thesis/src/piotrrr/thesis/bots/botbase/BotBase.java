package piotrrr.thesis.bots.botbase;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.GameObject;
import piotrrr.thesis.common.jobs.Job;
import piotrrr.thesis.gui.AppConfig;
import piotrrr.thesis.gui.MyPopUpDialog;
import piotrrr.thesis.tools.Dbg;
import piotrrr.thesis.tools.Timer;
import soc.qase.bot.NoClipBot;
import soc.qase.bot.ObserverBot;
import soc.qase.bot.PollingBot;
import soc.qase.file.bsp.BSPParser;
import soc.qase.state.PlayerGun;
import soc.qase.state.PlayerMove;
import soc.qase.state.World;
import soc.qase.tools.vecmath.Vector3f;

/**
 * The bot that is used as super class for all the other bots.
 * @author Piotr Gwizdała
 */
public class BotBase extends ObserverBot implements GameObject, UncaughtExceptionHandler {

    /**
     * Stores all the jobs of the bot, that he runs every frame.
     */
    private LinkedList<Job> botJobs = new LinkedList<Job>();
    /**
     * Remembers the number of the first frame from server. Used to measure time.
     */
    private static int firstFrameNumber = -1;
    /**
     * Counts the deaths of the bot.
     */
    private int deathsNumber = 0;
    /**
     * Stores information about the world from QASE
     */
    protected World world;
    /**
     * BSPParser from QASE
     */
    protected BSPParser bsp;
    /**
     * Is used to pause the bot :) debugging purposes.
     */
    public boolean botPaused = false;
    /**
     * Can be used to switch the bot to no-fire mode, but in
     * BotBase is not used. Intended to be used by extending classes.
     */
    public boolean noFire = false;
    /**
     * Can be used to switch the bot to no-movement mode, but in
     * BotBase is not used. Intended to be used by extending classes.
     */
    public boolean noMove = false;
    /**
     * Messages received from the server.
     */
    protected Vector messages = null;
    /**
     * Direction where to look when paused.
     */
    protected Vector3f pausedLookDir = new Vector3f(0, 0, 0);
    /**
     * The last frame that has been perceived by bot. If it is not smaller by 1 from
     * actual frame, it means the bot has lost some frames.
     */
    private int lastWorldFrame = 0;
    /**
     * Whether to shoot at players with the same name prefix as this bot
     */
    public boolean friendlyFire = true;
    /**
     * 
     */
    private int lostFramesCount = 0;
    /**
     * If true, after respawn the bot will try to obtain all weapons using cheats.
     */
    public boolean giveAllOnRespawn = true;
    /**
     * last time used by AI to calculate. In nanoseconds.
     */
    public long lastAIComputingTime = 0l;
    public HashMap<String, Timer> timers = new HashMap<String, Timer>();
    long startTime = System.currentTimeMillis();

    /**
     * Basic constructor
     * @param botName name of the bot to be created.
     * @param skinName the name of the skin to be used with the bot.
     */
    public BotBase(String botName, String skinName) {
        super(botName, skinName);
//        setHighThreadSafety(true);
//        setAutoInventoryRefresh(true);
        setUncaughtExceptionHandler(this);
        timers.put("all-ai", new Timer("all-ai"));
        timers.put("jobs", new Timer("jobs"));
        timers.put("bot-logic", new Timer("bot-logic"));
    }

    @Override
    public void runAI(World world) {
        if (world == null) {
            return;
        }
        try {
            resetTimers();
            timers.get("all-ai").resume();

            if (world.getFrame() != 1 + lastWorldFrame) {
                lostFramesCount++;
            }

            lastWorldFrame = world.getFrame();

            if (firstFrameNumber == -1) {
                if (giveAllOnRespawn) {
                    giveAllWeapons();
                }
                firstFrameNumber = world.getFrame();
            }

            this.world = world;
            //after getting the messages, they may not be availible, so here I
            //get them all at once and save to check on them later.
            messages = world.getMessages();

            timers.get("jobs").resume();
            runBotJobs();
            timers.get("jobs").pause();

            timers.get("bot-logic").resume();
            if (!botPaused) {
                botLogic();
            } else {
                setBotMovement(new Vector3f(), pausedLookDir, PlayerMove.WALK_STOPPED, PlayerMove.POSTURE_NORMAL);
            }
            messages = null;
            timers.get("bot-logic").pause();

            timers.get("all-ai").pause();
            saveTimersString();

        } catch (Exception e) {
            say("Runtime exception!");
            say(e.toString());
            String stack = e.toString();
            int count = 0;
            for (StackTraceElement te : e.getStackTrace()) {
                stack += "\n" + te.toString();
                count++;
                if (count >= 20) {
                    break;
                }
            }
            JOptionPane.showMessageDialog(
                    null,
                    stack,
                    getBotName(),
                    JOptionPane.ERROR_MESSAGE);
            MyPopUpDialog.showMyDialogBox(getBotName(), stack, MyPopUpDialog.error);
            e.printStackTrace();
            disconnect();
        }
    }

    /**
     * In this method all the bot's logic is implemented.
     * Extending classes should override it.
     */
    protected void botLogic() {
    }

    /**
     * Sends the given command as a console command.
     * @param cmd the string of console command.
     */
    public void consoleCommand(String cmd) {
        this.sendConsoleCommand(cmd);
    }

    /**
     * Sends given string as in-game chat message.
     * @param txt the text to be sent.
     */
    public void say(String txt) {
        this.sendConsoleCommand("\"" + txt + "\"");
    }

    /**
     * Runs bot jobs stored in botJobs list.
     */
    private void runBotJobs() {
        for (Job j : botJobs) {
            j.run();
        }
    }

    /**
     * @return current frame number
     */
    public int getFrameNumber() {
        if (world == null) {
            return -1;
        }
        return world.getFrame() - firstFrameNumber;
    }

    /**
     * Adds job to bot's job list.
     * @param j the job.
     * @return true if successful.
     */
    public boolean addBotJob(Job j) {
        return botJobs.add(j);
    }

    /**
     * Returns bot's current health.
     * @return health in float type - number between 0 and 100.
     */
    public float getBotHealth() {
        float h = getHealth();
        if (h < 0) {
            h = 100.0f;
        }
        return h;
    }

    /**
     * Returns bot's armor state.
     * @return The amount of armor that bot has. Between 0 and 100.
     */
    public float getBotArmor() {
        float a = getArmor();
        if (a < 0) {
            a = 0.0f;
        }
        return a;
    }

    /**
     * Get list of guns owned by bot.
     * @return the indexes of the owned guns only if there is also some ammo available.
     */
    public LinkedList<Integer> getIndexesOfOwnedGunsWithAmmo() {
        LinkedList<Integer> ret = new LinkedList<Integer>();
        int[] gunIndexes = {7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
        int[] ammoTable = {0, 0, 0, 0, 0, 0, 0,
            7, //blaster - ammo for blaster is just blaster himself
            18, //shotgun
            18, //ss
            19, //mgun
            19, //chgun
            12, // granades - ammo for granates are granates themselves
            12, //g launcher
            21, //r launcher
            20, //hyperblaster - energy cells
            22, //railgun - slugs
            20 //bfgk - energy cells
        };

        for (int i : gunIndexes) {
            if (hasItem(i) && hasItem(ammoTable[i])) {
                ret.add(i);
            }
        }
        return ret;

    }

    /**
     * Get how much ammo the bot has for given gun
     * @param gunIndex index of the gun in inventory
     * @return the number between 0 and 1 that tells how much ammo we have
     */
    public float getAmmunitionState(int gunIndex) {
        if (world == null) {
            return 0;
        }
        int max = PlayerGun.getMaxAmmoByGun(gunIndex);
        int ammo = world.getInventory().getCount(PlayerGun.getAmmoInventoryIndexByGun(gunIndex));
        if (max == -1) {
            return 1;
        }
        assert (float) ammo / max >= 0.0;
        assert (float) ammo / max <= 1.0;
        return (float) ammo / max;
    }

    @Override
    public void respawn() {
        super.respawn();
        deathsNumber++;

        if (giveAllOnRespawn) {
            giveAllWeapons();
            say("I came back after dieing for " + deathsNumber + " times. Give all!");
        } else {
            say("I came back after dieing for " + deathsNumber + " times.");
        }
    }

    /**
     * Tries to get all the weapons using cheats.
     */
    public void giveAllWeapons() {
        if (world == null) {
            return;
        }

        consoleCommand("give shotgun");
        world.getInventory().setCount(8, 1);

        consoleCommand("give super shotgun");
        world.getInventory().setCount(9, 1);

        consoleCommand("give machinegun");
        world.getInventory().setCount(10, 1);

        consoleCommand("give chaingun");
        world.getInventory().setCount(11, 1);

        consoleCommand("give grenade launcher");
        world.getInventory().setCount(13, 1);

        consoleCommand("give rocket launcher");
        world.getInventory().setCount(14, 1);

        consoleCommand("give hyperblaster");
        world.getInventory().setCount(15, 1);

        consoleCommand("give railgun");
        world.getInventory().setCount(16, 1);

        consoleCommand("give bfg10k");
        world.getInventory().setCount(17, 1);

        
        consoleCommand("give cells 60");
        world.getInventory().setCount(20, 50);

        consoleCommand("give slugs 3");
        world.getInventory().setCount(22, 2);

        consoleCommand("give shells 999");
        world.getInventory().setCount(18, 999);

        consoleCommand("give bullets 200");
        world.getInventory().setCount(19, 300);

        consoleCommand("give rockets 999");
        world.getInventory().setCount(21, 999);

        consoleCommand("give grenades 0");
        world.getInventory().setCount(12, 0);

//        consoleCommand("give armor");



//        consoleCommand("give body armor");

        /**
        BLASTER = 7, SHOTGUN = 8,
        SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
        GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
        RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
        ROCKETS = 21, SLUGS = 22;
         **/
//
//        for (int i = 7; i < 18; i++) {
//            if (i == 16 || i == 17) {
//                continue;
//            }
//            world.getInventory().setCount(i, 1);
//
//        }
//
//        for (int i = 18; i < 23; i++) {
//            if (i==22) continue;
//            world.getInventory().setCount(i, PlayerGun.getMaxAmmo(i) / 2);
//        }



    }

    /**
     * Gets the name of the current server map.
     * @return
     */
    public String getMapName() {
        if (getBsp() == null) {
            say("BSPParser is not availible!");
            return null;
        }
        String mapName = getBsp().getFileName();
        mapName = mapName.substring(mapName.lastIndexOf("/") + 1, mapName.lastIndexOf(".bsp"));
        return mapName;
    }

    /**
     * Used not to call the bsp parser fetching too often and to make sure its not null.
     * Convenience method.
     * @return
     */
    public BSPParser getBsp() {
        if (bsp != null) {
            return bsp;
        }
        bsp = this.getBSPParser();
        assert bsp != null;
        return bsp;
    }

    /**
     * Gets the world's messages that are having given prefix.
     * @return the messages that appeared in the world
     * with given prefix, but without it.
     */
    @SuppressWarnings("unchecked")
    public Vector<String> getMessages(String prefix) {
        Vector<String> ret = new Vector<String>();
        Vector<String> msgs = messages;
        if (msgs == null) {
            return null;
        }
        if (prefix.equals("")) {
            return msgs;
        }
        for (String s : msgs) {
            if (s.startsWith(prefix)) {
                ret.add(s.substring(prefix.length()));
            }
        }
        return ret;
    }

    /**
     * Gets current bot's position
     * @return bot's position as a Vector3f
     */
    public Vector3f getBotPosition() {
        return new Vector3f(world.getPlayer().getPlayerMove().getOrigin());
    }

    /**
     * Returns the world object - public method allows access from the outside.
     * @return
     */
    public World getWorld() {
        return world;
    }

    /**
     * Checks if the bot has given item
     * @param i inventory index of the item we want to check
     * @return true if bot possess the item i
     */
    public boolean botHasItem(int i) {
        return hasItem(i);
    }

    /**
     * Public method that returns the current weapon index the bot uses
     * @return
     */
    public int getCurrentWeaponIndex() {
        return getWeaponIndex();
    }

    /**
     * Public method allowing to change bot's weapon
     * @param i the inventory number of the weapon to change to
     */
    public void changeWeaponToIndex(int i) {
        changeWeaponByInventoryIndex(i);
    }

    /**
     * Get bot's name
     * @return bot's name string.
     */
    public String getBotName() {
        return getPlayerInfo().getName();
    }

    /**
     * Gives a command to the bot. BotBase doesn't support any commands,
     * but this method can be overloaded in extending classes.
     * @param cmd the command the bot should execute.
     */
    public void handleCommand(String cmd) {
    }

    /**
     * Set in which direction the bot should look when paused.
     * @param lookAt the position at which the bot should look.
     */
    public void setPauseLookAtPosition(Vector3f lookAt) {
        pausedLookDir = CommFun.getNormalizedDirectionVector(getBotPosition(), lookAt);
    }

    /**
     * If the cheats are on, the bot will turn on noclip mode, and fly
     * to the given position.
     * @param dst the position where the bot should move.
     */
    public void goToPositionWithNoClipCheating(Vector3f dst) {
//        clipToPosition(dst);
        MyPopUpDialog.showMyDialogBox("Can't do it", "BotBase was changed to inherit from PollingBot. \n" +
                "In order to allow noClip movement, \n" +
                "change inheritance to ObserverBot.", MyPopUpDialog.error);
    }

    @Override
    public String toString() {
        return getBotName();
    }

    @Override
    public String toDetailedString() {
        return toString();
    }

    @Override
    public Vector3f getObjectPosition() {
        return new Vector3f(getPosition());
    }

    /**
     *
     * @return an average computing time consumed by AI in milis.
     */
    public long getLastAIComputingTimeNanos() {
        return lastAIComputingTime;
    }

    public void saveTimersString() {
        String s = "";
        long max = -1;
        for (Timer t : timers.values()) {
            if (t.getElapsedTime() > max) {
                max = t.getElapsedTime();
            }
        }
        for (Timer t : timers.values()) {
            s += t.toStringAsPercentOf(max) + "\n";
//            s+=t.toString()+"\n";
        }
        double div = getFrameNumber() % 100;
        double aitime = timers.get("all-ai").getElapsedTime() / (div * 1000000);
        s += "\ntotal AI time: " + aitime + "ms";
        int fps = (int) ((1000 * getFrameNumber()) / (System.currentTimeMillis() - startTime));
        s += "\nframes per second = " + fps + "\n";
        if (fps != 0) {
            s += "each frame ms = " + 1000 / fps + "\n";
//            s += "max bots num = "+ (1000/fps)/aitime;
            s += "lost frames percent: " + (100 * lostFramesCount / getFrameNumber()) + "%\n";
        }
        timersString = s;
    }
    String timersString = "";

    public String getTimersString() {
        return timersString;
    }

    public void resetTimers() {
        if (getFrameNumber() % 100 != 0) {
            return;
        }
        for (Timer t : timers.values()) {
            t.reset();
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        String stack = e.toString();
        int count = 0;
        for (StackTraceElement te : e.getStackTrace()) {
            stack += "\n" + te.toString();
            count++;
            if (count >= 20) {
                break;
            }
        }
        JOptionPane.showMessageDialog(
                null,
                stack,
                getBotName(),
                JOptionPane.ERROR_MESSAGE);
        MyPopUpDialog.showMyDialogBox(getBotName(), stack, MyPopUpDialog.error);
        e.printStackTrace();
    }
}
