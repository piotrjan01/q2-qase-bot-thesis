package piotrrr.thesis.common.navigation;

import java.util.TreeSet;

import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.bots.tuning.NavConfig;
import piotrrr.thesis.bots.tuning.WeaponConfig;
import piotrrr.thesis.common.entities.EntityDoublePair;
import soc.qase.state.Entity;

public class TuningEntityRanking {

    public static TreeSet<EntityDoublePair> getEntityFuzzyRanking(MapBotBase bot) {
        TreeSet<EntityDoublePair> ret = new TreeSet<EntityDoublePair>();
    
        return ret;
    }

    public static void doSettings(MapBotBase bot, Entity e) {

        double benefit = 0;
        double deficiency = 0;
        double dist = 0;
        double ec = 0;

        if (e.getType().equals(Entity.TYPE_HEALTH)) {
            benefit = getItemHealthBenefit(bot, e);
            deficiency = getBotHealthDeficiency(bot, 0);
        }
        else if (e.getType().equals(Entity.TYPE_ARMOR)) {
            benefit = getItemArmorBenefit(bot, e);
            deficiency = getBotArmorDeficiency(bot, 0);
        }
        else if (e.isWeaponEntity()) {
            benefit = getItemWeaponBenefit(bot, e);
            deficiency = getBotWeaponDeficiency(bot, 0);
        }
        else if (e.getType().equals(Entity.TYPE_AMMO)) {
            benefit = getItemAmmoBenefit(bot, e);
            deficiency = getBotAmmoDeficiency(bot, 0);
        }
    }

    public static float getBotHealthDeficiency(MapBotBase bot, int addedHealth) {
        float h = bot.getBotHealth() + addedHealth;
        if (h > NavConfig.recommendedHealthLevel) {
            h = NavConfig.recommendedHealthLevel;
        }
        return 1f - h / (float) NavConfig.recommendedHealthLevel;
    }

    public static float getBotArmorDeficiency(MapBotBase bot, int addedArmor) {
        float a = bot.getBotArmor() + addedArmor;
        if (a > NavConfig.recommendedArmorLevel) {
            a = NavConfig.recommendedArmorLevel;
        }
        return 1f - a / (float) NavConfig.recommendedArmorLevel;
    }

    public static float getBotWeaponDeficiency(MapBotBase bot, int addedWeaponIndex) {
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
            if (bot.botHasItem(wp)) {
                ownedWeapons += bot.wConfig.getWeaponWeightByInvIndex(wp);
            }
        }

        //if there is some added weapon:
        if (addedWeaponIndex >= 7 && addedWeaponIndex <= 17) {
            ownedWeapons += bot.wConfig.getWeaponWeightByInvIndex(addedWeaponIndex);
        }

        float ret = ownedWeapons / weightSum;
//        Dbg.prn(bot.getBotName()+": ow = "+ownedWeapons+" ws = "+weightSum+" perc="+ret);
        if (ret < 0) {
            ret = 0;
        }
        if (ret > NavConfig.recommendedWeaponPercent) {
            ret = NavConfig.recommendedWeaponPercent;
        }
        return 1f - ret / NavConfig.recommendedWeaponPercent;

    }

    public static float getBotAmmoDeficiency(MapBotBase bot, int addedAmmoIndex) {
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
            if (bot.botHasItem(am)) {
                ownedAmmo += bot.wConfig.getAmmoWeightByInventoryIndex(am) *
                        bot.getAmmunitionState(getWeaponFromAmmoIndex(bot, am));
            }
        }

        if (addedAmmoIndex >= 18 && addedAmmoIndex <= 22) {
            ownedAmmo += bot.wConfig.getAmmoWeightByInventoryIndex(addedAmmoIndex) * 0.5;
        }

//		Dbg.prn("owned ammo: "+ownedAmmo+" max ammo: "+maxAmmo);
        float ret = ownedAmmo / maxAmmo;
//        Dbg.prn(bot.getBotName() + ": oa = " + ownedAmmo + " ma = " + maxAmmo + " perc=" + ret);
        if (ret < 0) {
            ret = 0;
        }
        if (ret > NavConfig.recommendedAmmoPercent) {
            ret = NavConfig.recommendedAmmoPercent;
        }
        return 1f - ret / NavConfig.recommendedAmmoPercent;

    }

    public static float getItemHealthBenefit(MapBotBase bot, Entity e) {

        int growth = 0;

        if (e.getType().equals(Entity.TYPE_HEALTH)) {
            if (e.getSubType().equals(Entity.SUBTYPE_MEDIUM)) {
                growth = 10;
            } else if (e.getSubType().equals(Entity.SUBTYPE_LARGE)) {
                growth = 25;
            } else if (e.getSubType().equals(Entity.SUBTYPE_STIMPACK)) {
                growth = 2;
            }
        } else if (e.getType().equals(Entity.TYPE_MEGAHEALTH)) {
            growth = 20;
        } else if (e.getType().equals(Entity.TYPE_ADRENALINE)) {
            growth = 20;
        } else if (e.getType().equals(Entity.TYPE_INVULNERABILITY)) {
            growth = 40;
        }

        float before = getBotHealthDeficiency(bot, 0);
        float after = getBotHealthDeficiency(bot, growth);
        return (before - after);
    }

    public static float getItemArmorBenefit(MapBotBase bot, Entity e) {

        int growth = 0;

        if (e.getType().equals(Entity.TYPE_ARMOR)) {
            if (e.getSubType().equals(Entity.SUBTYPE_JACKETARMOR)) {
                growth = 25;
            } else if (e.getSubType().equals(Entity.SUBTYPE_COMBATARMOR)) {
                growth = 50;
            } else if (e.getSubType().equals(Entity.SUBTYPE_BODYARMOR)) {
                growth = 100;
            } else if (e.getSubType().equals(Entity.SUBTYPE_ARMORSHARD)) {
                growth = 1;
            }

            float before = getBotArmorDeficiency(bot, 0);
            float after = getBotArmorDeficiency(bot, growth);
            return (before - after);

        }
        return 0;
    }

    public static float getItemWeaponBenefit(MapBotBase bot, Entity e) {

        if (e.isWeaponEntity()) {
            int ind = e.getInventoryIndex();
            float before = getBotWeaponDeficiency(bot, 0);
            float after = getBotWeaponDeficiency(bot, ind);
            return (before - after);
        }
        return 0;
    }

    public static float getItemAmmoBenefit(MapBotBase bot, Entity e) {
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
    private static int getWeaponFromAmmoIndex(MapBotBase bot, int am) {
        for (int i = 0; i < WeaponConfig.ammoTable.length; i++) {
            if (WeaponConfig.ammoTable[i] == am) {
                return i;
            }
        }
        return -1;
    }
}
