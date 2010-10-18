package piotrrr.thesis.bots.tuning;

public class NavConfig {

    /**
     * What defitiency we agree to when we attack the enemy
     */
    public static float aggressiveness = 0.5f;
    /**
     * The recommended ammount of health it is established the bot should have.
     * It is used to calculate the deficiency of health.
     */
    public static final int recommendedHealthLevel = 70;
    /**
     * The recommended armor the bot should have.
     * @see BotBase#recommendedHealthLevel
     */
    public static final int recommendedArmorLevel = 30;
    /**
     * The recommended firepower the bot should have.
     * @see BotBase#recommendedHealthLevel
     */
    public static final float recommendedWeaponPercent = 0.4f;

    /**
     * Recommended ammo percent, the bot should have
     */
    public static final float recommendedAmmoPercent = 0.3f;


    public static float weight_health = 0.9f;

    public static float weight_armor = 0.3f;

    public static float weight_weapon = 0.6f;

    public static float weight_ammo = 0.3f;

    public static float weight_distance = 0.2f;

    public static float weight_enemycost = 0.5f;
    
    public static float weight_aggresiveness = 0.7f;

}
