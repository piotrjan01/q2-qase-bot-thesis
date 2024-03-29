//--------------------------------------------------
// Name:			BasicBot.java
// Author:			Bernard.Gorman@computing.dcu.ie
//--------------------------------------------------

package soc.qase.bot;

import soc.qase.com.*;
import soc.qase.info.*;
import soc.qase.state.*;
import soc.qase.file.bsp.*;
import soc.qase.file.pak.*;
import soc.qase.tools.Utils;
import soc.qase.ai.waypoint.*;
import soc.qase.tools.vecmath.*;

import java.io.*;
import java.util.Vector;

import java.util.Observable;

/*-------------------------------------------------------------------*/
/**	An abstract class which provides all the base functionality needed
 *	by the game agent. All that is required is to provide a means of
 *	handling server updates, as in the ObserverBot and PollingBot classes.
 *	BasicBot also provides tailored, embedded access to the BSPParser and
 *	WaypointMap classes - methods in this class simply relay calls to the
 *	the BSPParser or Waypoint object as appropriate, with certain parameters
 *	pre-defined to the most useful values from the perspective of game
 *	agents (e.g. the bounding box used to trace through the level is set to
 *	the size of the agent's in-game character's bounding box). Users can
 *	also obtain a pointer to the underlying object, thereby allowing full
 *	access to all their facilities.
 *	@see ObserverBot
 *	@see PollingBot */
/*-------------------------------------------------------------------*/
public abstract class BasicBot extends Thread implements Bot
{

    int rate = 65000;

	private User user = null;
	protected Proxy proxy = null;

	private Angles angles = new Angles(0, 0, 0);
	private Angles delta_Angles = new Angles(0, 0, 0);

	private Velocity velocity = new Velocity(0, 0, 0);
	private Action action = new Action(false, false, false);

	private boolean connected = false;
	private boolean threadSafe = false;
	private boolean traceFromView = false;

	protected boolean ctfTeamAssigned = false;

	private boolean mapNotFound = false;
	private static String q2HomeDir = null;

	protected WaypointMap wpMap = null;
	protected BSPParser bsp = new BSPParser();

	private float sphereRadius = 18.0f;
	private boolean globalAngles = true;
	private static final Vector3f BOUNDING_MAX = new Vector3f(9, 25, 9);
	private static final Vector3f BOUNDING_MIN = new Vector3f(-9, -25, -9);

/*-------------------------------------------------------------------*/
/**	Default constructor. Creates the agent using default parameters. */
/*-------------------------------------------------------------------*/
	public BasicBot()
	{
		user = new User("QASE_Bot", "female/athena", rate, 1, 90, User.HAND_RIGHT, "");
		commonSetup(false, true);
	}

/*-------------------------------------------------------------------*/
/**	Constructor allowing the user to specify a name and skin (appearance)
 *	for the agent.
 *	@param botName name of the character during game session
 *	@param botSkin specifies the character's in-game appearance */
/*-------------------------------------------------------------------*/
	public BasicBot(String botName, String botSkin)
	{
		user = new User((botName == null ? "QASE_BasicBot" : botName), (botSkin == null ? "female/athena" : botSkin), rate, 1, 90, User.HAND_RIGHT, "");
		commonSetup(false, true);
	}

/*-------------------------------------------------------------------*/
/**	Constructor allowing the user to specify a name, skin, and whether
 *	it should manually track its inventory.
 *	@param botName name of the character during game session
 *	@param botSkin specifies the character's in-game appearance
 *	@param trackInv if true, the agent will manually track its inventory */
/*-------------------------------------------------------------------*/
	public BasicBot(String botName, String botSkin, boolean trackInv)
	{
		user = new User((botName == null ? "QASE_BasicBot" : botName), (botSkin == null ? "female/athena" : botSkin), rate, 1, 90, User.HAND_RIGHT, "");
		commonSetup(false, trackInv);
	}

/*-------------------------------------------------------------------*/
/**	Constructor allowing the user to specify a name, skin, whether the
 *	agent should operate in high thread safety mode, and whether it
 *	should manually track its inventory.
 *	@param botName name of the character during game session
 *	@param botSkin specifies the character's in-game appearance
 *	@param highThreadSafety if true, enables high thread safety mode
 *	@param trackInv if true, the agent will manually track its inventory */
/*-------------------------------------------------------------------*/
	public BasicBot(String botName, String botSkin, boolean highThreadSafety, boolean trackInv)
	{
		user = new User((botName == null ? "QASE_BasicBot" : botName), (botSkin == null ? "female/athena" : botSkin), rate, 1, 90, User.HAND_RIGHT, "");
		commonSetup(highThreadSafety, trackInv);
	}

/*-------------------------------------------------------------------*/
/**	Constructor allowing the user to specify a name, skin, server password,
 *	whether the agent should operate in high thread safety mode, and whether
 *	it should manually track its inventory.
 *	@param botName name of the character during game session
 *	@param botSkin specifies the character's in-game appearance
 *	@param password the password of the server, if necessary
 *	@param highThreadSafety if true, enables high thread safety mode
 *	@param trackInv if true, the agent will manually track its inventory */
/*-------------------------------------------------------------------*/
	public BasicBot(String botName, String botSkin, String password, boolean highThreadSafety, boolean trackInv)
	{
		user = new User((botName == null ? "QASE_BasicBot" : botName), (botSkin == null ? "female/athena" : botSkin), rate, 1, 90, User.HAND_RIGHT, password);
		commonSetup(highThreadSafety, trackInv);
	}

/*-------------------------------------------------------------------*/
/**	Constructor allowing the user to specify a name, skin, connection
 *	receive rate, type of messages received from server, field of view, 
 *	which hand the agent should hold its gun in, server password,
 *	whether the agent should operate in high thread safety mode, and whether
 *	it should manually track its inventory.
 *	@param botName name of the character during game session
 *	@param botSkin specifies the character's in-game appearance
 *	@param recvRate rate at which the client communicates with server
 *	@param msgLevel specifies which server messages to register interest in
 *	@param fov specifies the agent's field of vision
 *	@param hand specifies the hand in which the agent hold its gun
 *	@param password the password of the server, if necessary
 *	@param highThreadSafety if true, enables high thread safety mode
 *	@param trackInv if true, the agent will manually track its inventory */
/*-------------------------------------------------------------------*/
	public BasicBot(String botName, String botSkin, int recvRate, int msgLevel, int fov, int hand, String password, boolean highThreadSafety, boolean trackInv)
	{
		user = new User((botName == null ? "QASE_BasicBot" : botName), (botSkin == null ? "female/athena" : botSkin), (recvRate < 0 ? rate : recvRate), (msgLevel < 0 ? 1 : msgLevel), (fov < 0 ? 90 : fov), (hand < 0 ? User.HAND_RIGHT : hand), (password == null ? "" : password));
		commonSetup(highThreadSafety, trackInv);
	}

	private void commonSetup(boolean highThreadSafety, boolean trackInv)
	{
		threadSafe = highThreadSafety;
		proxy = new Proxy(user, highThreadSafety, trackInv);
	}

// all abstract methods to be supplied by derived classes
// custom bots should synchronize on World if high thread safety is enabled
/*-------------------------------------------------------------------*/
/**	Connect to a game server. To be implemented by derived classes.
 *	@param host a String representation of the host machine's IP address
 *	@param port the port on which the game server is running */
/*-------------------------------------------------------------------*/
	public abstract boolean connect(String host, int port);

/*-------------------------------------------------------------------*/
/**	Connect to a CTF game server. To be implemented by derived classes.
 *	@param host a String representation of the host machine's IP address
 *	@param port the port on which the game server is running
 *	@param ctfTeam the team to join; one of the CTF constants found in
 *	soc.qase.info.Server */
/*-------------------------------------------------------------------*/
	public abstract boolean connect(String host, int port, int ctfTeam);

/*-------------------------------------------------------------------*/
/**	Disconnect from the server. To be implemented by derived classes.*/
/*-------------------------------------------------------------------*/
	public abstract void disconnect();

/*-------------------------------------------------------------------*/
/**	The core AI routine. To be implemented by derived classes.
 *	@param world a World object representing the current gamestate */
/*-------------------------------------------------------------------*/
	public abstract void runAI(World world);

/*-------------------------------------------------------------------*/
/**	Flag the agent as being connected to or disconnected from the game server.
 *	@param value true if the agent is connected, false otherwise */
/*-------------------------------------------------------------------*/
	protected void setConnected(boolean value)
	{
		connected = value;
	}

/*-------------------------------------------------------------------*/
/**	Check whether the agent is connected to the server.
 *	@return value true if the agent is connected, false otherwise */
/*-------------------------------------------------------------------*/
	public boolean isConnected()
	{
		return connected;
	}

/*-------------------------------------------------------------------*/
/**	Set the agent to operate in low or high thread safety mode. In
 *	high thread safety mode, the gamestate object is locked while
 *	QASE is reading from it, preventing errors which may occur due to
 *	updates arriving in the middle of the AI cycle. Since BasicBot does
 *	not implement a server-handling mechanism, it is the responsibility
 *	of derived classes to correctly observe this contract (as is the case
 *	with ObserverBot and PollingBot). Note that, because the Proxy object
 *	is implicitly locked while updating all registered ObserverBots, high
 *	thread safety is not required except in cases where external threads
 *	may intervene in mid-AI cycle.
 *	@param highThreadSafety true to enable high safety mode, false to disable
 *	@see ObserverBot
 *	@see PollingBot*/
/*-------------------------------------------------------------------*/
	public void setHighThreadSafety(boolean highThreadSafety)
	{
		threadSafe = highThreadSafety;
		proxy.setHighThreadSafety(highThreadSafety);
	}

/*-------------------------------------------------------------------*/
/**	Check whether the agent is operating in high thread-safety mode.
 *	@return true if the agent is in high safety mode, false otherwise */
/*-------------------------------------------------------------------*/
	public boolean getHighThreadSafety()
	{
		return threadSafe;
	}

/*-------------------------------------------------------------------*/
/**	Specifies whether the agent should automatically request a full
 *	listing of its inventory on each frame. This can be used in place
 *	of manual inventory tracking - it ensures complete accuracy, at
 *	the cost of increasing the amount of network traffic per update.
 *	Also note that recorded DM2 files of agents using this approach
 *	will display an inventory window every second frame.
 *	@param refresh turn auto inventory refresh on/off */
/*-------------------------------------------------------------------*/
	public void setAutoInventoryRefresh(boolean refresh)
	{
		proxy.setAutoInventoryRefresh(refresh);
	}

/*-------------------------------------------------------------------*/
/**	Returns the User object associated with this bot, which contains
 *	information regarding the agent's in-game name, skin, FoV, etc.
 *	@return the User object containing details of the bot's
 *	configuration, or null if no such object exists */
/*-------------------------------------------------------------------*/
	protected User getPlayerInfo()
	{
		return user;
	}

/*-------------------------------------------------------------------*/
/**	Returns the Player object associated with this bot, which can then
 *	be further queried for information.
 *	@return the Player object containing full details of the bot's
 *	current state, or null if no such object exists */
/*-------------------------------------------------------------------*/
	protected Player getPlayerState()
	{
		return ((proxy == null || proxy.getWorld() == null) ? null : proxy.getWorld().getPlayer());
	}

/*-------------------------------------------------------------------*/
/**	Returns the Server object associated with the current game session,
 *	which contains information such as the server version, the map name,
 *	and whether the server is running CTF or a regular deathmatch.
 *	@return the Server object associated with the current session */
/*-------------------------------------------------------------------*/
	protected Server getServerInfo()
	{
		return (proxy == null ? null : proxy.getServer());
	}

/*-------------------------------------------------------------------*/
/**	Check whether the agent is currently alive and active in the game.
 *	@return true if the agent is active and alive, false otherwise */
/*-------------------------------------------------------------------*/
	protected boolean isBotAlive()
	{
		return proxy != null && proxy.inGame() && proxy.getWorld().getPlayer().isAlive();
	}

/*-------------------------------------------------------------------*/
/**	Get the agent's current position.
 *	@return the current position, or null if the agent is not
 *	currently connected */
/*-------------------------------------------------------------------*/
	protected Origin getPosition()
	{
		return ((proxy == null || proxy.getWorld() == null) ? null : proxy.getWorld().getPlayer().getPlayerMove().getOrigin());
	}

/*-------------------------------------------------------------------*/
/**	Get the agent's current orientation.
 *	@return the current orientation, or null if the agent is not
 *	currently connected */
/*-------------------------------------------------------------------*/
	protected Angles getOrientation()
	{
		return ((proxy == null || proxy.getWorld() == null) ? null : proxy.getWorld().getPlayer().getPlayerView().getViewAngles());
	}

/*-------------------------------------------------------------------*/
/**	Get the agent's current health.
 *	@return the agent's health value, or Integer.MIN_VALUE if the agent
 *	is not currently connected */
/*-------------------------------------------------------------------*/
	protected int getHealth()
	{
		return ((proxy != null && proxy.inGame()) ? proxy.getWorld().getPlayer().getPlayerStatus().getStatus(PlayerStatus.HEALTH) : Integer.MIN_VALUE);
	}

/*-------------------------------------------------------------------*/
/**	Get the inventory index of the current weapon.
 *	@return the index of the current weapon as indicated by the constants
 *	in the Inventory class, or Integer.MIN_VALUE if the agent is not
 *	currently connected */
/*-------------------------------------------------------------------*/
	protected int getWeaponIndex()
	{
		return ((proxy != null && proxy.inGame()) ? proxy.getWorld().getPlayer().getPlayerGun().getInventoryIndex() : Integer.MIN_VALUE);
	}

/*-------------------------------------------------------------------*/
/**	Get the amount of ammo the agent has for the current weapon.
 *	@return the current ammo, or Integer.MIN_VALUE if the agent
 *	is not currently connected */
/*-------------------------------------------------------------------*/
	protected int getAmmo()
	{
		return ((proxy != null && proxy.inGame()) ? proxy.getWorld().getPlayer().getPlayerStatus().getStatus(PlayerStatus.AMMO) : Integer.MIN_VALUE);
	}

/*-------------------------------------------------------------------*/
/**	Get the amount of armor currently held by the agent.
 *	@return the current armor value, or Integer.MIN_VALUE if the agent
 *	is not currently connected */
/*-------------------------------------------------------------------*/
	protected int getArmor()
	{
		return ((proxy != null && proxy.inGame()) ? proxy.getWorld().getPlayer().getPlayerStatus().getStatus(PlayerStatus.ARMOR) : Integer.MIN_VALUE);
	}

/*-------------------------------------------------------------------*/
/**	Switch the agent to a specified team during a CTF match (assuming in-game
 *	team switching is enabled).
 *	@param ctfTeam the team to join; one of the CTF constants found in
 *	soc.qase.info.Server */
/*-------------------------------------------------------------------*/
	protected void setCTFTeam(int ctfTeam)
	{
		ctfTeamAssigned = true;
		sendConsoleCommand("team " + Server.CTF_STRINGS[(Math.abs(ctfTeam) < 2 ? Math.abs(ctfTeam) : (int)Math.round(Math.random()))]);
	}

/*-------------------------------------------------------------------*/
/**	Resolve the CTF team number of the local agent, if the current server
 *	is running the CTF mod.
 *	@return the team number of the local agent; 0 = RED, 1 = BLUE */
/*-------------------------------------------------------------------*/
	protected int getCTFTeamNumber()
	{
		return (proxy == null ? Integer.MIN_VALUE : proxy.getCTFTeamNumber());
	}

/*-------------------------------------------------------------------*/
/**	Resolve the CTF team name of the local agent, if the current server
 *	is running the CTF mod.
 *	@return the team name of the local agent; either RED, BLUE or null
 *	if the agent is not currently on a team. */
/*-------------------------------------------------------------------*/
	protected String getCTFTeamString()
	{
		return (proxy == null ? null : proxy.getCTFTeamString());
	}

/*-------------------------------------------------------------------*/
/**	Determined whether the given player Entity is on the same CTF team
 *	as the local agent.
 *	@param otherPlayer the Entity representing the player to be checked
 *	@return true if otherPlayer is on the same CTF team as the local agent,
 *	false otherwise */
/*-------------------------------------------------------------------*/
	protected boolean isOnSameCTFTeam(Entity otherPlayer)
	{
		return (getCTFTeamNumber() >= 0 && getCTFTeamNumber() == otherPlayer.getCTFTeamNumber());
	}

/*-------------------------------------------------------------------*/
/**	Get the specified angle, as defined by the constants in the
 *	Angles class.
 *	@param angleType the angle to return
 *	@see soc.qase.state.Angles */
/*-------------------------------------------------------------------*/
	protected float getAngle(int angleType)
	{
		return angles.get(angleType);
	}

/*-------------------------------------------------------------------*/
/**	Set the agent's angles in each plane.
 *	@param yaw the new yaw angle
 *	@param pitch the new pitch angle
 *	@param roll the new roll angle */
/*-------------------------------------------------------------------*/
	protected void setAngle(float yaw, float pitch, float roll)
	{
		angles.set(yaw, pitch, roll);
	}

/*-------------------------------------------------------------------*/
/**	Set the agent's angles in the given plane, as defined by the constants
 *	in the Angles class.
 *	@param angleType the angle to change
 *	@param value the new angle to set
 *	@see soc.qase.state.Angles */
/*-------------------------------------------------------------------*/
	protected void setAngle(int angleType, float value)
	{
		angles.set(angleType, value);
	}

/*-------------------------------------------------------------------*/
/**	Get the current velocity in the given direction, as specified by
 *	the constants in the Velocity class.
 *	@param velocityType the direction (forward/right) whose magnitude
 *	is required
 *	@return the magnitude of the agent's velocity in the given direction
 *	@see soc.qase.state.Velocity */
/*-------------------------------------------------------------------*/
	protected int getVelocity(int velocityType)
	{
		return velocity.get(velocityType);
	}

/*-------------------------------------------------------------------*/
/**	Get the current walk state of the bot. This will be one of WALK_STOPPED,
 *	WALK_NORMAL or WALK_RUN, as specified in the PlayerMove class.
 *	@return the agent's current walk state
 *	@see soc.qase.state.PlayerMove */
/*-------------------------------------------------------------------*/
	protected int getWalkState()
	{
		return getPlayerState().getWalkState();
	}

/*-------------------------------------------------------------------*/
/**	Set the agent's velocity in each direction.
 *	@param forward the new forward velocity
 *	@param right the new right velocity
 *	@param up the new vertical velocity */
/*-------------------------------------------------------------------*/
	protected void setVelocity(int forward, int right, int up)
	{
		velocity.set(forward, right, up);
	}

/*-------------------------------------------------------------------*/
/**	Set the agent's velocity in a given direction, as given by the
 *	constants in the Velocity class.
 *	@param velocityType the direction whose velocity is to be set
 *	@param value the magnitude of the new velocity
 *	@see soc.qase.state.Velocity */
/*-------------------------------------------------------------------*/
	protected void setVelocity(int velocityType, int value)
	{
		velocity.set(velocityType, value);
	}

/*-------------------------------------------------------------------*/
/**	Get the agent's current action settings, as specified by the constants
 *	in the Action class.
 *	@param actionType the action to return
 *	@return true if the given action is active, false otherwise
 *	@see soc.qase.state.Action */
/*-------------------------------------------------------------------*/
	protected boolean getAction(int actionType)
	{
		return action.get(actionType);
	}

/*-------------------------------------------------------------------*/
/**	Set the agent's current actions.
 *	@param attack the action represents an attack
 *	@param use the action represents usage of current item
 *	@param any the action represents any button press
 *	@see soc.qase.state.Action */
/*-------------------------------------------------------------------*/
	protected void setAction(boolean attack, boolean use, boolean any)
	{
		action.set(attack, use, any);
	}

/*-------------------------------------------------------------------*/
/**	Set one of the agent's actions, as specified by the constants in
 *	the Action class.
 *	@param actionType the action to return
 *	@param value true if the given action should be active, false otherwise 
 *	@see soc.qase.state.Action */
/*-------------------------------------------------------------------*/
	protected void setAction(int actionType, boolean value)
	{
		action.set(actionType, value);
	}

/*-------------------------------------------------------------------*/
/**	Specify whether the agent should fire its weapon on the next frame.
 *	@param attack true if the agent should fire, false otherwise
 *	@see soc.qase.state.Action */
/*-------------------------------------------------------------------*/
	protected void setAttack(boolean attack)
	{
		action.setAttack(attack);
	}

/*-------------------------------------------------------------------*/
/**	Specify whether the agent should use the current item
 *	@param use true if the agent should use the item, false otherwise
 *	@see soc.qase.state.Action */
/*-------------------------------------------------------------------*/
	protected void setUse(boolean use)
	{
		action.setUse(use);
	}

/*-------------------------------------------------------------------*/
/**	Set the agent's posture to POSTURE_CROUCH, POSTURE_STAND or
 *	POSTURE_JUMP; these constants are found in the PlayerMove class.
 *	@param postureState specifies the bot's posture (crouch/stand/jump)
 *	@see soc.qase.state.PlayerMove */
/*-------------------------------------------------------------------*/
	protected void setPosture(int postureState)
	{
		velocity.setUp(postureState * 300);
	}

/*-------------------------------------------------------------------*/
/**	Specify whether the bot should jump. A value of 'true' causes the
 *	bot to jump, while 'false' returns it to a normal standing posture.
 *	@param jump indicates whether or not the agent should jump */
/*-------------------------------------------------------------------*/
	protected void setJump(boolean jump)
	{
		velocity.setUp(jump ? 300 : 0);
	}

/*-------------------------------------------------------------------*/
/**	Specify whether the bot should crouch. A value of 'true' causes the
 *	bot to crouch, while 'false' returns it to a normal standing posture.
 *	@param crouch indicates whether or not the agent should crouch */
/*-------------------------------------------------------------------*/
	protected void setCrouch(boolean crouch)
	{
		velocity.setUp(crouch ? -300 : 0);
	}

/*-------------------------------------------------------------------*/
/**	Check whether agent is currently jumping.
 *	@return true if jumping, false otherwise */
/*-------------------------------------------------------------------*/
	protected boolean isJumping()
	{
		return proxy != null && proxy.inGame() && proxy.getWorld().getPlayer().isJumping();
	}

/*-------------------------------------------------------------------*/
/**	Check whether agent is currently crouching.
 *	@return true if crouching, false otherwise */
/*-------------------------------------------------------------------*/
	protected boolean isCrouching()
	{
		return proxy != null && proxy.inGame() && proxy.getWorld().getPlayer().isCrouching();
	}

/*-------------------------------------------------------------------*/
/**	Get the agent's current posture, as one of the POSTURE constants
 *	in the PlayerMove class (POSTURE_CROUCH = -1, POSTURE_NORMAL = 0,
 *	POSTURE_JUMP = 1;).
 *	@return one of the POSTURE constants in PlayerMove */
/*-------------------------------------------------------------------*/
	protected int getPosture()
	{
		return  (proxy != null && proxy.inGame() ? proxy.getWorld().getPlayer().getPosture() : Integer.MIN_VALUE);
	}

/*-------------------------------------------------------------------*/
/**	Check whether agent is currently in water.
 *	@return true if in water, false otherwise */
/*-------------------------------------------------------------------*/
	protected boolean isUnderWater()
	{
		return proxy != null && proxy.inGame() && proxy.getWorld().getPlayer().isUnderWater();
	}

/*-------------------------------------------------------------------*/
/**	Determines the amount of time remaining until the player begins
 *	drowning, or Long.MIN_VALUE if the player is not underwater. If
 *	the player has a Rebreather or Environment Suit active - meaning
 *	that they can breathe underwater - the value returned will indicate
 *	the total amount of time until drowning, taking the active item
 *	into account.
 *	@return the time remaining until the player starts to drown in
 *	milliseconds, or Long.MIN_VALUE if the player is not underwater */
/*-------------------------------------------------------------------*/
	protected long timeUntilDrowning()
	{
		return (proxy != null && proxy.inGame() ? proxy.getWorld().getPlayer().timeUntilDrowning() : Long.MIN_VALUE);
	}

/*-------------------------------------------------------------------*/
/**	Checks whether the agent is currently drowning.
 *	@return true if the player is drowning, false otherwise. */
/*-------------------------------------------------------------------*/
	protected boolean isDrowning()
	{
		return proxy != null && proxy.inGame() && proxy.getWorld().getPlayer().isDrowning();
	}

/*-------------------------------------------------------------------*/
/**	Reactivate the agent in the game world upon its death. */
/*-------------------------------------------------------------------*/
	protected void respawn()
	{
		angles.setYaw(0);
		angles.setRoll(0);
		angles.setPitch(0);

		velocity.setForward(0);
		velocity.setRight(0);

		action.setAttack(true);

		while(!isBotAlive())
		{
			proxy.sendMovement(angles, velocity, action);

			try
			{
				Thread.sleep(10);
			}
			catch(InterruptedException ie)
			{	}
		}

		pacify();
	}

/*-------------------------------------------------------------------*/
/**	Stop all agent activities. */
/*-------------------------------------------------------------------*/
	protected void pacify()
	{
		action.setAny(false);
		action.setUse(false);
		action.setAttack(false);

		velocity.setRight(0);
		velocity.setForward(0);

		angles.setYaw(0);
		angles.setRoll(0);
		angles.setPitch(0);

		proxy.sendMovement(angles, velocity, action);
	}

	private float[] botAngles = new float[2];
	private Vector2f perp = new Vector2f(0, 0);
	private Vector2f moveDir2f = new Vector2f(0, 0), aimDir2f = new Vector2f(0, 0);

/*-------------------------------------------------------------------*/
/**	Convenience method to facilitate the separation of movement and
 *	aiming, and allow both to be specified in global co-ordinates. Also
 *	allows the programmer to specify the bot's 'posture' - that is, whether
 *	it is standing, crouching or jumping. The postureState parameter should
 *	be one of the POSTURE_CROUCH, POSTURE_NORMAL or POSTURE_JUMP constants
 *	from the PlayerMove class.
 *	@param moveDir the direction in which to move
 *	@param aimDir the direction in which to aim
 *	@param vel the agent's total velocity
 *	@param postureState indicates the bot's posture (crouch/stand/jump)
 *	@see soc.qase.state.PlayerMove */
/*-------------------------------------------------------------------*/
	protected void setBotMovement(Vector3f moveDir, Vector3f aimDir, float vel, int postureState)
	{
		if(moveDir == null && aimDir == null)
			return;
		else if(moveDir == null)
			moveDir = new Vector3f(aimDir);
		else if(aimDir == null)
			aimDir = new Vector3f(moveDir);

		moveDir2f.set(moveDir.x, moveDir.y);
		aimDir2f.set(aimDir.x, aimDir.y);

		if(vel < 0)
			vel = (int)moveDir2f.length();

		moveDir2f.normalize();
		aimDir2f.normalize();

		moveDir2f.scale(vel);
		perp.set(aimDir2f.y, -aimDir2f.x);

		velocity.setForward((int)Math.round(aimDir2f.x * moveDir2f.x + aimDir2f.y * moveDir2f.y));
		velocity.setRight((int)Math.round(perp.x * moveDir2f.x + perp.y * moveDir2f.y));
		setPosture(postureState);

		botAngles = Utils.calcAngles(aimDir);

		angles.setYaw(botAngles[0]);
		angles.setPitch(botAngles[1]);
	}

/*-------------------------------------------------------------------*/
/**	Convenience method to facilitate the separation of movement and
 *	aiming, and allow both to be specified in global co-ordinates. Also
 *	allows the programmer to specify the bot's 'posture' - that is, whether
 *	it is standing, crouching or jumping. The postureState parameter should
 *	be one of the POSTURE_CROUCH, POSTURE_NORMAL or POSTURE_JUMP constants
 *	from the PlayerMove class.<br>
 *	<br>
 *	Instead of providing an explicit velocity as above, the programmer
 *	specifies the current walk state (WALK_STOPPED, WALK_NORMAL, WALK_RUN)
 *	using the constants defined in the PlayerMove class. The method computes
 *	the correct velocity based on whether the agent is on land or submerged,
 *	and then passes the call to setBotMovement(Vector3f, Vector3f, float).
 *	To account for the possibility of the programmer passing
 *	an explicit velocity as an int rather than float, the call will be
 *	passed directly to the above method if walkState exceeds the range
 *	of the constant values (i.e. 0, 1, 2).
 *	@param moveDir the direction in which to move
 *	@param aimDir the direction in which to aim
 *	@param walkState the discrete movement speed to use
 *	@param postureState indicates the bot's posture (crouch/stand/jump)
 *	@see soc.qase.state.PlayerMove */
/*-------------------------------------------------------------------*/
	protected void setBotMovement(Vector3f moveDir, Vector3f aimDir, int walkState, int postureState)
	{
		float vel = 0f; // assume stopped

		if(walkState == 0 || walkState > 2)
			vel = (float)walkState;
		else if(proxy.getWorld().getPlayer().isUnderWater()) // in water
			vel = (walkState == 1 ? 110f : 300f);
		else // moving, on land
			vel = (walkState == 1 ? 200f : 300f);

		setBotMovement(moveDir, aimDir, vel, postureState);
	}

/*-------------------------------------------------------------------*/
/**	Instruct the agent to use a global or local co-ordinate system. In
 *	Quake 2, the agent operates using a set of local axes that are rotated
 *	relative to the global co-ordinate system depending on the player's
 *	orientation when spawning. Generally, programmers are advised to work
 *	in global co-ordinates (the default setting), but may occasionally need
 *	or want to work using the local axes instead.
 *	@param use true if the agent should use global co-ordinates, false
 *	if it should use local */
/*-------------------------------------------------------------------*/
	public void useGlobalAngles(boolean use)
	{
		globalAngles = use;
	}

/*-------------------------------------------------------------------*/
/**	Check whether the agent is using a global or local co-ordinate system.
 *	@return true if the agent is using global co-ordinates, false otherwise */
/*-------------------------------------------------------------------*/
	public boolean getUseGlobalAngles()
	{
		return globalAngles;
	}

/*-------------------------------------------------------------------*/
/**	Activate the agent's hand grenades. */
/*-------------------------------------------------------------------*/
	protected void activateGrenades()
	{
		useItem(PlayerGun.GRENADES);
	}

/*-------------------------------------------------------------------*/
/**	Change the active weapon. For use with the constants stored in the
 *	PlayerGun class.
 *	@param gun the index of the gun to activate
 *	@see soc.qase.state.PlayerGun */
/*-------------------------------------------------------------------*/
	protected void changeWeapon(int gun)
	{
		changeWeaponByInventoryIndex(gun);
	}

/*-------------------------------------------------------------------*/
/**	Change the current weapon by specifying a number from 0-9. The
 *	argument should be one of the KEY_WEAPON constants defined in the
 *	User class.
 *	@param gun the keyboard index of the weapon to activate */
/*-------------------------------------------------------------------*/
	protected void changeWeaponByKeyboardIndex(int gun)
	{
		useItem((gun >= 6 ? gun + 7 : (gun > 0 ? gun + 6 : 17)));
	}

/*-------------------------------------------------------------------*/
/**	Change the current weapon by specifying an inventory index.
 *	Typically uses the constants from the Inventory class.
 *	@param gun the inventory index of the weapon to activate
 *	@see soc.qase.state.Inventory */
/*-------------------------------------------------------------------*/
	protected void changeWeaponByInventoryIndex(int gun)
	{
		useItem(gun);
	}

/*-------------------------------------------------------------------*/
/**	Change the current weapon by specifying the index of the associated
 *	weapon model in the Model subsection of the Config table, i.e. the
 *	value returned by calling playerGun.getIndex().
 *	@param gun the index of the weapon to activate
 *	@see soc.qase.info.Config */
/*-------------------------------------------------------------------*/
	protected void changeWeaponByGunModelIndex(int gun)
	{
		useItem(PlayerGun.getGunInventoryIndex(gun));
	}

/*-------------------------------------------------------------------*/
/**	Use the specified item. Items are specified by inventory index,
 *	using the constants from the Inventory class.
 *	@param item the inventory index of the weapon to activate
 *	@see soc.qase.state.Inventory */
/*-------------------------------------------------------------------*/
	protected void useItem(int item)
	{
		if (proxy != null && proxy.inGame())
			proxy.useItem(item);
	}

/*-------------------------------------------------------------------*/
/**	Checks whether or not a particular timed buff (invulnerability,
 *	quad damage, environment suit, etc) is currently active on the
 *	bot; if so, returns the time remaining until the buff expires.
 *	If null is passed, then the time remaining for any active buff is
 *	returned. If no such buff is active, Integer.MIN_VALUE is returned.
 *	The string passed to this method should be one of the appropriate
 *	ICON_XYZ constants from the PlayerStatus class, e.g. ICON_INVULNERABILITY,
 *	ICON_ENVIRONMENT_SUIT, ICON_QUAD_DAMAGE; this is because  the buff
 *	is checked by examining the contents of the TIMER_ICON field in
 *	the player's status array. Note that the time is a floored integer
 *	representation of the real value - that is, a 30-second buff will
 *	begin at 29 and end with 0 being returned for the final second it
 *	is active.
 *	@param buffIcon icon string of a particular timed buff, or null to check for
 *	any buff
 *	@return the time remaining on the active buff, in seconds; or
 *	Integer.MIN_VALUE if no such buff is found */
/*-------------------------------------------------------------------*/
	protected int checkTimedBuff(String buffIcon)
	{
		return (proxy != null && proxy.inGame() ? proxy.getWorld().getPlayer().getPlayerStatus().checkTimedBuff(buffIcon) : Integer.MIN_VALUE);
	}

/*-------------------------------------------------------------------*/
/**	Request a full copy of the agent's current inventory. Used when
 *	auto inventory refresh is enabled.*/
/*-------------------------------------------------------------------*/
	protected void refreshInventory()
	{
		proxy.refreshInventory();
	}

/*-------------------------------------------------------------------*/
/**	Get count for specified inventory item.
 *	@param itemIndex item index. Should be one of the constants defined
 *	in the Inventory class.
 *	@return item index count. */
/*-------------------------------------------------------------------*/
	protected int getInventoryItemCount(int itemIndex)
	{
		World world;
		Inventory inv;

		if(proxy == null || (world = proxy.getWorld()) == null || (inv = world.getInventory()) == null)
			return -1;

		return inv.getCount(itemIndex);
	}

/*-------------------------------------------------------------------*/
/**	Get count for specified range of inventory items.
 *	@param startItemIndex item index at which to start. Should be one of
 *	the constants defined in the Inventory class.
 *	@param endItemIndex item index at which to end, inclusive. Should be
 *	one of the constants defined in the Inventory class.
 *	@return an array giving a count of each item in the range, or
 *	null if the inventory does not exist. */
/*-------------------------------------------------------------------*/
	protected int[] getInventoryItemCount(int startItemIndex, int endItemIndex)
	{
		World world;
		Inventory inv;

		if(proxy == null || (world = proxy.getWorld()) == null || (inv = world.getInventory()) == null)
			return null;

		return inv.getCount(startItemIndex, endItemIndex);
	}

/*-------------------------------------------------------------------*/
/**	Get count for specified inventory item. Note that this string-matching
 *	method is somewhat slower that the index methods above.
 *	@param item plain english string of the item as seen in-game, e.g.
 *	rocket launcher, hyperblaster, chaingun.
 *	@return item count. */
/*-------------------------------------------------------------------*/
	protected int getInventoryItemCount(String item)
	{
		World world;
		Inventory inv;

		if(proxy == null || (world = proxy.getWorld()) == null || (inv = world.getInventory()) == null)
			return -1;

		return inv.getCount(item);
	}

/*-------------------------------------------------------------------*/
/**	Determines whether the agent is in possession of the specified item.
 *	@param itemIndex item index. Should be one of the constants defined
 *	in the Inventory class.
 *	@return true if the agents possesses one or more of the specified
 *	item, false otherwise. */
/*-------------------------------------------------------------------*/
	protected boolean hasItem(int itemIndex)
	{
		return getInventoryItemCount(itemIndex) > 0;
	}

/*-------------------------------------------------------------------*/
/**	Determines whether the agent is in possession of the specified item.
 *	Note that this string-matching method is somewhat slower that the
 *	index method above.
 *	@param item plain english string of the item as seen in-game, e.g.
 *	rocket launcher, hyperblaster, chaingun.
 *	@return true if the agents possesses one or more of the specified
 *	item, false otherwise. */
/*-------------------------------------------------------------------*/
	protected boolean hasItem(String item)
	{
		return getInventoryItemCount(item) > 0;
	}

/*-------------------------------------------------------------------*/
/**	Send a non-blocking console message to connected host.
 *	@param command message to send */
/*-------------------------------------------------------------------*/
	protected void sendConsoleCommand(String command)
	{
		if(proxy != null && proxy.inGame())
			proxy.sendConsoleCommand(command);
	}

/*-------------------------------------------------------------------*/
/**	Find the nearest entity in the game world. Returns only entities
 *	which are currently active.
 *	@return nearest entity */
/*-------------------------------------------------------------------*/
	protected Entity getNearestEntity()
	{
		if(proxy == null || proxy.getWorld() == null)
			return null;

		return getNearestEntity(proxy.getWorld().getEntities());
	}

/*-------------------------------------------------------------------*/
/**	Find the nearest entity in the game world. Returns only entities
 *	which are currently active.
 *	@param cat the category of entity to search for. Should be one of
 *	the CAT_ constants in the Entity class, or null to search for all
 *	entity categories.
 *	@param type the type of entity to search for. Should be one of the
 *	TYPE_ constants in the Entity class, or null to search for all
 *	entity types.
 *	@param subType the subtype of entity to search for. Should be one
 *	of the SUBTYPE_ constants in the Entity class, or null to search
 *	for all entity subtypes.
 *	@return nearest entity */
/*-------------------------------------------------------------------*/
	protected Entity getNearestEntity(String cat, String type, String subType)
	{
		if(proxy == null || proxy.getWorld() == null)
			return null;

		return getNearestEntity(proxy.getWorld().getEntities(cat, type, subType, true));
	}

/*-------------------------------------------------------------------*/
/**	Find the nearest entity in the game world from those contained in
 *	the argument Vector.
 *	@param filteredEntities a Vector containing a selection of entities
 *	from the gamestate.
 *	@return nearest entity */
/*-------------------------------------------------------------------*/
	protected Entity getNearestEntity(Vector filteredEntities)
	{
		Entity tempEntity = null;
		Entity nearestEnemy = null;

		Origin currentPos = getPosition();

		float tempDistance = 0;
		float distance = Float.MAX_VALUE;

		for(int i = 0; i < filteredEntities.size(); i++)
		{
			tempEntity = (Entity)filteredEntities.elementAt(i);
			tempDistance = currentPos.distance(tempEntity.getOrigin());

			if(tempDistance < distance && tempDistance > 0)
			{
				distance = tempDistance;
				nearestEnemy = tempEntity;
			}
		}

		return nearestEnemy;
	}

/*-------------------------------------------------------------------*/
/**	Find the nearest enemy in the game world. Returns only entities
 *	which are currently active.
 *	@return nearest enemy */
/*-------------------------------------------------------------------*/
	protected Entity getNearestOpponent()
	{
		if(proxy == null || proxy.getWorld() == null)
			return null;

		return getNearestEntity(proxy.getWorld().getOpponents());
	}

/*-------------------------------------------------------------------*/
/**	Find the nearest item in the game world. Returns only entities
 *	which are currently active.
 *	@param type the type of item to search for. Should be one of the
 *	TYPE_ constants in the Entity class, or null to search for all
 *	item types.
 *	@param subType the subtype of item to search for. Should be one of
 *	the SUBTYPE_ constants in the Entity class, or null to search for all
 *	item subtypes.
 *	@return nearest item */
/*-------------------------------------------------------------------*/
	protected Entity getNearestItem(String type, String subType)
	{
		if(proxy == null || proxy.getWorld() == null)
			return null;

		return getNearestEntity(proxy.getWorld().getEntities(Entity.CAT_ITEMS, type, subType, true));
	}

/*-------------------------------------------------------------------*/
/**	Find the nearest item in the game world. Returns only entities
 *	which are currently active.
 *	@param type the type of weapon to search for. Should be one of the
 *	TYPE_ constants in the Entity class, or null to search for all
 *	weapon types.
 *	@return nearest weapon */
/*-------------------------------------------------------------------*/
	protected Entity getNearestWeapon(String type)
	{
		if(proxy == null || proxy.getWorld() == null)
			return null;

		return getNearestEntity(proxy.getWorld().getEntities(Entity.CAT_WEAPONS, type, null, true));
	}

/*-------------------------------------------------------------------*/
/**	Find the nearest object in the game world. Returns only entities
 *	which are currently active.
 *	@param type the type of object to search for. Should be one of the
 *	TYPE_ constants in the Entity class, or null to search for all
 *	object types.
 *	@param subType the subtype of object to search for. Should be one of
 *	the SUBTYPE_ constants in the Entity class, or null to search for all
 *	object subtypes.
 *	@return nearest item */
/*-------------------------------------------------------------------*/
	protected Entity getNearestObject(String type, String subType)
	{
		if(proxy == null || proxy.getWorld() == null)
			return null;

		return getNearestEntity(proxy.getWorld().getEntities(Entity.CAT_OBJECTS, type, subType, true));
	}

/*-------------------------------------------------------------------*/
/**	Set the agent's WaypointMap to be the specified WaypointMap.
 *	@param wp the WaypointMap that the agent should use */
/*-------------------------------------------------------------------*/
	protected void setWaypointMap(WaypointMap wp)
	{
		wpMap = wp;
	}

/*-------------------------------------------------------------------*/
/**	Return the agent's WaypointMap, thereby allowing full access to its
 *	facilities.
 *	@return the agent's WaypointMap */
/*-------------------------------------------------------------------*/
	protected WaypointMap getWaypointMap()
	{
		return wpMap;
	}

/*-------------------------------------------------------------------*/
/**	Load a WaypointMap object from a file created using the saveMap method.
 *	The resulting WaypointMap is set as the agent's navigation map, to be
 *	used when any waypoint-related BasicBot methods are invoked. A reference
 *	to the object is also returned for convenience.
 *	@param filename the path and name of the saved waypoint file
 *	@return the WaypointMap which was loaded from file, or null if the loading
 *	process failed
 *	@see soc.qase.ai.waypoint.WaypointMap#saveMap(String) */
/*-------------------------------------------------------------------*/
	protected WaypointMap loadWaypointMap(String filename)
	{
		return (wpMap = WaypointMap.loadMap(filename));
	}

/*-------------------------------------------------------------------*/
/**	Generate a WaypointMap by analysing a recorded DM2 demo. The resulting
 *	WaypointMap is set as the agent's navigation map, to be used when
 *	any waypoint-related BasicBot methods are invoked. A reference to the
 *	object is also returned for convenience. This method records the
 *	positions at which items were collected in the demo, and holds these
 *	locations constant through the clustering process.
 *	@param dm2Filename filename of the DM2 demo to analyse
 *	@param fNumWaypoints the number of nodes to generate for the WaypointMap.
 *	If this number is less than 1, it is treated as a percentage of the
 *	total number of observed player positions. If it is 1 or greater, it
 *	is treated as an absolute number of nodes to generate.
 *	@return the resulting WaypointMap
 *	@see soc.qase.ai.waypoint.WaypointMapGenerator#generate(String, float) */
/*-------------------------------------------------------------------*/
	protected WaypointMap generateWaypointMap(String dm2Filename, float fNumWaypoints)
	{
		return (wpMap = WaypointMapGenerator.generate(dm2Filename, fNumWaypoints));
	}

/*-------------------------------------------------------------------*/
/**	Get the closest Waypoint to the agent's current position at which
 *	an item of the the specified type resides. The category, type and
 *	subtype are typically passed using the constants found in the Entity
 *	class.
 *	@param cat the category of item to search for, or null to search for
 *	entities of any category
 *	@param type the type of item to search for, or null to search for
 *	entities of any type
 *	@param subType the subtype of item to search for, or null to search
 *	for entities of any subtype
 *	@return the closest Waypoint at which a matching item exists
 *	@see soc.qase.state.Entity */
/*-------------------------------------------------------------------*/
	protected Waypoint findClosestEntity(String cat, String type, String subType)
	{
		if(wpMap == null)
			return null;

		return wpMap.findClosestEntity(getPosition(), cat, type, subType);
	}

/*-------------------------------------------------------------------*/
/**	Get the closest Waypoint to the agent's current position at which
 *	the closest enemy player is located.
 *	@return the closest Waypoint to the nearest enemy player
 *	@see soc.qase.state.Entity */
/*-------------------------------------------------------------------*/
	protected Waypoint findClosestOpponent()
	{
		Entity tempEnemy = getNearestOpponent();

		if(wpMap == null || tempEnemy == null)
			return null;

		return wpMap.findClosestWaypoint(tempEnemy.getOrigin());
	}

/*-------------------------------------------------------------------*/
/**	Get the closest Waypoint to the agent's current position at which
 *	an item of the the given type resides. The item type is specified
 *	by inventory index; see the Inventory class for a list of inventory
 *	constants.
 *	@param itemInventoryIndex the inventory index corresponding to the
 *	item to search for
 *	@return the closest Waypoint at which a matching item exists
 *	@see soc.qase.state.Inventory */
/*-------------------------------------------------------------------*/
	protected Waypoint findClosestItem(int itemInventoryIndex)
	{
		if(wpMap == null)
			return null;

		return wpMap.findClosestItem(getPosition(), itemInventoryIndex);
	}

/*-------------------------------------------------------------------*/
/**	Get the closest Waypoint to the agent's current position at which
 *	an item of the the specified type resides. The type and subtype are
 *	typically passed using the constants found in the Entity class.
 *	@param type the type of item to search for, or null to search for
 *	items of any type
 *	@param subType the subtype of item to search for, or null to search
 *	for items of any subtype
 *	@return the closest Waypoint at which a matching item exists
 *	@see soc.qase.state.Entity */
/*-------------------------------------------------------------------*/
	protected Waypoint findClosestItem(String type, String subType)
	{
		return findClosestEntity(Entity.CAT_ITEMS, type, subType);
	}

/*-------------------------------------------------------------------*/
/**	Get the closest Waypoint to the agent's current position at which
 *	a weapon of the the specified type resides. The type is typically
 *	passed using the constants found in the Entity class.
 *	@param type the type of weapon to search for, or null to search for
 *	weapons of any type
 *	@return the closest Waypoint at which a matching weapon exists
 *	@see soc.qase.state.Entity */
/*-------------------------------------------------------------------*/
	protected Waypoint findClosestWeapon(String type)
	{
		return findClosestEntity(Entity.CAT_WEAPONS, type, null);
	}

/*-------------------------------------------------------------------*/
/**	Get the closest Waypoint to the agent's current position at which
 *	an object of the the specified type resides. The type and subtype are
 *	typically passed using the constants found in the Entity class.
 *	@param type the type of object to search for, or null to search for
 *	objects of any type
 *	@param subType the subtype of object to search for, or null to search
 *	for objects of any subtype
 *	@return the closest Waypoint at which a matching object exists
 *	@see soc.qase.state.Entity */
/*-------------------------------------------------------------------*/
	protected Waypoint findClosestObject(String type, String subType)
	{
		return findClosestEntity(Entity.CAT_OBJECTS, type, subType);
	}

/*-------------------------------------------------------------------*/
/**	Find the shortest path between the Waypoints closest to the agent's
 *	current position and the specified location. This uses the
 *	previously-generated cost and predecessor matrices.
 *	@param to the position to which we need a path
 *	@return a Waypoint array indicating the shortest path between the
 *	two Waypoints closest to the start and end positions */
/*-------------------------------------------------------------------*/
	protected Waypoint[] findShortestPath(Origin to)
	{
		if(wpMap == null)
			return null;

		return wpMap.findShortestPath(getPosition(), to);
	}

/*-------------------------------------------------------------------*/
/**	Get the path through the waypoint graph from the current position to
 *	the closest Waypoint at which an entity of the the given type resides.
 *	The category, type and subtype are typically passed using the
 *	constants found in the Entity class.
 *	@param cat the category of entity to search for, or null to search for
 *	entities of any category
 *	@param type the type of entity to search for, or null to search for
 *	entities of any type
 *	@param subType the subtype of entity to search for, or null to search for
 *	entities of any subtype
 *	@return a Waypoint array indicating the shortest path
 *	@see soc.qase.state.Entity */
/*-------------------------------------------------------------------*/
	protected Waypoint[] findShortestPathToEntity(String cat, String type, String subType)
	{
		if(wpMap == null)
			return null;

		return wpMap.findShortestPathToEntity(getPosition(), cat, type, subType);
	}

/*-------------------------------------------------------------------*/
/**	Get the path through the waypoint graph from the current position to
 *	the closest Waypoint at which an enemy player is located.
 *	@return a Waypoint array indicating the shortest path
 *	@see soc.qase.state.Entity */
/*-------------------------------------------------------------------*/
	protected Waypoint[] findShortestPathToOpponent()
	{
		Entity tempEnemy = getNearestOpponent();
		return (tempEnemy == null ? null : findShortestPath(tempEnemy.getOrigin()));
	}

/*-------------------------------------------------------------------*/
/**	Get the path through the waypoint graph from the current position to
 *	the closest Waypoint at which an item of the the given type resides.
 *	The item type is specified by inventory index; see the Inventory class
 *	for a list of inventory constants
 *	@param itemInventoryIndex the inventory index corresponding to the
 *	item to search for
 *	@return a Waypoint array indicating the shortest path
 *	@see soc.qase.state.Inventory */
/*-------------------------------------------------------------------*/
	protected Waypoint[] findShortestPathToItem(int itemInventoryIndex)
	{
		if(wpMap == null)
			return null;

		return wpMap.findShortestPathToItem(getPosition(), itemInventoryIndex);
	}

/*-------------------------------------------------------------------*/
/**	Get the path through the waypoint graph from the current position to
 *	the closest Waypoint at which an item of the the given type resides.
 *	The category, type and subtype are typically passed using the
 *	constants found in the Entity class.
 *	@param type the type of item to search for, or null to search for
 *	entities of any type
 *	@param subType the subtype of item to search for, or null to search for
 *	entities of any subtype
 *	@return a Waypoint array indicating the shortest path
 *	@see soc.qase.state.Entity */
/*-------------------------------------------------------------------*/
	protected Waypoint[] findShortestPathToItem(String type, String subType)
	{
		return findShortestPathToEntity(Entity.CAT_ITEMS, type, subType);
	}

/*-------------------------------------------------------------------*/
/**	Get the path through the waypoint graph from the current position to
 *	the closest Waypoint at which a weapon of the the given type resides.
 *	The type is typically passed using the constants found in the Entity
 *	class.
 *	@param type the type of weapon to search for, or null to search for
 *	weapons of any type
 *	@return a Waypoint array indicating the shortest path
 *	@see soc.qase.state.Entity */
/*-------------------------------------------------------------------*/
	protected Waypoint[] findShortestPathToWeapon(String type)
	{
		return findShortestPathToEntity(Entity.CAT_WEAPONS, type, null);
	}

/*-------------------------------------------------------------------*/
/**	Get the path through the waypoint graph from the current position to
 *	the closest Waypoint at which an object of the the given type resides.
 *	The type and subtype are typically passed using the constants found
 *	in the Entity class.
 *	@param type the type of object to search for, or null to search for
 *	objects of any type
 *	@param subType the subtype of object to search for, or null to search for
 *	objects of any subtype
 *	@return a Waypoint array indicating the shortest path
 *	@see soc.qase.state.Entity */
/*-------------------------------------------------------------------*/
	protected Waypoint[] findShortestPathToObject(String type, String subType)
	{
		return findShortestPathToEntity(Entity.CAT_OBJECTS, type, subType);
	}

/*-------------------------------------------------------------------*/
/**	Return the BSP parser object, thereby allowing full access to its
 *	facilities.
 *	@return the agent's BSP parser */
/*-------------------------------------------------------------------*/
	protected BSPParser getBSPParser()
	{
		if(isBotAlive() && (bsp.isMapLoaded() || readMap()))
			return bsp;
		else
			return null;
	}

/*-------------------------------------------------------------------*/
/** Returns all Item entities.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getItems(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getItems(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all entities which possess in-game models.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getModels(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getModels(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all Weapon entities.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getWeapons(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getWeapons(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all Monster entities.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getMonsters(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getMonsters(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all Door entities.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getDoors(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getDoors(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all Lift entities.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getLifts(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getLifts(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all Button entities.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getButtons(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getButtons(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all Illusory entities (i.e. visible but non-interactive).
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getIllusion(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getIllusion(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all Conveyor belts or trains.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getConveyors(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getConveyors(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all Teleport entities, both single-player and DeathMatch.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getTeleports(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getTeleports(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all single-player teleporters.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getNormalTeleports(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getNormalTeleports(vect);	}

/*-------------------------------------------------------------------*/
/** Returns DeathMatch teleporter entities.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getDMTeleports(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getDMTeleports(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all Secret Door entities.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getSecretDoors(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getSecretDoors(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all path corner entities.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getPathCorners(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getPathCorners(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all walkover-button entities.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getWalkovers(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getWalkovers(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all Teleport destination entities.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getTeleportDestinations(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getTeleportDestinations(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all Misc object entities (exploding barrels, etc).
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getMiscObjects(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getMiscObjects(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all spawn points, regardless of single or multi-player.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getStartPositions(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getStartPositions(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all single-player spawn points.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getPlayerStartPositions(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getPlayerStartPositions(vect);	}

/*-------------------------------------------------------------------*/
/** Returns all deathmatch spawn points.
 *	@param vect the Vector into which the entities will be added
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getDMStartPositions(Vector vect)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getDMStartPositions(vect);	}

/*-------------------------------------------------------------------*/
/** Returns an array of BSPLeaf objects representing the locations
 *	at which environmental features - water, lava, etc - matching
 *	the specified criteria are found. The argument should be a bitwise
 *	OR of one or more of the CONTENTS constants found in the BSPBrush
 *	class, e.g. CONTENTS_SLIME, CONTENTS_LAVA, CONTENTS_WATER. Each
 *	BSPLeaf contains two Vector3f objects specifying the minimum and
 *	maximum extents of the space occupied by the feature. For
 *	convenience, separate getLavaLocations, getWaterLocations,
 *	getSlimeLocations, getMistLocations and getWindowLocations
 *	methods are provided.
 *	@param brushBits a bitwise OR of the features to search for
 *	@return the BSPLeaf objects which match said bitwise OR, null
 *	if no matching leaves were found */
/*-------------------------------------------------------------------*/
	protected BSPLeaf[] getEnvironmentFeatureLocations(int brushBits)
	{
		if(!isBotAlive() || (!bsp.isMapLoaded() && !readMap()))
			return null;

		Vector envFeatureLeaves = new Vector();
		BSPLeaf[] bspLeaves = bsp.leafLump.leaves;

		for(int i = 0; i < bspLeaves.length; i++)
		{
			if((bspLeaves[i].brushOr & brushBits) != 0)
				envFeatureLeaves.add(bspLeaves[i]);
		}

		return (BSPLeaf[])envFeatureLeaves.toArray(new BSPLeaf[0]);
	}

/*-------------------------------------------------------------------*/
/** Returns an array of BSPLeaf objects representing the locations
 *	at which lava pools were found in the game environment.
 *	@return the BSPLeaf objects corresponding to lava pools, or null
 *	if no such pools were found */
/*-------------------------------------------------------------------*/
	protected BSPLeaf[] getLavaLocations()
	{
		return getEnvironmentFeatureLocations(BSPBrush.CONTENTS_LAVA);
	}

/*-------------------------------------------------------------------*/
/** Returns an array of BSPLeaf objects representing the locations
 *	at which water pools were found in the game environment.
 *	@return the BSPLeaf objects corresponding to water pools, or null
 *	if no such pools were found */
/*-------------------------------------------------------------------*/
	protected BSPLeaf[] getWaterLocations()
	{
		return getEnvironmentFeatureLocations(BSPBrush.CONTENTS_WATER);
	}

/*-------------------------------------------------------------------*/
/** Returns an array of BSPLeaf objects representing the locations
 *	at which poison slime pools were found in the game environment.
 *	@return the BSPLeaf objects corresponding to slime pools, or null
 *	if no such pools were found */
/*-------------------------------------------------------------------*/
	protected BSPLeaf[] getSlimeLocations()
	{
		return getEnvironmentFeatureLocations(BSPBrush.CONTENTS_SLIME);
	}

/*-------------------------------------------------------------------*/
/** Returns an array of BSPLeaf objects representing the locations
 *	at which mist coulds were found in the game environment.
 *	@return the BSPLeaf objects corresponding to mist clouds, or null
 *	if no such clouds were found */
/*-------------------------------------------------------------------*/
	protected BSPLeaf[] getMistLocations()
	{
		return getEnvironmentFeatureLocations(BSPBrush.CONTENTS_MIST);
	}

/*-------------------------------------------------------------------*/
/** Returns an array of BSPLeaf objects representing the locations
 *	at which windows were found in the game environment.
 *	@return the BSPLeaf objects corresponding to windows, or null
 *	if no windows were found */
/*-------------------------------------------------------------------*/
	protected BSPLeaf[] getWindowLocations()
	{
		return getEnvironmentFeatureLocations(BSPBrush.CONTENTS_WINDOW);
	}

/*-------------------------------------------------------------------*/
/** Returns all entities of the specified type. The supplied entity ID
 *	should match one of the integer constants found in BSPEntity.
 *	@param vect the Vector into which the entities will be added
 *	@param entID the type of entity to find and return; should be one
 *	of the integer constants from BSPEntity
 *	@return a reference to the newly-populated vect for convenience */
/*-------------------------------------------------------------------*/
	protected Vector getEntityType(Vector vect, int entID)
	{	return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? null : bsp.getEntityType(vect, entID);	}

	private Origin o = null;
	private Vector3f bbmin = null, bbmax = null;

/*-------------------------------------------------------------------*/
/** Determines whether the agent is currently riding one of the lift
 *	entities specified in the BSP file of the map.
 *	@return a reference to the BSPEntity describing the lift if the
 *	agent is on one, null otherwise. */
/*-------------------------------------------------------------------*/
	protected BSPEntity isOnLift()
	{
		if(!isBotAlive() || (!bsp.isMapLoaded() && !readMap()))
			return null;

		o = proxy.getWorld().getPlayer().getPlayerMove().getOrigin();

		for(int i = 0; i < bsp.entitiesLump.entities.length; i++)
		{
			if(bsp.entitiesLump.entities[i].isLift)
			{
				bbmin = bsp.entitiesLump.entities[i].model.bboxMin;
				bbmax = bsp.entitiesLump.entities[i].model.bboxMax;

				if(o.getX() >= bbmin.x && o.getX() <= bbmax.x && o.getY() >= bbmin.y && o.getY() <= bbmax.y && o.getZ() >= bbmin.z && o.getZ() <= bbmax.z + 50)
					return bsp.entitiesLump.entities[i];
			}
		}

		return null;
	}

/*-------------------------------------------------------------------*/
/**	Indicate whether visibility-checking functions should simply start
 *	from the actual location of the player entity, or from its point-of-view
 *	(i.e. the "camera" position). Defaults to FALSE.
 *	@param useVO if true, checks visibility of other points in the game
 *	environment from the bot's POV; if false, from the bot's location in
 *	the game world (i.e. the centre of the agent model) */
/*-------------------------------------------------------------------*/
	protected void useViewOffset(boolean useVO)
	{
		traceFromView = useVO;
	}

	private Vector3f dir = new Vector3f(0, 0, 0);
	private Vector3f pos = new Vector3f(0, 0, 0);
	private Vector3f enemyPos = new Vector3f(0, 0, 0);

/*-------------------------------------------------------------------*/
/**	Check whether a particular entity is visible from the player's
 *	current position.
 *	@param e the entity whose visibility will be checked
 *	@return true if visible, false otherwise */
/*-------------------------------------------------------------------*/
	protected boolean isVisible(Entity e)
	{
		return (e != null && isVisible(e.getOrigin()));
	}

/*-------------------------------------------------------------------*/
/**	Check whether a particular point in the environment is visible from
 *	the player's current position.
 *	@param o the point whose visibility will be checked
 *	@return true if visible, false otherwise */
/*-------------------------------------------------------------------*/
	protected boolean isVisible(Origin o)
	{
		return (o != null && isVisible(new Vector3f(o)));
	}

/*-------------------------------------------------------------------*/
/**	Check whether a particular point in the environment is visible from
 *	the player's current position.
 *	@param v the point whose visibility will be checked
 *	@return true if visible, false otherwise */
/*-------------------------------------------------------------------*/
	protected boolean isVisible(Vector3f v)
	{
		if(v == null || proxy == null || proxy.getWorld() == null) return false;

		pos.set(proxy.getWorld().getPlayer().getPlayerMove().getOrigin());
		if(traceFromView) pos.add(proxy.getWorld().getPlayer().getPlayerView().getViewOffset());

		return !isBotAlive() || (!bsp.isMapLoaded() && !readMap()) ? false : bsp.isVisible(pos, v);
	}

/*-------------------------------------------------------------------*/
/**	Check whether the nearest enemy in the game is currently visible.
 *	@return true if visible, false otherwise */
/*-------------------------------------------------------------------*/
	protected boolean isNearestEnemyVisible()
	{
		return isNearestEnemyVisible(false);
	}

/*-------------------------------------------------------------------*/
/**	Check whether the nearest enemy in the game is currently visible.
 *	@param withinFOV if true, constrain visibility check to agent's
 *	line-of-sight
 *	@return true if visible, false otherwise */
/*-------------------------------------------------------------------*/
	protected boolean isNearestEnemyVisible(boolean withinFOV)
	{
		if(!isBotAlive() || (!bsp.isMapLoaded() && !readMap()))
			return false;

		Entity nearEnemy = getNearestOpponent();

		if(nearEnemy != null)
		{
			enemyPos.set(nearEnemy.getOrigin());
			dir.sub(enemyPos, pos);

			if(traceFromView) pos.add(proxy.getWorld().getPlayer().getPlayerView().getViewOffset());
			return (withinFOV ? Utils.calcAngles(dir)[0] <= proxy.getWorld().getPlayer().getPlayerView().getFOV() / 2.0 && bsp.isVisible(pos, enemyPos) : bsp.isVisible(pos, enemyPos));
		}
		else
			return false;
	}

/*-------------------------------------------------------------------*/
/**	Projects a bounding-box through the game world in a given direction
 *	from the agent's current position, and returns the location of the
 *	first collision with solid geometry. Finds and loads the current game
 *	map transparently when called, using BSPParser.
 *	@param dir direction to sweep bounding box
 *	@param maxDist maximum distance across which to sweep
 *	@return the location of the first collision
 *	@see soc.qase.file.bsp.BSPParser */
/*-------------------------------------------------------------------*/
	protected Vector3f getObstacleLocation(Vector3f dir, float maxDist)
	{
		return getObstacleLocation(dir, BSPParser.TRACE_BOX, BSPBrush.CONTENTS_SOLID, maxDist);
	}

/*-------------------------------------------------------------------*/
/**	Get the radius of the sphere used to trace through the level. By
 *	default this is 18.0 units, or the same width as the player's
 *	bounding box.
 *	@return the sphere radius */
/*-------------------------------------------------------------------*/
	protected float getTraceSphereRadius()
	{
		return sphereRadius;
	}

/*-------------------------------------------------------------------*/
/**	Set the radius of the sphere used to trace through the level. This
 *	is permitted to be variable to account for different projectile sizes.
 *	@param radius the new sphere radius */
/*-------------------------------------------------------------------*/
	protected void setTraceSphereRadius(float radius)
	{
		sphereRadius = radius;
	}

/*-------------------------------------------------------------------*/
/**	Projects a bounding-box, sphere or line through the game world in
 *	a given direction from the agent's current position, and returns
 *	the location of the first collision with geometry matching the given
 *	type (specified using the constants in BSPBrush). Finds and loads
 *	the current game map transparently when called, using BSPParser.
 *	@param dir direction to sweep bounding box
 *	@param traceType the type of trace, specified using the constants
 *	found in BSPParser
 *	@param brushType the type of brush to check against, specified by
 *	the constants found in BSPBrush. Allows the agent to check for
 *	different types of terrain
 *	@param maxDist maximum distance across which to sweep
 *	@return the location of the first collision
 *	@see soc.qase.file.bsp.BSPParser
 *	@see soc.qase.file.bsp.BSPBrush*/
/*-------------------------------------------------------------------*/
	protected Vector3f getObstacleLocation(Vector3f dir, int traceType, int brushType, float maxDist)
	{
		if(!isBotAlive() || (!bsp.isMapLoaded() && !readMap()))
			return null;

		pos.set(proxy.getWorld().getPlayer().getPlayerMove().getOrigin());
		if(traceFromView) pos.add(proxy.getWorld().getPlayer().getPlayerView().getViewOffset());

		bsp.setBrushType(brushType);

		if(traceType == BSPParser.TRACE_LINE)
			return bsp.getObstacleLocation(pos, dir, maxDist);
		else if(traceType == BSPParser.TRACE_SPHERE)
			return bsp.getObstacleLocation(pos, dir, sphereRadius, maxDist);
		else if(traceType == BSPParser.TRACE_BOX)
			return bsp.getObstacleLocation(pos, dir, BOUNDING_MIN, BOUNDING_MAX, maxDist);
		else
			return null;
	}

/*-------------------------------------------------------------------*/
/**	Projects a bounding-box through the game world in a given direction
 *	from the agent's current position, and returns the distance to the
 *	nearest solid geometry. Finds and loads the current game map
 *	transparently when called, using BSPParser.
 *	@param dir direction to sweep bounding box
 *	@param maxDist maximum distance across which to sweep
 *	@return the location of the first collision
 *	@see soc.qase.file.bsp.BSPParser */
/*-------------------------------------------------------------------*/
	protected float getObstacleDistance(Vector3f dir, float maxDist)
	{
		return getObstacleDistance(dir, BSPParser.TRACE_BOX, BSPBrush.CONTENTS_SOLID, maxDist);
	}

/*-------------------------------------------------------------------*/
/**	Projects a bounding-box, sphere or line through the game world in
 *	a given direction from the agent's current position, and returns
 *	the distance to the nearest geometry matching the given type
 *	(specified using the constants in BSPBrush). Finds and loads the
 *	current game map transparently when called, using BSPParser.
 *	@param dir direction to sweep bounding box
 *	@param traceType the type of trace, specified using the constants
 *	found in BSPParser
 *	@param brushType the type of brush to check against, specified by
 *	the constants found in BSPBrush. Allows the agent to check for
 *	different types of terrain
 *	@param maxDist maximum distance across which to sweep
 *	@return the location of the first collision
 *	@see soc.qase.file.bsp.BSPParser
 *	@see soc.qase.file.bsp.BSPBrush*/
/*-------------------------------------------------------------------*/
	protected float getObstacleDistance(Vector3f dir, int traceType, int brushType, float maxDist)
	{
		if(!isBotAlive() || (!bsp.isMapLoaded() && !readMap()))
			return Float.NaN;

		pos.set(proxy.getWorld().getPlayer().getPlayerMove().getOrigin());
		if(traceFromView) pos.add(proxy.getWorld().getPlayer().getPlayerView().getViewOffset());

		bsp.setBrushType(brushType);

		if(traceType == BSPParser.TRACE_LINE)
			return bsp.getObstacleDistance(pos, dir, maxDist);
		else if(traceType == BSPParser.TRACE_SPHERE)
			return bsp.getObstacleDistance(pos, dir, sphereRadius, maxDist);
		else if(traceType == BSPParser.TRACE_BOX)
			return bsp.getObstacleDistance(pos, dir, BOUNDING_MIN, BOUNDING_MAX, maxDist);
		else
			return Float.NaN;
	}

/*-------------------------------------------------------------------*/
/**	Set the Quake 2 home directory. Used when locating the local BSP files
 *	containing the game geometry. Two alternatives to calling this method
 *	exist; the user can pass the folder location to the JVM as a variable
 *	called QUAKE2 using the -D switch (eg 'java -DQUAKE2="C:/quake2"'), or
 *	- the preferred approach - an environment variable called 'QUAKE2' can
 *	be declared which points to the home folder.
 *	@param q2hd the location of the Quake 2 home folder */
/*-------------------------------------------------------------------*/
	public static void setQuake2HomeDirectory(String q2hd)
	{
		q2HomeDir = q2hd;
	}

/*-------------------------------------------------------------------*/
/**	Return the Quake 2 home folder location.
 *	@return the location of the Quake 2 home folder */
/*-------------------------------------------------------------------*/
	public static String getQuake2HomeDirectory()
	{
		return q2HomeDir;
	}

/*-------------------------------------------------------------------*/
/**	Read a local geometry file into memory. The path to the BSP file can
 *	begin with the alias 'Q2HOME' (eg 'Q2HOME/maps/bsp1.bsp') which will
 *	automatically be resolved to the actual Quake 2 home folder using
 *	findQuake2HomeDirectory. If the correct BSP file is know before the
 *	agent connects to the server, this method can be used to pre-load it.
 *	@param filename the location of the BSP file
 *	@return true if the file was successfully read, false otherwise
 *	@see #setQuake2HomeDirectory
 *	@see #findQuake2HomeDirectory */
/*-------------------------------------------------------------------*/
	protected boolean readMap(String filename)
	{
		if(filename.substring(0, 6).equalsIgnoreCase("Q2HOME"))
		{
			if(q2HomeDir == null)
				findQuake2HomeDirectory();

			filename = q2HomeDir + filename.substring(6);
		}

		return bsp.load(filename);
	}

/*-------------------------------------------------------------------*/
/**	Read the current game map into memory. This will automatically
 *	deduce the name of the map, and will then search all possible
 *	locations in decreasing order of likelihood, including within PAK
 *	archives. Uses PAKParser and BSPParser.
 *	@return true if the map was successfully found and loaded, false
 *	otherwise
 *	@see soc.qase.file.pak.BSPParser
 *	@see soc.qase.file.pak.PAKParser */
/*-------------------------------------------------------------------*/
	private boolean readMap()
	{
		if(!isBotAlive() || mapNotFound)
			return false;

		try
		{	String pathAndFileName = null;
			String gameDir = proxy.getServer().getGameDirectory();
			String mapName = proxy.getServer().getMapName();
	
			if(gameDir == null || gameDir.length() == 0)
				gameDir = "baseq2";
	
			if(q2HomeDir == null || q2HomeDir.length() == 0)
				findQuake2HomeDirectory();
	
			// try to load BSP assuming filename == mapName
			if(!bsp.load(q2HomeDir + "/" + gameDir + "/maps/" + mapName + ".bsp"))
			{
				String pakBSPFilename = null;
				String pakDir = q2HomeDir + "/" + gameDir + "/";
	
				// search PAKs assuming filename == mapName
				for(int i = 0; i < 10; i++)
				{
					pakBSPFilename = PAKParser.findFileFromPAK(pakDir + "pak" + i + ".pak", mapName + ".bsp");
	
					if(pakBSPFilename != null)
					{
						bsp.load(pakDir + "pak" + i + ".pak#" + pakBSPFilename);
						break;
					}
				}
			}
	
			// search in BSP files for map name
			if(!bsp.isMapLoaded())
			{
				File bspDir = new File(q2HomeDir + "/" + gameDir + "/maps");
				String[] fileList = bspDir.list();
	
				for(int i = 0; i < fileList.length; i++)
				{
					if(fileList[i].toLowerCase().indexOf(".bsp") != -1 && BSPParser.isMapNameInFile(q2HomeDir + "/" + gameDir + "/maps/" + fileList[i], mapName))
					{
						bsp.load(q2HomeDir + "/" + gameDir + "/maps/" + fileList[i]);
						break;
					}
				}
			}
	
			// search in PAK files for map name
			if(!bsp.isMapLoaded())
			{
				String foundFile = null;
				String pakDir = q2HomeDir + "/" + gameDir + "/";
	
				for(int i = 0; i < 10; i++)
				{
					foundFile = PAKParser.findBSPFileFromPAK(pakDir + "pak" + i + ".pak", mapName);
	
					if(foundFile != null)
					{
						bsp.load(pakDir + "pak" + i + ".pak#" + foundFile);
						break;
					}
				}
			}
	
			mapNotFound = !bsp.isMapLoaded();
		}
		catch(Exception e)
		{	}

		return bsp.isMapLoaded();
	}

	private String findQuake2HomeDirectory()
	{
		q2HomeDir = System.getProperty("QUAKE2");

/*		// System.getenv not valid on most J2SE platforms
		if(q2Dir == null || q2Dir.length() == 0)
		{
			try
			{
				q2Dir = System.getenv("QUAKE2");
			}
			catch(Exception e)
			{	}
		}
*/

		if(q2HomeDir == null || q2HomeDir.length() == 0)
			q2HomeDir = Utils.parseEnvironmentVariables("QUAKE2");

		if(q2HomeDir == null || q2HomeDir.length() == 0)
			q2HomeDir = "c:/quake2";

		return q2HomeDir;
	}

/*-------------------------------------------------------------------*/
/**	Check if the proxy is recording the current game.
 *	@return true if the proxy is recording the current game,
 *	otherwise false. */
/*-------------------------------------------------------------------*/
	public boolean isRecording()
	{
		return proxy.isRecording();
	}

	protected void sendMovement()
	{
		if(globalAngles)
		{
			delta_Angles = proxy.getWorld().getPlayer().getPlayerMove().getDeltaAngles();
			angles.setYaw(angles.getYaw() - delta_Angles.getYaw());
			angles.setPitch(angles.getPitch() - delta_Angles.getPitch());
			angles.setRoll(-delta_Angles.getRoll());
		}

		proxy.sendMovement(angles, velocity, action);
	}

        public Proxy getProxy() {
            return proxy;
        }
}
