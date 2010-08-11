package piotrrr.thesis.bots.rlbot;

import java.util.Map;

import piotrrr.thesis.bots.referencebot.ReferenceBotEntityRanking;

public class RLBotEntityRanking  extends ReferenceBotEntityRanking {
	
	
	
	public static float getBotWeaponDeficiency(RLBot bot, int addedWeaponIndex) {
		/**
		int BLASTER = 7, SHOTGUN = 8,
		SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
		GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
		RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
		ROCKETS = 21, SLUGS = 22;
		**/
		 
		float weightSum = 0f;
		
		for (Map.Entry<Integer, RLCombatModule.WeaponScore> e : bot.combatModule.weaponRanking.entrySet()) {
			weightSum += e.getValue().getScore();
		}

		float ownedWeapons = 0;
		
		for (Map.Entry<Integer, RLCombatModule.WeaponScore> e : bot.combatModule.weaponRanking.entrySet()) {
                    if (bot.botHasItem(e.getKey()))
			ownedWeapons += e.getValue().getScore();
		}
			
		
		//if there is some added weapon:
		if (addedWeaponIndex >= 7 && addedWeaponIndex <= 17) {
			ownedWeapons += bot.combatModule.weaponRanking.get(addedWeaponIndex).getScore();
		}
		
		float ret = 1f - ownedWeapons / (weightSum * bot.nConfig.weaponDeficiencyTolerance);
		if (ret < 0) ret = 0;
		return ret;  
		
	}
	
	
	
	

}
