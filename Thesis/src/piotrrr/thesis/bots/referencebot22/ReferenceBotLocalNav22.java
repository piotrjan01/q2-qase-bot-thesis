package piotrrr.thesis.bots.referencebot22;

import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.navigation.LocalNav;
import piotrrr.thesis.common.navigation.NavInstructions;
import piotrrr.thesis.common.navigation.NavPlan;
import piotrrr.thesis.tools.Dbg;
import soc.qase.state.PlayerMove;
import soc.qase.tools.vecmath.Vector3f;

public class ReferenceBotLocalNav22 implements LocalNav {
	
	public static final int acceptableDistance = 40;

	@Override
	public NavInstructions getNavigationInstructions(MapBotBase bot) {
		
		NavPlan plan = bot.plan;
		
		Vector3f playerPos = bot.getBotPosition();
		Vector3f movDir = null;
		Vector3f desiredPos = null;
		
		
		//If we are next to destination.
		if (CommFun.getDistanceBetweenPositions(plan.dest.getObjectPosition(), playerPos)
				<= acceptableDistance) {
			plan.done = true;
			return null;
		}
		
		
		//TODO: why the path can be null? ????
		//If the path is null, we can't do anything.
		if (plan.path == null) {
			plan.path = bot.kb.findShortestPath(playerPos, plan.dest.getObjectPosition());
			if (plan.path == null) {
				Dbg.err("Path is null!");
				return null;
			}
		}
		
		int posture = PlayerMove.POSTURE_NORMAL;
		
		//If we are close enough to waypoint, consider the next one.
		if (plan.pathIndex < plan.path.length && CommFun.getDistanceBetweenPositions(plan.path[plan.pathIndex].getObjectPosition(), playerPos)
				<= acceptableDistance) {
			plan.pathIndex++;
		}
		
		if (plan.pathIndex >= plan.path.length) { 
			//If we have already went through all the path, 
			//we set ourselves towards destination
			desiredPos = plan.dest.getObjectPosition();
		}
		else desiredPos = plan.path[plan.pathIndex].getObjectPosition();
		
		movDir = CommFun.getNormalizedDirectionVector(playerPos, desiredPos);
		
		return new NavInstructions(movDir, movDir, posture, PlayerMove.WALK_RUN);
	}
	
}
