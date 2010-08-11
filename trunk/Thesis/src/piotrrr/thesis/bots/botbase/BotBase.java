package piotrrr.thesis.bots.botbase;

import java.util.LinkedList;
import java.util.Vector;

import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.GameObject;
import piotrrr.thesis.common.jobs.Job;
import soc.qase.bot.NoClipBot;
import soc.qase.file.bsp.BSPParser;
import soc.qase.state.PlayerGun;
import soc.qase.state.PlayerMove;
import soc.qase.state.World;
import soc.qase.tools.vecmath.Vector3f;

/**
 * The bot that is used as super class for all the other bots.
 * @author Piotr Gwizdała
 */
public class BotBase extends NoClipBot implements GameObject {

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
     * The maximum ammount of health it is established the bot has.
     * It is used to calculate the percent of health. In a game, sometimes the bot
     * may have more than 100% of health.
     */
    public static final int maxHealth = 100;
    /**
     * The maximum armor the bot can have.
     * @see BotBase#maxHealth
     */
    public static final int maxArmor = 100;
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
     * If true, after respawn the bot will try to obtain all weapons using cheats.
     */
    public boolean giveAllOnRespawn = true;

    /**
     * Basic constructor
     * @param botName name of the bot to be created.
     * @param skinName the name of the skin to be used with the bot.
     */
    public BotBase(String botName, String skinName) {
        super(botName, skinName);
    }

    @Override
    public void runAI(World world) {
        try {
            if (world.getFrame() != 1 + lastWorldFrame) {
                say("LOST FRAMES: " + (world.getFrame() - lastWorldFrame));
            }
            lastWorldFrame = world.getFrame();

            if (firstFrameNumber == -1) {
                firstFrameNumber = world.getFrame();
            }

            this.world = world;
            //after getting the messages, they may not be availible, so here I
            //get them all at once and save to check on them later.
            messages = world.getMessages();

            runBotJobs();
            
            if (!botPaused) {
                botLogic();
            } else {
                setBotMovement(new Vector3f(), pausedLookDir, PlayerMove.WALK_STOPPED, PlayerMove.POSTURE_NORMAL);
            }
            messages = null;
        } catch (Exception e) {
            say("Runtime exception!");
            say(e.toString());
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
        int max = PlayerGun.getMaxAmmoByGun(gunIndex);
        int ammo = world.getInventory().getCount(gunIndex);
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
        consoleCommand("give shotgun");
        consoleCommand("give super shotgun");
        consoleCommand("give machinegun");
        consoleCommand("give chaingun");
        consoleCommand("give grenade launcher");
        consoleCommand("give rocket launcher");
        consoleCommand("give hyperblaster");
        consoleCommand("give railgun");
        consoleCommand("give bfg10k");
        consoleCommand("give cells");
        consoleCommand("give armor");
        consoleCommand("give body armor");
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
        clipToPosition(dst);
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
}
