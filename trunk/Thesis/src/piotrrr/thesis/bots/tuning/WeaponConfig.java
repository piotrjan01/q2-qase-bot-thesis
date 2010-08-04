package piotrrr.thesis.bots.tuning;

public class WeaponConfig extends Config {
	
	/**
	BLASTER = 7, SHOTGUN = 8,
	SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
	GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
	RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
	ROCKETS = 21, SLUGS = 22;
	**/
	public int [] usableWeapons = {7, 8, 9, 10, 11, 14, 15, 16, 17};
	
	public int [] usableAmmo = {18, 19, 20, 21, 22};
	
	
	/**
	 * The table that specifies which gun uses which ammunition type.
	 */
	public static final int [] ammoTable = { 0, 0, 0, 0, 0, 0, 0,
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
	
	/**
	 * Weapon preference weight
	 */
	public int wpWBlaster = 10;
	public int wpWBlaster_MIN = 0;
	public int wpWBlaster_MAX = 100;
	
	/**
	 * Weapon preference weight
	 */
	public int wpWShotgun = 30;
	public int wpWShotgun_MIN = 0;
	public int wpWShotgun_MAX = 100;
	
	/**
	 * Weapon preference weight
	 */	
	public int wpWSuperShotgun = 40;
	public int wpWSuperShotgun_MIN = 0;
	public int wpWSuperShotgun_MAX = 100;

	/**
	 * Weapon preference weight
	 */	
	public int wpWMachinegun = 40;
	public int wpWMachinegun_MIN = 0;
	public int wpWMachinegun_MAX = 100;

	/**
	 * Weapon preference weight
	 */	
	public int wpWChaingun = 60;
	public int wpWChaingun_MIN = 0;
	public int wpWChaingun_MAX = 100;

	/**
	 * Weapon preference weight
	 */	
	public int wpWGrenades = 0;
	public int wpWGrenades_MIN = 0;
	public int wpWGrenades_MAX = 100;

	/**
	 * Weapon preference weight
	 */	
	public int wpWGrenadeLauncher = 0;
	public int wpWGrenadeLauncher_MIN = 0;
	public int wpWGrenadeLauncher_MAX = 100;

	/**
	 * Weapon preference weight
	 */	
	public int wpWRocketLauncher = 50;
	public int wpWRocketLauncher_MIN = 0;
	public int wpWRocketLauncher_MAX = 100;

	/**
	 * Weapon preference weight
	 */	
	public int wpWHyperblaster = 60;
	public int wpWHyperblaster_MIN = 0;
	public int wpWHyperblaster_MAX = 100;
	
	/**
	 * Weapon preference weight
	 */	
	public int wpWRailgun = 90;
	public int wpWRailgun_MIN = 0;
	public int wpWRailgun_MAX = 100;
	
	/**
	 * Weapon preference weight
	 */	
	public int wpWBFG10K = 40;
	public int wpWBFG10K_MIN = 0;
	public int wpWBFG10KMax_MAX = 100;
	
	public int getWeaponWeightByInvIndex(int ind) {
		/**
		int BLASTER = 7, SHOTGUN = 8,
		SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
		GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
		RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
		ROCKETS = 21, SLUGS = 22;
		**/
		
		switch (ind) {
		case 7:
			return wpWBlaster;
		case 8:
			return wpWShotgun;
		case 9:
			return wpWSuperShotgun;
		case 10:
			return wpWMachinegun;
		case 11:
			return wpWChaingun;
		case 12:
			return wpWGrenades;
		case 13:
			return wpWGrenadeLauncher;
		case 14:
			return wpWRocketLauncher;
		case 15:
			return wpWHyperblaster;
		case 16:
			return wpWRailgun;
		case 17:
			return wpWBFG10K;
		default:
			return 0;
		}
	}
	
	public int getAmmoWeightByInventoryIndex(int ind) {
		int ret = 0;
		//for all the weapons
		for (int wp : usableWeapons) {
			//if the weapon uses the ammo - we increase its weight
			if (ammoTable[wp] == ind) ret += getWeaponWeightByInvIndex(wp);
		}
		return ret;
	}
	
}
