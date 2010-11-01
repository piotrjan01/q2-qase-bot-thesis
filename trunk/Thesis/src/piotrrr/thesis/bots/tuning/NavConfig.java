package piotrrr.thesis.bots.tuning;

import java.io.Serializable;
import java.lang.reflect.Field;

public class NavConfig implements Serializable {

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
    public OptParam weight_health = new OptParam(0.9f, 0, 1, 0.1);
    public OptParam weight_armor = new OptParam(0.3f, 0, 1, 0.1);
    public OptParam weight_weapon = new OptParam(0.6f, 0, 1, 0.1);
    public OptParam weight_ammo = new OptParam(0.3f, 0, 1, 0.1);
    public OptParam weight_distance = new OptParam(0.2f, 0, 1, 0.1);
    public OptParam weight_enemycost = new OptParam(0.5f, 0, 1, 0.1);
    public OptParam weight_aggresiveness = new OptParam(0.7f, 0, 1, 0.1);

    public void randAllParams() {
        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                OptParam p = (OptParam) f.get(this);
                p.setRandomValue();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        String ret = "NavConfig: \n";
        for (Field f : this.getClass().getDeclaredFields()) {
            try {
                if (!f.getType().equals(OptParam.class)) {
                    continue;
                }
                OptParam p = (OptParam) f.get(this);
                ret += f.getName()+": "+p.getValue()+"\n";
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return ret+"\n";
    }




}
