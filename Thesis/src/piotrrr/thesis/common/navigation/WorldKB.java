package piotrrr.thesis.common.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.combat.EnemyInfo;
import piotrrr.thesis.common.entities.EntityDoublePair;
import piotrrr.thesis.common.entities.EntityType;
import piotrrr.thesis.tools.Dbg;
import soc.qase.ai.waypoint.Waypoint;
import soc.qase.ai.waypoint.WaypointMap;
import soc.qase.state.Entity;
import soc.qase.tools.vecmath.Vector3f;

/**
 * @author Piotr Gwizdała
 */
public class WorldKB {

    /**
     * Bot that owns this KB
     */
    MapBotBase bot = null;
    /**
     * The map that is used to navigate.
     */
    public WaypointMap map = null;
    /**
     * The black list of pickup things. If the bot decides to pickup something,
     * he shouldn't repeat this decision for some time, in case the thing can not be
     * picked up.
     */
    LinkedList<Integer> targetBlacklist;
    /**
     * Stores information on the enemies. Key=entity id, value=enemy info
     */
    public HashMap<Integer, EnemyInfo> enemyInformation = new HashMap<Integer, EnemyInfo>();
    /**
     * Entity number + the frame when it was seen last time. Used to determine if given entity may or not be active.
     */
    HashMap<Integer, Integer> entitySeenLastTime = new HashMap<Integer, Integer>();
    /**
     * A cache that is used to determine if given entity is reachable or not. Key=entity id, Val=true if reachable
     */
    HashMap<Integer, Boolean> entitiesReachability = new HashMap<Integer, Boolean>();
    /**
     * The maximum size of pickupBlaclist
     */
    static final int TARGET_BLACKLIST_MAX_SIZE = 8;

    private WorldKB(WaypointMap map, MapBotBase bot) {
        targetBlacklist = new LinkedList<Integer>();
        this.map = map;
        this.bot = bot;
    }

    /**
     * Adds all the entities that are on the map to KB.
     * @param map - the map
     * @return the knowledge base
     */
    public static WorldKB createKB(String mapPath, MapBotBase bot) {
        WaypointMap map = WaypointMap.loadMap(mapPath);
        Dbg.prn("Map path: " + mapPath);
        assert map != null;
        WorldKB ret = new WorldKB(map, bot);
        return ret;
    }

    /**
     * Returns the number of Entities being stored in KB.
     * @return
     */
    public int getKBSize() {
        return getAllItems().size();
    }

    /**
     * Returns all active entities of the specified type
     * @param et entity type
     * @param frameNumber the frame number at which those entities should be active.
     * @return
     */
    public Vector<Entity> getActiveEntitiesByType(EntityType et, long frameNumber) {
        Vector<Entity> ret = new Vector<Entity>();
        for (Object o : bot.getWorld().getEntities(false)) {
            Entity e = (Entity) o;
            EntityType itType = EntityType.getEntityType(e);
            if (!et.equals(itType)) {
                continue;
            }
            if (!isActive(e)) {
                continue;
            }
            if (e.isPlayerEntity() && e.getName().equals(bot.getBotName())) {
                continue;
            }
            if (targetBlacklist.contains(e.getNumber())) {
                continue;
            }
//			if (getPickupFailureCount(e) > MAX_PICKUP_FAILURE_COUNT) continue;
            if (!isEntityReachable(e, bot)) {
                continue;
            }
            ret.add(e);
        }
        return ret;
    }

    /**
     * @param pos position of reference
     * @param maxRange the maximal range from position of reference
     * @param currentFrame the frame at which the entity should be active
     * @return the list of entities that are active and within specified range from given position
     */
    public Vector<Entity> getActiveEntitiesWithinTheRange(Vector3f pos, int maxRange, long currentFrame) {
        Vector<Entity> ret = new Vector<Entity>();
        for (Object o : bot.getWorld().getEntities(false)) {
            Entity e = (Entity) o;
            if (!isPickableType(e)) {
                continue;
            }
            if (!isActive(e)) {
                continue;
            }
            if (targetBlacklist.contains(e.getNumber())) {
                continue;
            }
//			if (getPickupFailureCount(e) > MAX_PICKUP_FAILURE_COUNT) continue;
            if (!isEntityReachable(e, bot)) {
                continue;
            }
            double dist = CommFun.getDistanceBetweenPositions(pos, e.getObjectPosition());
            if (dist > maxRange) {
                continue;
            }
            ret.add(e);
        }
        return ret;

    }

    /**
     * @return all the items known to be in the world (active and inactive).
     */
    @SuppressWarnings("unchecked")
    public Vector<Entity> getAllItems() {
        Vector<Entity> items = new Vector<Entity>();
        items.addAll(bot.getWorld().getWeapons(false));
        items.addAll(bot.getWorld().getItems(false));
        return items;
    }

    /**
     * @return all the visible waypoints from current bot's position.
     */
    public Vector<Waypoint> getAllVisibleWaypoints() {
        Vector<Waypoint> ret = new Vector<Waypoint>();
        Waypoint[] wps = map.getAllNodes();
        for (Waypoint wp : wps) {
            if (bot.getBsp().isVisible(bot.getBotPosition(), wp.getObjectPosition())) {
                ret.add(wp);
            }
        }
        return ret;
    }

    /**
     * @return all visible entities for the bot.
     */
    public Vector<Entity> getAllVisibleEntities() {
        Vector<Entity> ret = new Vector<Entity>();
        for (Object o : bot.getWorld().getEntities(false)) {
            Entity e = (Entity) o;
            if (!bot.getBsp().isVisible(bot.getBotPosition(), e.getObjectPosition())) {
                continue;
            }
            ret.add(e);
        }
        return ret;
    }

    public Vector<Entity> getAllPickableEntities() {
        Vector<Entity> ret = new Vector<Entity>();
        for (Object o : bot.getWorld().getEntities(false)) {
            Entity e = (Entity) o;
            if (!isActive(e)) {
                continue;
            }
            if (!isPickableType(e)) {
                continue;
            }
            if (targetBlacklist.contains(e.getNumber())) {
                continue;
            }
//			if (getPickupFailureCount(e) > MAX_PICKUP_FAILURE_COUNT) continue;
            if (!isEntityReachable(e, bot)) {
                continue;
            }
            ret.add(e);
        }
        return ret;
    }

    /**
     * Adds the given entry to black-list
     * @param e
     */
    public void addToBlackList(Entity ent) {
        targetBlacklist.add(ent.getNumber());
        if (targetBlacklist.size() > TARGET_BLACKLIST_MAX_SIZE) {
            targetBlacklist.pop();
        }
        return;
    }

    /**
     * @param from the origin position
     * @param to the destination position
     * @return the shortest path using bot's map from origin to destination
     */
    public Waypoint[] findShortestPath(Vector3f from, Vector3f to) {
        return map.findShortestPath(from, to);
    }

    /**
     * @return some item from the world (active or not) tha can be used to
     * naviate to it, in case there is nothing else to do.
     */
    public Entity getSomeItem() {
        Vector<Entity> ents = getAllItems();
        if (!targetBlacklist.isEmpty()) {
            int ind = targetBlacklist.getFirst();
            for (Entity en : ents) {
                if (en.getNumber() == ind) {
                    return en;
                }
            }
        }
        Random r = new Random();
        return ents.get(r.nextInt(ents.size()));
    }

    /**
     * @return all entities that are unreachable
     */
    public Vector<Entity> getAllEntsWithPickupFailure() {
        Vector<Entity> ret = new Vector<Entity>();
        for (Entity e : getAllItems()) {
            if (!isEntityReachable(e, bot)) {
                ret.add(e);
            }
        }
        return ret;
    }

    /**
     * Checks whether the entity has a pickable type.
     * @param e
     * @return
     */
    private boolean isPickableType(Entity e) {
        if (e.getCategory().equalsIgnoreCase(Entity.CAT_ITEMS) ||
                e.getCategory().equalsIgnoreCase(Entity.CAT_WEAPONS)) {
            return true;
        }
        return false;
    }

    public void updateSeenEntities() {
        for (Entity e : getAllVisibleEntities()) {
            entitySeenLastTime.put(e.getNumber(), bot.getFrameNumber());
        }
    }

    public boolean isActive(Entity e) {
        if (e.getActive()) {
            return true; //if marked as active - ret true
        }
        if (!entitySeenLastTime.containsKey(e.getNumber())) {
            return false; //if it wasnt seen ever - false
        }
        int rt = e.getRespawnTime(); // otherwise get respawn time
        return (bot.getFrameNumber() >= entitySeenLastTime.get(e.getNumber()) + rt); //true if enough time has passed
    }

    /**
     * Updates the information on the enemies in the world
     */
    @SuppressWarnings("unchecked")
    public void updateEnemyInformation() {
        Vector<Integer> toDelete = new Vector<Integer>();
        Vector enems = bot.getWorld().getOpponents(true);

        for (Object o : enems) {
            Entity e = (Entity) o;
            if (enemyInformation.containsKey(e.getNumber())) {
                EnemyInfo ei = enemyInformation.get(e.getNumber());
                ei.updateEnemyInfo(e, bot.getFrameNumber());
            } else {
                enemyInformation.put(e.getNumber(), new EnemyInfo(e, bot.getFrameNumber()));
            }
        }

        for (EnemyInfo ei : enemyInformation.values()) {
            if ( /*! ei.ent.getActive() ||*/ei.isOutdated(bot.getFrameNumber())) {
                toDelete.add(ei.ent.getNumber());
            }
            if (!bot.friendlyFire && isFriend(ei)) {
                toDelete.add(ei.ent.getNumber());
            }
        }
//		Dbg.prn("enemies size: "+enemyInformation.size()+" to delete: "+toDelete.size()+" ina="+ina+" outd="+otd);
        for (Integer i : toDelete) {
            enemyInformation.remove(i);
        }

//		assert (enemyInformation.size() == 0) : "enems is not empty, huh? enems size="+enems.size();

    }

    public boolean isFriend(EnemyInfo ei) {
        try {
            String ep = ei.ent.getName();
            ep = ep.substring(0, ep.indexOf("-"));

            String fp = bot.getBotName();
            fp = fp.substring(0, fp.indexOf("-"));

            return ep.equals(fp);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return all known information about enemies
     */
    public Vector<EnemyInfo> getAllEnemyInformation() {
        Vector<EnemyInfo> ret = new Vector<EnemyInfo>();
        ret.addAll(enemyInformation.values());
        return ret;
    }

    @Override
    public String toString() {

        int edges = 0;
        if (map != null) {
            for (Waypoint w : map.getAllNodes()) {
                edges += w.getEdges().length;
            }
        }

        return "Knowledge base info: \n" +
                "size: " + getKBSize() + "\n" +
                "blacklist size: " + targetBlacklist.size() + "\n" +
                "enemy info size: " + getAllEnemyInformation().size() + "\n" +
                "nodes on map: " + map.getAllNodes().length + "\n" +
                "edges on map: " + edges + "\n" +
                "pickup failures size: " + getAllEntsWithPickupFailure().size() + "\n" +
                "seen ents size: " + entitySeenLastTime.size() + "\n";
    }

    public boolean isEntityReachable(Entity e, MapBotBase bot) {
        Boolean b = entitiesReachability.get(e.getNumber());
        if (b != null) {
            return b;
        }
        Waypoint wp = bot.kb.map.findClosestWaypoint(e.getObjectPosition());
        float dist = CommFun.getDistanceBetweenPositions(wp.getObjectPosition(), e.getObjectPosition());
        float obstDist = bot.getBsp().getObstacleDistance(wp.getObjectPosition(), e.getObjectPosition(), EnemyInfo.agentsHeight / 6, 2 * dist);
        if (obstDist < dist) {
            b = false;
        } else if (Math.abs(wp.getObjectPosition().z - e.getObjectPosition().z) > EnemyInfo.agentsHeight / 3) {
            b = false;
        } else {
            b = true;
        }

        entitiesReachability.put(e.getNumber(), b);
        return b;
    }
}
