package piotrrr.thesis.common.navigation;

import java.util.Random;
import piotrrr.thesis.bots.referencebot.*;
import java.util.TreeSet;
import java.util.Vector;

import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.bots.tuning.NavConfig;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.combat.EnemyInfo;
import piotrrr.thesis.common.entities.EntityDoublePair;
import piotrrr.thesis.common.entities.EntityType;
import piotrrr.thesis.common.navigation.GlobalNav;
import piotrrr.thesis.common.navigation.NavPlan;
import piotrrr.thesis.gui.AppConfig;
import piotrrr.thesis.tools.Dbg;
import soc.qase.ai.waypoint.Waypoint;
import soc.qase.state.Entity;
import soc.qase.tools.vecmath.Vector3f;

/**
 * The global navigation module of the MapBotBase.
 * @author Piotr Gwizdała
 * @see MapBotBase
 */
public class FuzzyGlobalNav implements GlobalNav {

    static double mainDecisionTimeout = 40;
    static double spontDecisionTimeout = 20;
    static double antiStuckDecisionTimeout = 10;
    static int maximalDistance = 400;

    /**
     * Returns the new plan that the bot should follow
     * @param bot the bot for which the plan is being established
     * @param oldPlan the bot's old plan
     * @return the new plan for the bot (can be the same as the oldPlan)
     */
    @Override
    public NavPlan establishNewPlan(MapBotBase bot, NavPlan oldPlan) {

        mainDecisionTimeout = 40;
        spontDecisionTimeout = 20;
        antiStuckDecisionTimeout = 10;

        /**
         * When do we change the plan?
         * + when we don't have plan
         * + when we accomplish the old one
         * - when the state was changed (fitness or firepower)
         * + when the bot is stuck
         * + when the decision times out....?
         * - when the enemy appears
         * + when the bot has good pickup opportunity
         * + when it has died
         */
        boolean changePlan = false;

		String talk = "";

        if (oldPlan == null) {
            changePlan = true;
			talk = "plan change: no plan!";
        } else if (oldPlan.done) {
            changePlan = true;
            bot.kb.addToBlackList(oldPlan.dest);
			talk = "plan change: old plan is done!";
        } //if the bot is stuck
        else if (bot.stuckDetector.isStuck) {
//            bot.dtalk.addToLog("plan change: bot is stuck !");
            bot.stuckDetector.reset();
            return getSpontaneousAntiStuckPlan(bot);
        } //if we timed out with the plan
        else if (oldPlan.deadline <= bot.getFrameNumber()) {
//            bot.dtalk.addToLog("plan change: old plan timed out!");
            changePlan = true;
        }

//		if (talk != "" ) bot.dtalk.addToLog(talk);

        /**
         * What do we do when we decide to change the plan?
         * + if we see something that we can pick up in spontaneous decision - we pick it
         * + if we don't do spontaneous pickup, we get the entities from KB basing on bot's state and
         * choose one of them and create the plan to reach it.
         */
        NavPlan plan = null; // the plan to return

        //If we didn't want to change the plan, we may still do it if there is some good spontaneous plan.
        if (!changePlan && oldPlan != null && !oldPlan.isSpontaneos) {
            plan = getSpontaneousPlan(bot);
        }
        if (plan != null) {
            return plan;
        }

        //If no spontaneous plans available, we continue with old one...
        if (!changePlan) {
            return oldPlan;
        }


        if (NavConfig.aggressiveness > FuzzyEntityRanking.getMaximalDeficiency(bot)) {
            plan = getEnemyAttackingPlan(bot, oldPlan);

            if (plan != null) {
//                bot.dtalk.addToLog("GOING TO ATTTACK !!!");
                return plan;
            }
        }

        //Get the entity ranking:
        bot.timers.get("rank").resume();
        TreeSet<EntityDoublePair> ranking = FuzzyEntityRanking.getEntityFuzzyRanking(bot);
        bot.timers.get("rank").pause();
//		bot.dtalk.addToLog(ReferenceBotEntityRanking.getRankingDebugInfo(bot));

        while ((plan == null || plan.path == null)) {

            if (ranking.size() == 0 || bot.stuckDetector.isStuck) {
                Entity wp = bot.kb.getSomeItem();
//				double distance = getDistanceFollowingMap(bot, bot.getBotPosition(), wp.getObjectPosition());
//				bot.dtalk.addToLog("ranking size = 0, going for random item!");
                plan = new NavPlan(bot, wp, (int) (mainDecisionTimeout));
//				plan.path = bot.kb.findShortestPath(bot.getBotPosition(), plan.dest.getObjectPosition());
                if (plan.path == null) {
                    return getSpontaneousAntiStuckPlan(bot);
                }


                return plan;
            }

			double distance = getDistanceFollowingMap(bot, bot.getBotPosition(), ranking.last().ent.getObjectPosition());
			int lower = (ranking.size() >= 2) ? (int)(ranking.lower(ranking.last()).dbl) : 0;
//			bot.dtalk.addToLog("got new plan: rank: "+((int)ranking.last().dbl)+
//					" > "+lower+
//					" et: "+EntityType.getEntityType(ranking.last().ent)+
//					" dist: "+distance+" timeout: "+mainDecisionTimeout);
            plan = new NavPlan(bot, ranking.last().ent, (int) (mainDecisionTimeout));

//			plan.path = bot.kb.findShortestPath(bot.getBotPosition(), plan.dest.getObjectPosition());
            ranking.pollLast();

        }
        return plan;
    }

    /**
     * Returns the best available spontaneous plan for the given bot. Can be null.
     * @param bot the bot for whom we search for the plan
     * @return the navigation plan with just wan waypoint that is close to the bot - so called spontaneous plan.
     */
    static NavPlan getSpontaneousPlan(MapBotBase bot) {
        bot.timers.get("nav0").resume();
        NavPlan newPlan = null;

        Vector<Entity> entries = bot.kb.getActiveEntitiesWithinTheRange(bot.getBotPosition(), maximalDistance, bot.getFrameNumber());
        if (entries.size() == 0) {
            return null;
        }

        Entity chosen = null;
        for (Entity ent : entries) {
            if (!CommFun.areOnTheSameHeight(bot.getBotPosition(), ent.getObjectPosition())) {
                continue;
            }
            if (!bot.getBsp().isVisible(bot.getBotPosition(), ent.getObjectPosition())) {
                continue;
            }
            if (chosen != null) {
                float distOld = CommFun.getDistanceBetweenPositions(chosen.getObjectPosition(), bot.getBotPosition());
                float distNew = CommFun.getDistanceBetweenPositions(ent.getObjectPosition(), bot.getBotPosition());
                if (distNew < distOld) {
                    chosen = ent;
                }
            } else {
                chosen = ent;
            }
        }

        if (chosen == null) {
            bot.timers.get("nav0").pause();
            return null;
        }

//		if ( ! CommFun.areOnTheSameHeight(chosen.wp.getPosition(), bot.getBotPosition())) Dbg.err("not the same height!!!");
//		else Dbg.prn("bot h: "+bot.getBotPosition().z+" target h: "+chosen.wp.getPosition().z);

        bot.kb.addToBlackList(chosen);

        newPlan = new NavPlan(bot, chosen, (int) (spontDecisionTimeout));
        newPlan.path = new Waypoint[1];
        newPlan.path[0] = new Waypoint(chosen.getObjectPosition());
        newPlan.isSpontaneos = true;

//		double distance = CommFun.getDistanceBetweenPositions(bot.getBotPosition(), chosen.getObjectPosition());
//		bot.dtalk.addToLog("got new spontaneous plan: et: "+chosen.toString()+" dist: "+distance+" timeout: "+spontDecisionTimeout);

        bot.timers.get("nav0").pause();
        return newPlan;
    }

    /**
     *
     * @param bot
     * @return a plan leading to closest known enemy position
     */
    static NavPlan getEnemyAttackingPlan(MapBotBase bot, NavPlan oldPlan) {
        NavPlan ret = null;
        EnemyInfo e = null;
        double minRisk = Double.POSITIVE_INFINITY;
        for (EnemyInfo ei : bot.kb.getAllEnemyInformation()) {
//            if (ei.lastUpdateFrame < bot.getFrameNumber() +);
            double mapDist = getDistanceFollowingMap(bot, bot.getBotPosition(), ei.getObjectPosition());
            double dist = CommFun.getDistanceBetweenPositions(bot.getBotPosition(), ei.getObjectPosition());
            if (oldPlan != null && ei.ent.getNumber() == oldPlan.dest.getNumber() && dist < 300) {
                Dbg.err(bot.getBotName()+"> not going again for that guy... dist="+dist);
                continue; //if we are close and enemy is not updated, it means it is a zombie :)
            }
            if (mapDist < minRisk) {
                minRisk = mapDist;
                e = ei;
            }
        }
        if (e != null) {
            Waypoint dw = bot.kb.map.findClosestWaypoint(e.getObjectPosition());
            Waypoint dest = getRecursivelyRandomWaypointCloseToGiven(bot, dw, 3);
            ret = new NavPlan(bot, dest, e.ent, (long) mainDecisionTimeout);
        }
        return ret;
    }

    static Waypoint getRecursivelyRandomWaypointCloseToGiven(MapBotBase bot, Waypoint wp, int lvl) {
        if (lvl != 1) {
            wp = getRecursivelyRandomWaypointCloseToGiven(bot, wp, lvl - 1);
        }
        Waypoint[] edgs = wp.getEdges();
        if (edgs == null || edgs.length == 0) {
            return wp;
        }
        Random r = new Random();
        return edgs[r.nextInt(edgs.length)];
    }

    /**
     * If the bot is stuck it may need the spontaneous plan in some random direction in order to
     * move it out from the stuck position, get again close to it's known waypoints and be able to
     * find a new plan.
     * @param bot
     * @return the random spontaneous decision.
     */
    static NavPlan getSpontaneousAntiStuckPlan(MapBotBase bot) {
        Entity re = new Entity();
        re.setNumber(-1);
        Vector3f botPos = bot.getBotPosition();
        Random r = new Random();
        re.setOrigin(new Vector3f(botPos.x+r.nextInt(1000)-500, botPos.y+r.nextInt(1000)-500, botPos.z).toOrigin());
        int timeout = (int) (antiStuckDecisionTimeout);
        NavPlan ret = new NavPlan(bot, re, timeout);
        ret.path = new Waypoint[2];
        ret.path[0] = new Waypoint(re.getObjectPosition());
        ret.path[1] = new Waypoint(re.getObjectPosition());
        ret.isSpontaneos = true;
//		int wpInd = bot.kb.map.indexOf(random.getNode());
//		double distance = CommFun.getDistanceBetweenPositions(bot.getBotPosition(), re.getObjectPosition());
//		bot.dtalk.addToLog("got new anti-stuck spontaneous plan: dist: "+distance+" timeout: "+timeout);
        return ret;
    }

    /**
     * Returns the distance between given positions following the map
     * @param bot the bot for whom the distance is calculated
     * @param from initial position
     * @param to final position
     * @return distance between from and to following the shortest path on the map.
     * Double.MAX_VALUE is returned in case there is no path.
     */
    static double getDistanceFollowingMap(MapBotBase bot, Vector3f from, Vector3f to) {
        double distance = 0.0d;
        Waypoint[] path = bot.kb.map.findShortestPath(from, to);
        if (path == null) {
//			Dbg.err("Path is null at counting distance on map.");
            return Double.MAX_VALUE;
        }
        Vector3f pos = from;
        for (Waypoint wp : path) {
            distance += CommFun.getDistanceBetweenPositions(pos, wp.getObjectPosition());
            pos = wp.getObjectPosition();
        }
        return distance;
    }
}
