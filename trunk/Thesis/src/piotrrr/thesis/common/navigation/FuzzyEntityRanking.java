package piotrrr.thesis.common.navigation;

import java.util.HashMap;
import java.util.LinkedList;
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

public class FuzzyEntityRanking {

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

    private static class EntityFuzzyVals {

        float ec;
        float dist;
        float benef;
        float need;
        Entity e;

        public EntityFuzzyVals(float ec, float dist, float benef, float need, Entity e) {
            this.ec = ec;
            this.dist = dist;
            this.benef = benef;
            this.need = need;
            this.e = e;
        }
    }

    private static class Maximums {

        float maxEnCost = 1f;
        float maxDist = 1f;
        float maxBenefit = 1f;
        float maxNeed = 1f;
    }

    public static TreeSet<EntityDoublePair> getEntityFuzzyRanking(MapBotBase bot) {
        TreeSet<EntityDoublePair> ret = new TreeSet<EntityDoublePair>();
        LinkedList<EntityFuzzyVals> tmpList = new LinkedList<EntityFuzzyVals>();
        RankingCache cache = new RankingCache();

        Maximums m = new Maximums();

        for (Entity ent : bot.kb.getAllPickableEntities()) {
//            bot.timers.get("nav1").resume();
            float ec = getEnemyCostFuzzy(bot, ent, cache, 1);
            if (ec > m.maxEnCost) {
                m.maxEnCost = ec;
            }
//            bot.timers.get("nav1").pause();
//            bot.timers.get("nav2").resume();
            float dist = getCloseDistanceFuzzy(bot, ent, cache, 1);
            if (dist > m.maxDist) {
                m.maxDist = dist;
            }
//            bot.timers.get("nav2").pause();
//            bot.timers.get("nav3").resume();
            float benef = getBotBenefitFuzzy(bot, ent, cache, 1);
            if (benef > m.maxBenefit) {
                m.maxBenefit = benef;
            }
//            bot.timers.get("nav3").pause();
//            bot.timers.get("nav4").resume();
            float need = getBotNeedFuzzy(bot, ent, cache, 1);
            if (need > m.maxNeed) {
                m.maxNeed = need;
            }
//            bot.timers.get("nav4").pause();

            tmpList.add(new EntityFuzzyVals(ec, dist, benef, need, ent));
        }

//        bot.timers.get("nav0").resume();
        for (EntityFuzzyVals efv : tmpList) {
            efv.ec = 1 - efv.ec; //we negate the enemy cost, as we want it to be low
            float fuzzyVal = fuzzyLogicalAnd4(efv.benef, efv.dist, efv.ec, efv.need);
            ret.add(new EntityDoublePair(efv.e, fuzzyVal));
        }
//        bot.timers.get("nav0").pause();
//        Dbg.prn("Cache use = "+(100d*(double)cache.cacheUse) / (double)cache.fnCalls);
//        Dbg.prn("Chosen ent: "+ret.last().ent.toString()+" rank: "+ret.last().dbl);
        return ret;
    }

    public static TreeSet<EntityDoublePair> getEnemyRetreatFuzzyRanking(MapBotBase bot) {
        TreeSet<EntityDoublePair> ret = new TreeSet<EntityDoublePair>();
        LinkedList<EntityFuzzyVals> tmpList = new LinkedList<EntityFuzzyVals>();
        RankingCache cache = new RankingCache();

        Maximums m = new Maximums();

        for (Entity ent : bot.kb.getAllPickableEntities()) {

            float ec = getEnemyCostFuzzy(bot, ent, cache, 1);
            if (ec > m.maxEnCost) {
                m.maxEnCost = ec;
            }

            tmpList.add(new EntityFuzzyVals(ec, 0f, 0f, 0f, ent));
        }

//        bot.timers.get("nav0").resume();
        for (EntityFuzzyVals efv : tmpList) {
            efv.ec = 1 - efv.ec; //we negate the enemy cost, as we want it to be low
            float fuzzyVal = efv.ec;
            ret.add(new EntityDoublePair(efv.e, fuzzyVal));
        }
//        bot.timers.get("nav0").pause();
//        Dbg.prn("Cache use = "+(100d*(double)cache.cacheUse) / (double)cache.fnCalls);
//        Dbg.prn("Chosen ent: "+ret.last().ent.toString()+" rank: "+ret.last().dbl);
        return ret;
    }

    public static float fuzzyLogicalAnd4(float a, float b, float c, float d) {
        return Math.min(a, Math.min(b, Math.min(c, d)));
    }

//    public static String getRankingDebugInfo(MapBotBase bot) {
//        RankingCache cache = new RankingCache();
//        TreeSet<EntityDoublePair> ranking = getEntityFuzzyRanking(bot);
//        int count = 0;
//        String t = "\n";
//        t += bot.getBotName() + ", h=" + bot.getBotHealth() + ", a=" + bot.getBotArmor();
//        t += "\ndef-hlh=" + getBotHealthDeficiency(bot, 0);
//        t += " def-arm=" + getBotArmorDeficiency(bot, 0);
//        t += "\ndef-amm=" + getBotAmmoDeficiency(bot, 0);
//        t += " def-wpn=" + getBotWeaponDeficiency(bot, 0);
//        t += " ranking-size=" + ranking.size() + "\n";
//        for (EntityDoublePair ed : ranking.descendingSet()) {
//            count++;
//            if (count > 5) {
//                break;
//            }
//            t += "\n" + count + ": ent=" + ed.ent.toString() + " rank=" + ed.dbl;
//            t += "\nben-hlh=" + getItemHealthBenefit(bot, ed.ent);
//            t += " ben-arm=" + getItemArmorBenefit(bot, ed.ent);
//            t += "\nben-amm=" + getItemAmmoBenefit(bot, ed.ent);
//            t += " ben-wpn=" + getItemWeaponBenefit(bot, ed.ent);
//            t += "\nenemy-cost=" + getEnemyCost(bot, ed.ent, cache);
//            t += "\ndistance=" + getDistanceFollowingMap(bot, bot.getBotPosition(), ed.ent.getObjectPosition(), cache);
//            t += "\n";
//
//            Entity e = ed.ent;
//            NavConfig c = bot.nConfig;
//
//            t += "\ndef-hlh=" + getBotHealthDeficiency(bot, 0);
//            t += " def-arm=" + getBotArmorDeficiency(bot, 0);
//            t += " def-amm=" + getBotAmmoDeficiency(bot, 0);
//            t += " def-wpn=" + getBotWeaponDeficiency(bot, 0);
//            t += "\nneed=" + getBotNeedFuzzy(bot, e, cache) + " benef=" + getBotBenefitFuzzy(bot, e, cache) + " safety=" + getSafetyFuzzy(bot, e, cache) + " closeness=" + getCloseDistanceFuzzy(bot, e,cache);
//            t += "\n";
//            //            t += e.toString() + ":\n h=" + ftoperc(h / rank) + "% arm=" + ftoperc(arm / rank) + "% w=" + ftoperc(w / rank) +
////                    "% amm=" + ftoperc(amm / rank) + "% ec=" + ftoperc(ec / rank) + "% dist=" + ftoperc(dist / rank) + "%";
//
//
//        }
//        return t;
//    }
    private static int ftoperc(float f) {
        return (int) (f * 100);
    }

    /**
     * Gets the fuzzy value of the relation 'bot wants the item'
     * @param bot
     * @param e
     * @return
     */
    /**
     * Returns the fuzzy value of the relation 'bot needs item of given item category'
     * @param bot
     * @param e
     * @return
     */
    private static float getBotNeedFuzzy(MapBotBase bot, Entity e, RankingCache c, float maxNeed) {
        float ret = 0;
        if (e.getCategory().equals(Entity.CAT_WEAPONS)) {
            ret = getBotWeaponDeficiency(bot, 0);
        } else if (e.getCategory().equals(Entity.CAT_ITEMS)) {
            if (e.getType().equals(Entity.TYPE_AMMO)) {
                ret = getBotAmmoDeficiency(bot, 0);
            } else if (e.getType().equals(Entity.TYPE_HEALTH) || e.getType().equals(Entity.TYPE_MEGAHEALTH)) {
                ret = getBotHealthDeficiency(bot, 0);
            } else if (e.getType().equals(Entity.TYPE_ARMOR)) {
                ret = getBotArmorDeficiency(bot, 0);
            }
        }
        return ret / maxNeed;
    }

    /**
     * Returns the fuzzy value of the relation 'bot will benefit from item e'
     * @param bot
     * @param e
     * @return
     */
    private static float getBotBenefitFuzzy(MapBotBase bot, Entity e, RankingCache c, float maxBenefit) {
        float ret = 0;
        if (e.getCategory().equals(Entity.CAT_WEAPONS)) {
            ret = getItemWeaponBenefit(bot, e);
        } else if (e.getCategory().equals(Entity.CAT_ITEMS)) {
            if (e.getType().equals(Entity.TYPE_AMMO)) {
                ret = getItemAmmoBenefit(bot, e);
            } else if (e.getType().equals(Entity.TYPE_HEALTH) || e.getType().equals(Entity.TYPE_MEGAHEALTH)) {
                ret = getItemHealthBenefit(bot, e);
            } else if (e.getType().equals(Entity.TYPE_ARMOR)) {
                ret = getItemArmorBenefit(bot, e);
            }
        }
        return ret / maxBenefit;
    }

//    /**
//     * Returns the fuzzy value of the relation 'it is safe to go for item'
//     * @param bot
//     * @param e
//     * @param maxEnCost
//     * @return
//     */
//    public static float getSafetyFuzzy(MapBotBase bot, Entity e, RankingCache c) {
//        if (c.maxEnCost == 0) return 1; //if nothing has enemy cost - all ents are safe
//        return 1 - getEnemyCost(bot, e, c) / c.maxEnCost;
//    }
    /**
     * Returns the fuzzy value of the relation 'the item is close to the bot'
     * @param bot
     * @param e
     * @param maxDistance
     * @return
     */
    private static float getCloseDistanceFuzzy(MapBotBase bot, Entity e, RankingCache c, float maxDist) {
        float dist = (float) getDistanceFollowingMap(bot, bot.getBotPosition(), e.getObjectPosition(), c);
        float ret = dist / maxDist;
        return ret;
    }

    private static float getEnemyCostFuzzy(MapBotBase bot, Entity e, RankingCache c, float maxEnemyCost) {
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
        return cost / maxEnemyCost;
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

    public static float getMaximalDeficiency(MapBotBase b) {
        return Math.max(
                Math.max(getBotAmmoDeficiency(b, 0), getBotArmorDeficiency(b, 0)),
                Math.max(getBotHealthDeficiency(b, 0), getBotWeaponDeficiency(b, 0)));
    }
}
