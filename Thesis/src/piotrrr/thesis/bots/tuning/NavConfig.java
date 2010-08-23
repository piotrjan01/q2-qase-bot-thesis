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
}
