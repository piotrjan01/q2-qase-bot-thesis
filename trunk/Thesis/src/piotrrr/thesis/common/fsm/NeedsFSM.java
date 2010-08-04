package piotrrr.thesis.common.fsm;

import piotrrr.thesis.bots.referencebot.*;
import java.util.LinkedList;

import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.common.entities.EntityTypeDoublePair;

/**
 * The Finite State Machine that is used to determine how much the bot 
 * wants each type of entity in its current state.
 * @author Piotr Gwizda�a
 */
public class NeedsFSM {
	
	//TODO: values to adjust:
	/**
	 * The wellness that is considered to be good.
	 */
	static final float GOOD_WELLNESS = 60f;
	/**
	 * Wellness considered to be bad.
	 */
	static final float BAD_WELLNESS = 40f;
	/**
	 * The importance of the health in wellness calculation.
	 */
	static final float HEALTH_WEIGHT = 0.6f;
	/**
	 * The importance of armor in wellness calculation.
	 */
	static final float ARMOR_WEIGHT = 0.4f;
	/**
	 * The fire power that is considered to be good.
	 */
	static final float GOOD_FIRE_POWER = 0.0035f;
	/**
	 * The bad fire power that is considered to be insufficient.
	 */
	static final float BAD_FIRE_POWER = GOOD_FIRE_POWER*0.8f;
	
	/**
	 * The bot which uses the FSM.
	 */
	BotBase bot;
	
	/**
	 * The current state.
	 */
	State state;
	
	/**
	 * Basic constructor
	 * @param bot the bot that uses FSM
	 */
	public NeedsFSM(BotBase bot) {
		this.bot = bot;
		this.state = new HealingState(bot);
	}
	
	/**
	 * Returns the array of entity types that bot desires in current state
	 * along with a value indicating how much it desires them.
	 * @return
	 */
	public EntityTypeDoublePair [] getDesiredEntities() {
		state = state.getNextState();
		return state.getDesiredEntities();
	}
	
	/**
	 * @return the current state's name.
	 */
	public String getCurrentStateName() {
		return state.getClass().toString();
	}
	
	/**
	 * Returns the bot's wellness value.
	 * @param bot the bot whose wellness we want to calculate
	 * @return the wellness.
	 */
	static float getBotWellness(BotBase bot) {
		return HEALTH_WEIGHT*bot.getBotHealth()+ARMOR_WEIGHT*bot.getBotArmor();
	}
	
	/**
	 * Returns the bot's fire power according to the scores table of each weapon.
	 * @return the number between 0 and 1.
	 */
	static float getBotFirePower(BotBase bot) {
		/**
		 * TODO: Values to adjust:
		int BLASTER = 7, SHOTGUN = 8,
		SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
		GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
		RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
		ROCKETS = 21, SLUGS = 22;
		**/
		int [] importanceTable = { 0, 0, 0, 0, 0, 0, 0,
				0, //blaster - isn't counted
				10, //shotgun - good for short and medium dist.
				15, //ss - v. good for short dist.
				15, //mgun - the bot rulez with it
				30, //chgun - as well here
				0, // granades - the bot sux with them.
				5, //g launcher - kind of sux still.
				30, //r launcher - is ok for slow moving enemies.
				30, //hyperblaster - ok
				35, //railgun - Rules
				15 //bfgk - energy cells are used too fast with it
		};
		float maxImportance = 0;
		for (int i : importanceTable) maxImportance += i;

		float result = 0.0f;
		LinkedList<Integer> guns = bot.getIndexesOfOwnedGunsWithAmmo();
		for (int i : guns) {
				result += (importanceTable[i]*bot.getAmmunitionState(i));
		}
		return result / maxImportance;
		
	}

}
