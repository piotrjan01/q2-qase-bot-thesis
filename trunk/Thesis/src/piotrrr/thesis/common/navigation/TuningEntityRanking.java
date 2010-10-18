package piotrrr.thesis.common.navigation;

import java.util.HashMap;
import java.util.TreeSet;

import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.bots.tuning.NavConfig;
import piotrrr.thesis.bots.tuning.WeaponConfig;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.combat.EnemyInfo;
import piotrrr.thesis.common.entities.EntityDoublePair;
import soc.qase.ai.waypoint.Waypoint;
import soc.qase.state.Entity;
import soc.qase.tools.vecmath.Vector3f;

public class TuningEntityRanking {

    private static class MyVector extends Vector3f {

        public MyVector(Vector3f v) {
            x = v.x;
            y = v.y;
            z = v.z;
        }

        @Override
        public int hashCode() {
            return (int) x;
        }
    }

    private static class RankingCache {

        HashMap<MyVector, HashMap<MyVector, Double>> distCache = new HashMap<MyVector, HashMap<MyVector, Double>>();
        HashMap<MyVector, HashMap<MyVector, Waypoint[]>> pathCache = new HashMap<MyVector, HashMap<MyVector, Waypoint[]>>();
        int cacheUse = 0;
        int fnCalls = 0;
    }

    private static class Measures {

        //benefits
        double healthBen = 0;
        double armorBen = 0;
        double weaponBen = 0;
        double ammoBen = 0;
        //deficiencies
        double healthDef = 0;
        double armorDef = 0;
        double weaponDef = 0;
        double ammoDef = 0;
        double dist = 0;
        double enemyCost = 0;

        void updateToMaximumValues(Measures m) {
            if (healthBen < m.healthBen) healthBen = m.healthBen;
            if (armorBen < m.armorBen) armorBen = m.armorBen;
            if (weaponBen < m.weaponBen) weaponBen = m.weaponBen;
            if (ammoBen < m.ammoBen) ammoBen = m.ammoBen;
            if (healthDef < m.healthDef) healthDef = m.healthDef;
            if (armorDef < m.armorDef) armorDef = m.armorDef;
            if (weaponDef < m.weaponDef) weaponDef = m.weaponDef;
            if (ammoDef < m.ammoDef) ammoDef = m.ammoDef;
            if (dist < m.dist) dist = m.dist;
            if (enemyCost < m.enemyCost) enemyCost = m.enemyCost;
        }

        void normalize(Measures maximums) {
            healthBen /= maximums.healthBen;
            armorBen /= maximums.armorBen;
            weaponBen /= maximums.weaponBen;
            ammoBen /= maximums.ammoBen;
            healthDef /= maximums.healthDef;
            armorDef /= maximums.armorDef;
            weaponDef /= maximums.weaponDef;
            ammoDef /= maximums.ammoDef;
            dist /= maximums.dist;
            enemyCost /= maximums.enemyCost;
        }

    }

    public static TreeSet<EntityDoublePair> getEntityFuzzyRanking(MapBotBase bot) {
        TreeSet<EntityDoublePair> ret = new TreeSet<EntityDoublePair>();
        RankingCache cache = new RankingCache();
        HashMap<Entity, Measures> em = new HashMap<Entity, Measures>();
        Measures max = new Measures();

        //get measures and maximums
        for (Entity ent : bot.kb.getAllPickableEntities()) {
            Measures m = getMeasures(bot, ent, cache);
            em.put(ent, m);
            max.updateToMaximumValues(m);
        }

        //normalize measures
        for (Measures m : em.values()) {
            m.normalize(max);
        }

        for (Entity e : em.keySet()) {
            Measures m = em.get(e);
            double rank = bot.nConfig.weight_health*(m.healthBen+m.healthDef);
            rank += bot.nConfig.weight_armor*(m.armorBen+m.armorDef);
            rank += bot.nConfig.weight_weapon*(m.weaponBen+m.weaponDef);
            rank += bot.nConfig.weight_ammo*(m.ammoBen+m.ammoDef);
            rank -= bot.nConfig.weight_distance*m.dist;
            rank -= bot.nConfig.weight_enemycost*m.enemyCost;
            ret.add(new EntityDoublePair(e, rank));
        }
        
        return ret;
    }

    private static Measures getMeasures(MapBotBase bot, Entity e, RankingCache cache) {

        Measures m = new Measures();

        if (e.getType().equals(Entity.TYPE_HEALTH)) {
            m.healthBen = getItemHealthBenefit(bot, e);
            m.healthDef = getBotHealthDeficiency(bot, 0);
        } else if (e.getType().equals(Entity.TYPE_ARMOR)) {
            m.armorBen = getItemArmorBenefit(bot, e);
            m.armorDef = getBotArmorDeficiency(bot, 0);
        } else if (e.isWeaponEntity()) {
            m.weaponBen = getItemWeaponBenefit(bot, e);
            m.weaponDef = getBotWeaponDeficiency(bot, 0);
        } else if (e.getType().equals(Entity.TYPE_AMMO)) {
            m.ammoBen = getItemAmmoBenefit(bot, e);
            m.ammoDef = getBotAmmoDeficiency(bot, 0);
        }

        m.dist = getDistanceFollowingMap(bot, bot.getBotPosition(), e.getObjectPosition(), cache);
        m.enemyCost = getEnemyCost(bot, e, cache);

        return
         m;
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

    /**
     * Returns the distance between given positions following the map
     * @param bot the bot for whom the distance is calculated
     * @param from initial position
     * @param to final position
     * @return distance between from and to following the shortest path on the map.
     * Double.MAX_VALUE is returned in case there is no path.
     */
    private static double getDistanceFollowingMap(MapBotBase bot, Vector3f vfrom, Vector3f vto, RankingCache cache) {
        cache.fnCalls++;
        MyVector from = new MyVector(vfrom);
        MyVector to = new MyVector(vto);

        HashMap<MyVector, Double> map = cache.distCache.get(from);
        if (map != null) {
            Double p = map.get(to);
            if (p != null) {
                cache.cacheUse++;
                return p;
            }
        }

        double distance = 0.0d;
        Waypoint[] path = getShortestPath(bot, from, to, cache);
        if (path == null) {
//			Dbg.err("Path is null at counting distance on map.");
            return Double.MAX_VALUE;
        }
        Vector3f pos = from;
        for (Waypoint wp : path) {
            distance += CommFun.getDistanceBetweenPositions(pos, wp.getObjectPosition());
            pos = wp.getObjectPosition();
        }

        if (map == null) {
            map = new HashMap<MyVector, Double>();
            cache.distCache.put(from, map);
        }
        map.put(to, distance);

        return distance;
    }

    private static Waypoint[] getShortestPath(MapBotBase bot, Vector3f vfrom, Vector3f vto, RankingCache cache) {
        cache.fnCalls++;
        MyVector from = new MyVector(vfrom);
        MyVector to = new MyVector(vto);

        HashMap<MyVector, Waypoint[]> map = cache.pathCache.get(from);
        if (map != null) {
            Waypoint[] p = map.get(to);
            if (p != null) {
                cache.cacheUse++;
                return p;
            }
        }

        Waypoint[] path = bot.kb.findShortestPath(from, to);

        if (map == null) {
            map = new HashMap<MyVector, Waypoint[]>();
            cache.pathCache.put(from, map);
        }
        map.put(to, path);

        return path;

    }


     private static float getEnemyCost(MapBotBase bot, Entity e, RankingCache c) {
        float riskyDistance = 1000;
        float cost = 0;

        Waypoint[] path = getShortestPath(bot, bot.getBotPosition(), e.getObjectPosition(), c);
        if (path == null) {
            return 0;
        }
        for (EnemyInfo en : bot.kb.getAllEnemyInformation()) {
            for (Waypoint w : path) {
//                if (!bot.getBsp().isVisible(w.getObjectPosition(), en.getObjectPosition())) {
//                    continue; TOOOO SLOOWW !!!!!!!
//                }
                float dist = CommFun.getDistanceBetweenPositions(w.getObjectPosition(), en.getObjectPosition());
                if (dist < riskyDistance) {
                    cost += 1 - dist / riskyDistance;
                }
            }
        }
        return cost;
    }

}
