package piotrrr.thesis.common.navigation;

import java.util.HashMap;
import java.util.TreeSet;

import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.bots.tuning.WeaponConfig;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.combat.EnemyInfo;
import piotrrr.thesis.common.entities.EntityDoublePair;
import piotrrr.thesis.gui.AppConfig;
import piotrrr.thesis.tools.Dbg;
import soc.qase.ai.waypoint.Waypoint;
import soc.qase.state.Entity;
import soc.qase.tools.vecmath.Vector3f;

public class FuzzyEntityRanking {

    private static float botMaxHealth = 100f;
    private static float botMaxArmor = 200f;
    private static float botMaxAmmo = Float.NaN;
    private static float botMaxWeapons = Float.NaN;

    private static float yagerWParam = 2;

    private static void setMaxAmmoAndMaxWeapns(MapBotBase bot) {

        botMaxAmmo = 0;
        botMaxWeapons = 0;

        for (int am : bot.wConfig.usableAmmo) {
            botMaxAmmo += bot.wConfig.getAmmoWeightByInventoryIndex(am);
        }

        for (int w : bot.wConfig.usableWeapons) {
            botMaxWeapons += bot.wConfig.getWeaponWeightByInvIndex(w);
        }
    }

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

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MyVector other = (MyVector) obj;
            if (x == other.x && y == other.y && z == other.z) {
                return true;
            } else {
                return false;
            }
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
        double dist = 0;
        double enemyCost = 0;
    }

    public static TreeSet<EntityDoublePair> getEntityFuzzyRanking(MapBotBase bot) {

        if (Float.isNaN(botMaxAmmo) || Float.isNaN(botMaxWeapons)) {
            Dbg.prn("setting max ammo and max weapons");
            setMaxAmmoAndMaxWeapns(bot);
        }

        TreeSet<EntityDoublePair> ret = new TreeSet<EntityDoublePair>();
        RankingCache cache = new RankingCache();
        HashMap<Entity, Measures> em = new HashMap<Entity, Measures>();

        double maxDist = 0;
        double maxEnemyCost = 0;
        double maxHealthBen = -10;
        double maxArmorBen = -10;
        double maxWeaponBen = -10;
        double maxAmmoBen = -10;

        //get measures and maximums
        for (Entity ent : bot.kb.getAllPickableEntities()) {
            Measures m = getMeasures(bot, ent, cache);
            em.put(ent, m);
            if (m.enemyCost > maxEnemyCost) {
                maxEnemyCost = m.enemyCost;
            }
            if (!Double.isNaN(m.dist) && m.dist > maxDist) {
                maxDist = m.dist;
            }
            if (m.healthBen > maxHealthBen) {
                maxHealthBen = m.healthBen;
            }
            if (m.armorBen > maxArmorBen) {
                maxArmorBen = m.armorBen;
            }
            if (m.weaponBen > maxWeaponBen) {
                maxWeaponBen = m.weaponBen;
            }
            if (m.ammoBen > maxAmmoBen) {
                maxAmmoBen = m.ammoBen;
            }
        }

        if (maxHealthBen <= 0) {
            maxHealthBen = 1;
        }
        if (maxArmorBen <= 0) {
            maxArmorBen = 1;
        }
        if (maxWeaponBen <= 0) {
            maxWeaponBen = 1;
        }
        if (maxAmmoBen <= 0) {
            maxAmmoBen = 1;
        }


        float hd = getBotHealthDeficiency(bot, 0);
        float ard = getBotArmorDeficiency(bot, 0);
        float amd = getBotAmmoDeficiency(bot, 0);
        float wd = getBotWeaponDeficiency(bot, 0);

        for (Entity e : em.keySet()) {
            Measures m = em.get(e);
            double rank = 0;

            boolean err = false;

            String dbg = bot.getBotName() + "> " + e.toString() + " ";

            //Weights constant, independent from context
            if (e.getType().equals(Entity.TYPE_HEALTH)) {
                rank = bot.nConfig.weight_health.getValue() * hd;
                rank = fuzzyAnd(rank, bot.nConfig.weight_health_ben.getValue() * m.healthBen / maxHealthBen);
                dbg += "hd=" + bot.nConfig.weight_health.getValue()*hd + " hb=" + bot.nConfig.weight_health_ben.getValue() * m.healthBen / maxHealthBen;
            } else if (e.getType().equals(Entity.TYPE_ARMOR)) {
                rank = bot.nConfig.weight_armor.getValue() * ard;
                rank = fuzzyAnd(rank, bot.nConfig.weight_armor_ben.getValue() * m.armorBen / maxArmorBen);
                dbg += "ard=" + bot.nConfig.weight_armor.getValue() * ard + " arb=" + bot.nConfig.weight_armor_ben.getValue() * m.armorBen / maxArmorBen;
            } else if (e.isWeaponEntity()) {
                rank = bot.nConfig.weight_weapon.getValue() * wd;
                rank = fuzzyAnd(rank, bot.nConfig.weight_weapon_ben.getValue() * m.weaponBen / maxWeaponBen);
                dbg += "wd=" + bot.nConfig.weight_weapon.getValue() * wd + " wb=" + bot.nConfig.weight_weapon_ben.getValue() * m.weaponBen / maxWeaponBen;
            } else if (e.getType().equals(Entity.TYPE_AMMO)) {
                rank = bot.nConfig.weight_ammo.getValue() * amd;
                rank = fuzzyAnd(rank, bot.nConfig.weight_ammo_ben.getValue() * m.ammoBen / maxAmmoBen);
                dbg += "ad=" + bot.nConfig.weight_ammo.getValue() * amd + " ab=" + bot.nConfig.weight_ammo_ben.getValue() * m.ammoBen / maxAmmoBen;
            }

            if (!err && Double.isNaN(rank)) {
                Dbg.prn("Rank NaN after w*(def+ben) --> Entity:" + e.toString());
                Dbg.prn("ard=" + ard + " wd=" + wd + " m.armorBen=" + m.armorBen + " m.weapnBen=" + m.weaponBen);
                Dbg.prn("botMaxWeapons=" + botMaxWeapons + " maxArmorBen=" + maxArmorBen);
                Dbg.prn("");
                err = true;
            }

            //weights depending on context
            if (maxEnemyCost != 0) {
                rank = fuzzyAnd(rank, 1 - bot.nConfig.weight_enemycost.getValue() * m.enemyCost / maxEnemyCost);
                dbg += " ec=" + bot.nConfig.weight_enemycost.getValue() * m.enemyCost / maxEnemyCost;
            }

            if (!Double.isNaN(m.dist)) {
                rank = fuzzyAnd(rank, 1 - bot.nConfig.weight_distance.getValue() * m.dist / maxDist);
                dbg += " dist=" + bot.nConfig.weight_distance.getValue() * m.dist / maxDist;
                if (!err && Double.isNaN(rank)) {
                    Dbg.prn("Rank NaN after dist");
                    err = true;
                }
            }


            if (!err && Double.isNaN(rank)) {
                Dbg.prn("Rank NaN after enemy cost");
                Dbg.prn("m.enemyCost=" + m.enemyCost + " maxEnemyCost=" + maxEnemyCost);
                err = true;
            }

            dbg += " rank=" + rank;

            if (AppConfig.debug) Dbg.prn(dbg);

            ret.add(new EntityDoublePair(e, rank));
        }

        cache.distCache.clear();
        cache.pathCache.clear();
        return ret;
    }

    public static float getWeigthedDeficiency(MapBotBase bot) {

        if (botMaxAmmo == Float.NaN || botMaxWeapons == Float.NaN) {
            setMaxAmmoAndMaxWeapns(bot);
        }

        float hd = getBotHealthDeficiency(bot, 0);
        float ard = getBotArmorDeficiency(bot, 0);
        float amd = getBotAmmoDeficiency(bot, 0);
        float wd = getBotWeaponDeficiency(bot, 0);

        double st = fuzzyOr(bot.nConfig.weight_health.getValue() * hd, bot.nConfig.weight_armor.getValue() * ard);
        st = fuzzyOr(st, bot.nConfig.weight_armor.getValue() * amd);
        st = fuzzyOr(st, bot.nConfig.weight_weapon.getValue() * wd);
        return (float) st;
    }

    private static Measures getMeasures(MapBotBase bot, Entity e, RankingCache cache) {

        Measures m = new Measures();

        if (e.getType().equals(Entity.TYPE_HEALTH)) {
            m.healthBen = getItemHealthBenefit(bot, e);
        } else if (e.getType().equals(Entity.TYPE_ARMOR)) {
            m.armorBen = getItemArmorBenefit(bot, e);
        } else if (e.isWeaponEntity()) {
            m.weaponBen = getItemWeaponBenefit(bot, e);
        } else if (e.getType().equals(Entity.TYPE_AMMO)) {
            m.ammoBen = getItemAmmoBenefit(bot, e);
        }

        m.dist = getDistanceFollowingMap(bot, bot.getBotPosition(), e.getObjectPosition(), cache);
        m.enemyCost = getEnemyCost(bot, e, cache);

        return m;
    }

    private static double fuzzyAnd(double a, double b) {
        double aToW = 1;
        double bToW = 1;
        for (int i=0; i<yagerWParam; i++) {
            aToW *= a;
            bToW *= b;
        }
        double abToWToW = 1;
        for (int i=0; i<yagerWParam; i++) {
            abToWToW *= (aToW+bToW);
        }
        return Math.min(1, abToWToW);
    }

    private static double fuzzyOr(double a, double b) {
        double maToW = 1;
        double mbToW = 1;
        for (int i=0; i<yagerWParam; i++) {
            maToW *= 1-a;
            mbToW *= 1-b;
        }
        double wRootMaMbToW = Math.pow(maToW+mbToW, 1.0/yagerWParam);
        return 1.0 - Math.min(1, wRootMaMbToW);
    }

    private static float getBotHealthDeficiency(MapBotBase bot, int addedHealth) {
        float h = bot.getBotHealth() + addedHealth;
        return 1f - h / botMaxHealth;
    }

    private static float getBotArmorDeficiency(MapBotBase bot, int addedArmor) {
        float a = bot.getBotArmor() + addedArmor;
        return 1f - a / botMaxArmor;
    }

    private static float getBotWeaponDeficiency(MapBotBase bot, int addedWeaponIndex) {
        /**
        int BLASTER = 7, SHOTGUN = 8,
        SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
        GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
        RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
        ROCKETS = 21, SLUGS = 22;
         **/
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

        return 1f - ownedWeapons / botMaxWeapons;

    }

    private static float getBotAmmoDeficiency(MapBotBase bot, int addedAmmoIndex) {
        /**
        int BLASTER = 7, SHOTGUN = 8,
        SUPER_SHOTGUN = 9, MACHINEGUN = 10, CHAINGUN = 11, GRENADES = 12,
        GRENADE_LAUNCHER = 13, ROCKET_LAUNCHER = 14, HYPERBLASTER = 15,
        RAILGUN = 16, BFG10K = 17, SHELLS = 18, BULLETS = 19, CELLS = 20,
        ROCKETS = 21, SLUGS = 22;
         **/
        float ownedAmmo = 0;

        for (int am : bot.wConfig.usableAmmo) {
            if (bot.botHasItem(am)) {
                ownedAmmo += bot.wConfig.getAmmoWeightByInventoryIndex(am) *
                        bot.getAmmunitionState(getWeaponFromAmmoIndex(bot, am));
            }
        }

        if (addedAmmoIndex >= 18 && addedAmmoIndex <= 22) {
            //FIXME: 0.3 is not the usual size of ammo pack...
            ownedAmmo += bot.wConfig.getAmmoWeightByInventoryIndex(addedAmmoIndex) * 0.3;
        }

        return 1f - ownedAmmo / botMaxAmmo;
    }

    private static float getItemHealthBenefit(MapBotBase bot, Entity e) {

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

        if (growth == 0) {
            return 0;
        }

        float before = getBotHealthDeficiency(bot, 0);
        float after = getBotHealthDeficiency(bot, growth);
        return (before - after);
    }

    private static float getItemArmorBenefit(MapBotBase bot, Entity e) {

        int growth = 0;

        if (e.getType().equals(Entity.TYPE_ARMOR)) {
            if (e.getSubType().equals("jacket")) {
                growth = 25;
            } else if (e.getSubType().equals("combat")) {
                growth = 50;
            } else if (e.getSubType().equals("body")) {
                growth = 100;
            } else if (e.getSubType().equals("shard")) {
                growth = 1;
            }

            float before = getBotArmorDeficiency(bot, 0);
            float after = getBotArmorDeficiency(bot, growth);
//            Dbg.prn("type="+e.getType()+" subtype="+e.getSubType()+" cat="+e.getCategory());
//            Dbg.prn("armor growth="+growth+" before-after="+(before - after)+" aftet="+after);
            return (before - after);

        }
        return 0;
    }

    private static float getItemWeaponBenefit(MapBotBase bot, Entity e) {

        if (e.isWeaponEntity()) {
            int ind = e.getInventoryIndex();
            float before = getBotWeaponDeficiency(bot, 0);
            float after = getBotWeaponDeficiency(bot, ind);
            return (before - after);
        }
        return 0;
    }

    private static float getItemAmmoBenefit(MapBotBase bot, Entity e) {
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
            return Double.NaN;
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
