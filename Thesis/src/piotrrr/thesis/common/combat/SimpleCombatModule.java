package piotrrr.thesis.common.combat;


import piotrrr.thesis.bots.tuning.CombatConfig;
import piotrrr.thesis.bots.tuning.WeaponConfig;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.CommFun;
import soc.qase.tools.vecmath.Vector3f;

public class SimpleCombatModule {
	
	public static FiringDecision getFiringDecision(MapBotBase bot) {
		Vector3f playerPos = bot.getBotPosition();
		EnemyInfo chosen = null;
		float chosenRisk = Float.MAX_VALUE;
		for (EnemyInfo ei : bot.kb.enemyInformation.values()) {
			
			if ( ei.getBestVisibleEnemyPart(bot) == null ) continue;
					
			float risk = CommFun.getDistanceBetweenPositions(playerPos, ei.getObjectPosition());
			if (risk < chosenRisk) {
				chosen = ei;
				chosenRisk = risk;
			}
		}
		if (chosen == null)	return null;
		float distance = CommFun.getDistanceBetweenPositions(playerPos, chosen.getObjectPosition());
		return new FiringDecision(chosen, chooseWeapon(bot, distance));
	}
	
	
	
	/**
	 * Chooses weapons considering the distance to the enemy.
	 */
	public static int chooseWeapon(MapBotBase bot, float distance) {
		/**
		int BLASTER = 7, SHOTGUN = 8,
		SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
		GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
		RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
		ROCKETS = 21, SLUGS = 22;
		**/
		
		if (bot.forcedweapon != 0) return bot.forcedweapon;
		
		int maxWeight = -1;
		int gunInd = 7;
		for (int i=7; i<18; i++) {
			if ( ! bot.botHasItem(i) || ! bot.botHasItem(WeaponConfig.ammoTable[i])) continue;
			if (distance < bot.cConfig.maxShortDistance4WpChoice && CombatConfig.isBannedForShortDistance(i)) continue;
			if (distance > bot.cConfig.minLongDistance && CombatConfig.isBannedForLongDistance(i)) continue;
			int weight = bot.wConfig.getWeaponWeightByInvIndex(i);
			if (weight > maxWeight) {
				maxWeight = weight;
				gunInd = i;
			}
		}
		
		return gunInd;
		
	}

}
