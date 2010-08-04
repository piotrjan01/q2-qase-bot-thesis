package piotrrr.thesis.bots.referencebot;

import java.util.TreeSet;

import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.bots.tuning.NavConfig;
import piotrrr.thesis.bots.tuning.WeaponConfig;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.combat.EnemyInfo;
import piotrrr.thesis.common.entities.EntityDoublePair;
import soc.qase.ai.waypoint.Waypoint;
import soc.qase.state.Entity;

public class ReferenceBotEntityRanking {
	
	public static TreeSet<EntityDoublePair> getEntityRanking(ReferenceBot bot) {
		TreeSet<EntityDoublePair> ret = new TreeSet<EntityDoublePair>();
		for (Entity e : bot.kb.getAllPickableEntities()) {
			float rank = getRankForEntity(bot, e);
			ret.add(new EntityDoublePair(e, rank));
//			Dbg.prn("add: "+e.toString()+" rank: "+rank+" ranking-size: "+ret.size());
		}
		return ret;
	}
	
	public static String getRankingDebugInfo(ReferenceBot bot) {
		TreeSet<EntityDoublePair> ranking = getEntityRanking(bot);
		int count = 0;
		String t = "\n";
//		t += bot.getBotName()+", h="+bot.getBotHealth()+", a="+bot.getBotArmor();
//		t += "\ndef-hlh="+getBotHealthDeficiency(bot, 0);
//		t += " def-arm="+getBotArmorDeficiency(bot, 0);
//		t += "\ndef-amm="+getBotAmmoDeficiency(bot, 0);
//		t += " def-wpn="+getBotWeaponDeficiency(bot, 0);
//		t += " ranking-size="+ranking.size()+"\n";
		for (EntityDoublePair ed : ranking.descendingSet()) {
			count++;
			if (count > 5) break;
//			t += "\n"+count+": ent="+ed.ent.toString()+" rank="+ed.dbl;
//			t += "\nben-hlh="+getItemHealthBenefit(bot, ed.ent);
//			t += " ben-arm="+getItemArmorBenefit(bot, ed.ent);
//			t += "\nben-amm="+getItemAmmoBenefit(bot, ed.ent);
//			t += " ben-wpn="+getItemWeaponBenefit(bot, ed.ent);
//			t += "\nenemy-cost="+getEnemyCost(bot, ed.ent);
//			t += "\ndistance="+SmartBotGlobalNav.getDistanceFollowingMap(bot, bot.getBotPosition(), ed.ent.getObjectPosition());
//			t += "\n";
			
			Entity e = ed.ent;
			NavConfig c = bot.nConfig;
			float h = c.healthWeight*getItemHealthBenefit(bot, e)*getBotHealthDeficiency(bot, 0);
			float arm = c.armorWeight*getItemArmorBenefit(bot, e)*getBotArmorDeficiency(bot, 0) ;
			float w = c.weaponsWeight*getItemWeaponBenefit(bot, e)*getBotWeaponDeficiency(bot, 0);
			float amm = c.ammoWeight*getItemAmmoBenefit(bot, e)*getBotAmmoDeficiency(bot, 0) ;
			float ec = c.enemyCostWeight*getEnemyCost(bot, e) ;
			float dist = c.distanceWeight*getDistanceFactor(bot, e);
			
			float rank = Math.abs(h)+Math.abs(arm)+Math.abs(w)+Math.abs(amm)+Math.abs(ec)+Math.abs(dist);
			
			t += "\ndef-hlh="+getBotHealthDeficiency(bot, 0);
			t += " def-arm="+getBotArmorDeficiency(bot, 0);
			t += " def-amm="+getBotAmmoDeficiency(bot, 0);
			t += " def-wpn="+getBotWeaponDeficiency(bot, 0);
			t += e.toString()+":\n h="+ftoperc(h/rank)+"% arm="+ftoperc(arm/rank)+"% w="+ftoperc(w/rank)+
			"% amm="+ftoperc(amm/rank)+"% ec="+ftoperc(ec/rank)+"% dist="+ftoperc(dist/rank)+"%";
			
			
		}
		return t;
	}
	
	private static int ftoperc(float f) {
		return (int)(f*100);
	}
	
	
	
	public static float getRankForEntity(ReferenceBot bot, Entity e) {
		NavConfig c = bot.nConfig;
		float rank = c.healthWeight*getItemHealthBenefit(bot, e)*getBotHealthDeficiency(bot, 0);
		rank += c.armorWeight*getItemArmorBenefit(bot, e)*getBotArmorDeficiency(bot, 0);
		rank += c.weaponsWeight*getItemWeaponBenefit(bot, e)*getBotWeaponDeficiency(bot, 0);
		rank += c.ammoWeight*getItemAmmoBenefit(bot, e)*getBotAmmoDeficiency(bot, 0);
		rank -= c.enemyCostWeight*getEnemyCost(bot, e);
		rank -= c.distanceWeight*getDistanceFactor(bot, e);
		return rank;
	}
	
	public static float getDistanceFactor(ReferenceBot bot, Entity e) {
		float dist = (float) ReferenceBotGlobalNav.getDistanceFollowingMap(bot, bot.getBotPosition(), e.getObjectPosition());
		float ret = dist / NavConfig.MAX_DISTANCE;
		if (ret > 1) ret = 1;
		// !!! increased by health deficiency percent
		return ret*(1+getBotHealthDeficiency(bot, 0));
	}
	
	public static float getEnemyCost(ReferenceBot bot, Entity e) {
		float riskyDistance = 400;
		float cost = 0;
		
		Waypoint [] path = bot.kb.map.findShortestPath(bot.getBotPosition(), e.getObjectPosition());
		if (path == null) return Float.MAX_VALUE;
		float costMax = path.length;
		for (EnemyInfo en : bot.kb.getAllEnemyInformation()) {
			for (Waypoint w : path) {
				float dist = CommFun.getDistanceBetweenPositions(w.getObjectPosition(), en.getObjectPosition()); 
				if (dist < riskyDistance) 
					cost += 1 - dist / riskyDistance; 
			}
		}
		return cost/costMax;
	}
	
	public static float getBotHealthDeficiency(ReferenceBot bot, int addedHealth) {
		float h = bot.getBotHealth() + addedHealth;
		if (h > BotBase.maxHealth) h = BotBase.maxHealth;
		return 1f - h / (float)BotBase.maxHealth;
	}
	
	public static float getBotArmorDeficiency(ReferenceBot bot, int addedArmor) {
		float a = bot.getBotArmor() + addedArmor;
		if (a > BotBase.maxArmor) a = BotBase.maxArmor;
		return 1f - a / (float)BotBase.maxArmor;
	}
	
	public static float getBotWeaponDeficiency(ReferenceBot bot, int addedWeaponIndex) {
		/**
		int BLASTER = 7, SHOTGUN = 8,
		SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
		GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
		RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
		ROCKETS = 21, SLUGS = 22;
		**/
		 
		float weightSum = 0f;
		
		for (int wp : bot.wConfig.usableWeapons) {
			weightSum += bot.wConfig.getWeaponWeightByInvIndex(wp);
		}

		float ownedWeapons = 0;
		
		for (int wp : bot.wConfig.usableWeapons) {
			if (bot.botHasItem(wp))
				ownedWeapons += bot.wConfig.getWeaponWeightByInvIndex(wp);
		}
		
		//if there is some added weapon:
		if (addedWeaponIndex >= 7 && addedWeaponIndex <= 17) {
			ownedWeapons += bot.wConfig.getWeaponWeightByInvIndex(addedWeaponIndex);
		}
		
		float ret = 1f - ownedWeapons / (weightSum * bot.nConfig.weaponDeficiencyTolerance);
		if (ret < 0) ret = 0;
		return ret;  
		
	}
	
	public static float getBotAmmoDeficiency(ReferenceBot bot, int addedAmmoIndex) {
		/**
		int BLASTER = 7, SHOTGUN = 8,
		SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
		GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
		RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
		ROCKETS = 21, SLUGS = 22;
		**/
		 
		float maxAmmo = 0f;
		
		for (int am : bot.wConfig.usableAmmo) {
			maxAmmo += bot.wConfig.getAmmoWeightByInventoryIndex(am);
		}
		
		float ownedAmmo = 0;
		
		for (int am : bot.wConfig.usableAmmo) {
			if (bot.botHasItem(am))
				ownedAmmo += bot.wConfig.getAmmoWeightByInventoryIndex(am) * 
							bot.getAmmunitionState(getWeaponFromAmmoIndex(bot, am));
		}
		
		if (addedAmmoIndex >= 18 && addedAmmoIndex <= 22) {
			ownedAmmo += bot.wConfig.getAmmoWeightByInventoryIndex(addedAmmoIndex) * 
				bot.getAmmunitionState(getWeaponFromAmmoIndex(bot, addedAmmoIndex));
		}
		
//		Dbg.prn("owned ammo: "+ownedAmmo+" max ammo: "+maxAmmo);
		float ret = 1f - ownedAmmo / maxAmmo; 
		if (ret < 0) ret = 0;
		return  ret;
		
	}
	
	public static float getItemHealthBenefit(ReferenceBot bot, Entity e) {
		
		int growth = 0;
		
		if (e.getType().equals(Entity.TYPE_HEALTH)) {
			if (e.getSubType().equals(Entity.SUBTYPE_MEDIUM)) growth = 10;
			else if (e.getSubType().equals(Entity.SUBTYPE_LARGE)) growth = 25;
			else if (e.getSubType().equals(Entity.SUBTYPE_STIMPACK)) growth = 2;
		}
		else if (e.getType().equals(Entity.TYPE_MEGAHEALTH)) growth = 20;
		else if (e.getType().equals(Entity.TYPE_ADRENALINE)) growth = 20;
		else if (e.getType().equals(Entity.TYPE_INVULNERABILITY)) growth = 40;
		
		float before = getBotHealthDeficiency(bot, 0);
		float after = getBotHealthDeficiency(bot, growth);
		return (before - after);
	}
	
	public static float getItemArmorBenefit(ReferenceBot bot, Entity e) {
		
		int growth = 0;
		
		if (e.getType().equals(Entity.TYPE_ARMOR)) {
			if (e.getSubType().equals(Entity.SUBTYPE_JACKETARMOR)) growth = 25;
			else if (e.getSubType().equals(Entity.SUBTYPE_COMBATARMOR)) growth = 50;
			else if (e.getSubType().equals(Entity.SUBTYPE_BODYARMOR)) growth = 100;
			else if (e.getSubType().equals(Entity.SUBTYPE_ARMORSHARD)) growth = 1;
			
			float before = getBotArmorDeficiency(bot, 0);
			float after = getBotArmorDeficiency(bot, growth);
			return (before - after);
			
		}
		return 0;
	}
	
	public static float getItemWeaponBenefit(ReferenceBot bot, Entity e) {
		
		if (e.isWeaponEntity()) {
			int ind = e.getInventoryIndex();
			float before = getBotWeaponDeficiency(bot, 0);
			float after = getBotWeaponDeficiency(bot, ind);
			return (before - after);
		}
		return 0;
	}
  	
	public static float getItemAmmoBenefit(ReferenceBot bot, Entity e) {
		//FIXME:
		if (e.getType().equals(Entity.TYPE_AMMO)) {
			int ind = e.getInventoryIndex();
			float before = getBotAmmoDeficiency(bot, 0);
			float after = getBotAmmoDeficiency(bot, ind);
			return (before - after);
		}
		return 0;
	}
	
	/**
	 * Finds the weapon index that uses specified ammo
	 * @param bot
	 * @param am
	 * @return
	 */
	private static int getWeaponFromAmmoIndex(ReferenceBot bot, int am) {
		for (int i=0; i<WeaponConfig.ammoTable.length; i++) {
			if (WeaponConfig.ammoTable[i] == am) return i;
		}
		return -1;
	}
	
	
	
	
	

}
