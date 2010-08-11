package piotrrr.thesis.bots.referencebot;

import java.util.HashMap;
import java.util.TreeSet;

import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.bots.tuning.NavConfig;
import piotrrr.thesis.bots.tuning.WeaponConfig;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.combat.EnemyInfo;
import piotrrr.thesis.common.entities.EntityDoublePair;
import piotrrr.thesis.tools.Dbg;
import soc.qase.ai.waypoint.Waypoint;
import soc.qase.state.Entity;
import soc.qase.tools.vecmath.Vector3f;

public class ReferenceBotEntityRanking {

    private static class MyVector extends Vector3f {

         public MyVector(Vector3f v) {
            x = v.x;
            y = v.y;
            z = v.z;
        }

        @Override
        public int hashCode() {
            return (int)x;
        }
        
    }

    private static class RankingCache {

        HashMap<MyVector, HashMap<MyVector, Double>> distCache = new HashMap<MyVector, HashMap<MyVector, Double>>();
        HashMap<MyVector, HashMap<MyVector, Waypoint[]>> pathCache = new HashMap<MyVector, HashMap<MyVector, Waypoint[]>>();

        int cacheUse = 0;

        float maxEnCost = 1f;
        
        float maxDist = 1f;

        float maxBenefit = 1f;

        float maxNeed = 1f;

    }

    public static TreeSet<EntityDoublePair> getEntityFuzzyRanking(MapBotBase bot) {
        TreeSet<EntityDoublePair> ret = new TreeSet<EntityDoublePair>();
        RankingCache cache = new RankingCache();


        float maxEn = 0;
        float maxDist = 0;
        float maxBenef = 0;
        float maxNeed = 0;
        for (Entity ent : bot.kb.getAllPickableEntities()) {
            float ec = getEnemyCost(bot, ent, cache);
            if (ec > maxEn) {
                maxEn = ec;
            }
            float dist = (float) getDistanceFollowingMap(bot, bot.getBotPosition(), ent.getObjectPosition(), cache);
            if (dist > maxDist) {
                maxDist = dist;
            }
            float benef = getBotBenefitFuzzy(bot, ent, cache);
            if (benef > maxBenef) {
                maxBenef = benef;
            }
            float need = getBotNeedFuzzy(bot, ent, cache);
            if (need > maxNeed) {
                maxNeed = need;
            }
        }
        cache.maxBenefit = maxBenef;
        cache.maxDist = maxDist;
        cache.maxEnCost = maxEn;
        cache.maxNeed = maxNeed;

        for (Entity e : bot.kb.getAllPickableEntities()) {
            float rank = getEntityFuzzyRankingValue(bot, e, cache);
            ret.add(new EntityDoublePair(e, rank));
//            Dbg.prn("add: " + e.toString() + " rank: " + rank + " ranking-size: " + ret.size());
        }
//        Dbg.prn("Cache use = "+cache.cacheUse);
//        Dbg.prn("Chosen ent: "+ret.last().ent.toString()+" rank: "+ret.last().dbl);
        return ret;
    }

    public static String getRankingDebugInfo(MapBotBase bot) {
        RankingCache cache = new RankingCache();
        TreeSet<EntityDoublePair> ranking = getEntityFuzzyRanking(bot);
        int count = 0;
        String t = "\n";
        t += bot.getBotName() + ", h=" + bot.getBotHealth() + ", a=" + bot.getBotArmor();
        t += "\ndef-hlh=" + getBotHealthDeficiency(bot, 0);
        t += " def-arm=" + getBotArmorDeficiency(bot, 0);
        t += "\ndef-amm=" + getBotAmmoDeficiency(bot, 0);
        t += " def-wpn=" + getBotWeaponDeficiency(bot, 0);
        t += " ranking-size=" + ranking.size() + "\n";
        for (EntityDoublePair ed : ranking.descendingSet()) {
            count++;
            if (count > 5) {
                break;
            }
            t += "\n" + count + ": ent=" + ed.ent.toString() + " rank=" + ed.dbl;
            t += "\nben-hlh=" + getItemHealthBenefit(bot, ed.ent);
            t += " ben-arm=" + getItemArmorBenefit(bot, ed.ent);
            t += "\nben-amm=" + getItemAmmoBenefit(bot, ed.ent);
            t += " ben-wpn=" + getItemWeaponBenefit(bot, ed.ent);
            t += "\nenemy-cost=" + getEnemyCost(bot, ed.ent, cache);
            t += "\ndistance=" + getDistanceFollowingMap(bot, bot.getBotPosition(), ed.ent.getObjectPosition(), cache);
            t += "\n";

            Entity e = ed.ent;
            NavConfig c = bot.nConfig;

            t += "\ndef-hlh=" + getBotHealthDeficiency(bot, 0);
            t += " def-arm=" + getBotArmorDeficiency(bot, 0);
            t += " def-amm=" + getBotAmmoDeficiency(bot, 0);
            t += " def-wpn=" + getBotWeaponDeficiency(bot, 0);
            t += "\nneed=" + getBotNeedFuzzy(bot, e, cache) + " benef=" + getBotBenefitFuzzy(bot, e, cache) + " safety=" + getSafetyFuzzy(bot, e, cache) + " closeness=" + getCloseDistanceFuzzy(bot, e,cache);
            t += "\n";
            //            t += e.toString() + ":\n h=" + ftoperc(h / rank) + "% arm=" + ftoperc(arm / rank) + "% w=" + ftoperc(w / rank) +
//                    "% amm=" + ftoperc(amm / rank) + "% ec=" + ftoperc(ec / rank) + "% dist=" + ftoperc(dist / rank) + "%";


        }
        return t;
    }

    private static int ftoperc(float f) {
        return (int) (f * 100);
    }

    /**
     * Gets the fuzzy value of the relation 'bot wants the item'
     * @param bot
     * @param e
     * @return
     */
    private static float getEntityFuzzyRankingValue(MapBotBase bot, Entity e, RankingCache c) {
        float rank = Math.min(Math.min(Math.min(getBotNeedFuzzy(bot, e, c), getBotBenefitFuzzy(bot, e, c)), getSafetyFuzzy(bot, e, c)), getCloseDistanceFuzzy(bot, e,c));
//        System.out.println("rank="+rank+" ent="+e.toString());
//        System.out.println("need="+getBotNeedFuzzy(bot, e, c)+" benef="+getBotBenefitFuzzy(bot, e, c)+" safe="+getSafetyFuzzy(bot, e, c)+" dist="+getCloseDistanceFuzzy(bot, e, c)+"\n");
        return rank;
    }

    /**
     * Returns the fuzzy value of the relation 'bot needs item of given item category'
     * @param bot
     * @param e
     * @return
     */
    public static float getBotNeedFuzzy(MapBotBase bot, Entity e, RankingCache c) {
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
        return ret / c.maxNeed;
    }

    /**
     * Returns the fuzzy value of the relation 'bot will benefit from item e'
     * @param bot
     * @param e
     * @return
     */
    public static float getBotBenefitFuzzy(MapBotBase bot, Entity e, RankingCache c) {
        float ret = 0;
        if (e.getCategory().equals(Entity.CAT_WEAPONS)) {
            ret= getItemWeaponBenefit(bot, e);
        } else if (e.getCategory().equals(Entity.CAT_ITEMS)) {
            if (e.getType().equals(Entity.TYPE_AMMO)) {
                ret= getItemAmmoBenefit(bot, e);
            } else if (e.getType().equals(Entity.TYPE_HEALTH) || e.getType().equals(Entity.TYPE_MEGAHEALTH)) {
                ret= getItemHealthBenefit(bot, e);
            } else if (e.getType().equals(Entity.TYPE_ARMOR)) {
                ret= getItemArmorBenefit(bot, e);
            }
        }
        return ret / c.maxBenefit;
    }

    /**
     * Returns the fuzzy value of the relation 'it is safe to go for item'
     * @param bot
     * @param e
     * @param maxEnCost
     * @return
     */
    public static float getSafetyFuzzy(MapBotBase bot, Entity e, RankingCache c) {
        if (c.maxEnCost == 0) return 1; //if nothing has enemy cost - all ents are safe
        return 1 - getEnemyCost(bot, e, c) / c.maxEnCost;
    }

    /**
     * Returns the fuzzy value of the relation 'the item is close to the bot'
     * @param bot
     * @param e
     * @param maxDistance
     * @return
     */
    public static float getCloseDistanceFuzzy(MapBotBase bot, Entity e, RankingCache c) {
        float dist = (float) getDistanceFollowingMap(bot, bot.getBotPosition(), e.getObjectPosition(), c);
        float ret = dist / c.maxDist;
        return ret;
    }

    public static float getEnemyCost(MapBotBase bot, Entity e, RankingCache c) {
        float riskyDistance = 1000;
        float cost = 0;

        Waypoint[] path = getShortestPath(bot, bot.getBotPosition(), e.getObjectPosition(), c);
        if (path == null) {
            return 0;
        }
        for (EnemyInfo en : bot.kb.getAllEnemyInformation()) {
            for (Waypoint w : path) {
                if (!bot.getBsp().isVisible(w.getObjectPosition(), en.getObjectPosition())) {
                    continue;
                }
                float dist = CommFun.getDistanceBetweenPositions(w.getObjectPosition(), en.getObjectPosition());
                if (dist < riskyDistance) {
                    cost += 1 - dist / riskyDistance;
                }
            }
        }
        return cost;
    }

    public static float getBotHealthDeficiency(MapBotBase bot, int addedHealth) {
        float h = bot.getBotHealth() + addedHealth;
        if (h > BotBase.maxHealth) {
            h = BotBase.maxHealth;
        }
        return 1f - h / (float) BotBase.maxHealth;
    }

    public static float getBotArmorDeficiency(MapBotBase bot, int addedArmor) {
        float a = bot.getBotArmor() + addedArmor;
        if (a > BotBase.maxArmor) {
            a = BotBase.maxArmor;
        }
        return 1f - a / (float) BotBase.maxArmor;
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

        float ret = 1f - ownedWeapons / (weightSum * NavConfig.weaponDeficiencyTolerance);
        if (ret < 0) {
            ret = 0;
        }
        return ret;

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
            ownedAmmo += bot.wConfig.getAmmoWeightByInventoryIndex(addedAmmoIndex) *
                    bot.getAmmunitionState(getWeaponFromAmmoIndex(bot, addedAmmoIndex));
        }

//		Dbg.prn("owned ammo: "+ownedAmmo+" max ammo: "+maxAmmo);
        float ret = 1f - ownedAmmo / maxAmmo;
        if (ret < 0) {
            ret = 0;
        }
        return ret;

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
        MyVector from = new MyVector(vfrom);
        MyVector to = new MyVector(vto);

        if (cache.distCache.containsKey(from)) {
            if (cache.distCache.get(from).containsKey(to)) {
                cache.cacheUse++;
                return cache.distCache.get(from).get(to);
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

        if (!cache.distCache.containsKey(from)) {
            cache.distCache.put(from, new HashMap<MyVector, Double>());
        }
        cache.distCache.get(from).put(to, distance);

        return distance;
    }

    private static Waypoint[] getShortestPath(MapBotBase bot, Vector3f vfrom, Vector3f vto, RankingCache cache) {
        MyVector from = new MyVector(vfrom);
        MyVector to = new MyVector(vto);
        
        if (cache.pathCache.containsKey(from)) {
            if (cache.pathCache.get(from).containsKey(to)) {
                cache.cacheUse++;
                return cache.pathCache.get(from).get(to);
            }
        }
        Waypoint[] path  = bot.kb.findShortestPath(from, to);
        if (!cache.pathCache.containsKey(from)) {
            cache.pathCache.put(from, new HashMap<MyVector, Waypoint[]>());
        }
        cache.pathCache.get(from).put(to, path);

        return path;

    }
}
