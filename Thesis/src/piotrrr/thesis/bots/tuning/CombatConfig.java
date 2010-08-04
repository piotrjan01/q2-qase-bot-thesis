package piotrrr.thesis.bots.tuning;

public class CombatConfig {
	
	/**
	 * Maximum time allowed that the bullet would take to hit the target. 
	 * If time to hit is grater than this, the shooting is not performed.
	 */
	public float maxTimeToHit = 20f;
	
	/**
	 * Maximum error of the prediction to still consider the prediction good
	 */
	public float maxPredictionError = 60f;
	
	/**
	 * The maximal age of enemy information to be considered relevant
	 * for prediction in the firing module.
	 */
	public int maxEnemyInfoAge4Firing = 2;
	
	/**
	 * The maximum distance to be still considered as short distance when firing
	 */
	public float maxShortDistance4Firing = 100f;
	
	/**
	 * The maximum distance to be still considered as short distance when choosing a weapon
	 */
	public float maxShortDistance4WpChoice = 400f;
	
	/**
	 * The minimum distance to be still considered as long distance
	 */
	public float minLongDistance = 800f;	
	
	/**
	int BLASTER = 7, SHOTGUN = 8,
	SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
	GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
	RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
	ROCKETS = 21, SLUGS = 22;
	**/
	
	/**
	 * The guns banned for the distance
	 */
	private static final int [] shortDistBanned = { 13, 12, 14, 17 };
	
	/**
	 * The guns banned for the distance
	 */
	private static final int [] longDistBanned = { 8, 9, 12, 13 };

        /**
	 * The guns that may hurt the shooting person
	 */
	private static final int [] dangerousGuns = { 12, 13, 14, 17 };
	
	/**
	 * Checks if the given weapon is banned for short distance
	 * @param inventoryIndex - the inventory index of the weapon
	 * @return
	 */
	public static boolean isBannedForShortDistance(int inventoryIndex) {
		for (int i : shortDistBanned) 
			if (i == inventoryIndex) return true;
		return false;
	}
	
	/**
	 * Checks if the given weapon is banned for long distance
	 * @param inventoryIndex - the inventory index of the weapon
	 * @return
	 */
	public static boolean isBannedForLongDistance(int inventoryIndex) {
		for (int i : longDistBanned) 
			if (i == inventoryIndex) return true;
		return false;
	}
	

        /**
         * Returns the speed of the bullet for a given gun index
         * @param gunIndex the gun index from the bot's inventory
         * @return the speed of the bullet of the given gun.
         */
	public int getBulletSpeedForGivenGun(int gunIndex) {
		/**
		int BLASTER = 7, SHOTGUN = 8,
		SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
		GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
		RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
		ROCKETS = 21, SLUGS = 22;
		**/
		
		//These are the calibrated values. 
		int rocketSpeed = 50;
		int blasterSpeed = 100;
		int hblasterSpeed = 100;
		
		int bspeed = Integer.MAX_VALUE;
		
		switch (gunIndex) {
		
		case 7:
			bspeed = blasterSpeed;
			break;
		case 14:
		case 17:
			bspeed = rocketSpeed;
			break;
		case 15:
			bspeed = hblasterSpeed;
			break;
		case 16:
		case 8:
		case 9:
		case 10:
		case 11:
			break;
		default: 
//			System.err.println("Warning: Wrong inventory gun index passed to " +
//					"BasicFiringModule. Gave: "+gunIndex);
			
		}
		return bspeed;
	}

    public boolean isDangerousToShootWith(int gunIndex) {
        for (int i : dangerousGuns) if (i==gunIndex) return true;
        return false;
    }
	
	
	
	
}
