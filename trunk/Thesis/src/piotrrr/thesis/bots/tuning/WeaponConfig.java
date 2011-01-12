package piotrrr.thesis.bots.tuning;

public class WeaponConfig {

    /**
    BLASTER = 7, SHOTGUN = 8,
    SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
    GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
    RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
    ROCKETS = 21, SLUGS = 22;
     **/
    /**
     * The weapons that the bot can use
     */
    public int[] usableWeapons = {7, 8, 9, 10, 11, 14, 15, 16, 17};

    /**
     * The ammo that the bot can use
     */
    public int[] usableAmmo = {18, 19, 20, 21, 22};
    /**
     * The table that specifies which gun uses which ammunition type.
     */
    public static final int[] ammoTable = {0, 0, 0, 0, 0, 0, 0,
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
    /**
     * Weapon preference weight
     */
    public int wpWShotgun = 20;
    /**
     * Weapon preference weight
     */
    public int wpWSuperShotgun = 30;
    /**
     * Weapon preference weight
     */
    public int wpWMachinegun = 40;
    /**
     * Weapon preference weight
     */
    public int wpWChaingun = 50;
    /**
     * Weapon preference weight
     */
    public int wpWGrenades = 0;
    /**
     * Weapon preference weight
     */
    public int wpWGrenadeLauncher = 0;
    /**
     * Weapon preference weight
     */
    public int wpWRocketLauncher = 40;
    /**
     * Weapon preference weight
     */
    public int wpWHyperblaster = 80;
    /**
     * Weapon preference weight
     */
    public int wpWRailgun = 90;
    /**
     * Weapon preference weight
     */
    public int wpWBFG10K = 40;

    /**
     *
     * @param ind the index of the weapon in bot's inventory
     * @return the weapon weight
     */
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

    /**
     * @param ind the inventory index of the ammunition
     * @return the weight of specified ammo
     */
    public int getAmmoWeightByInventoryIndex(int ind) {
        int ret = 0;
        //for all the weapons
        for (int wp : usableWeapons) {
            //if the weapon uses the ammo - we increase its weight
            if (ammoTable[wp] == ind) {
                ret += getWeaponWeightByInvIndex(wp);
            }
        }
        return ret;
    }
}
