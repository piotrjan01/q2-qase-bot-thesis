package piotrrr.thesis.common.combat;

import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.GameObject;
import piotrrr.thesis.gui.AppConfig;
import soc.qase.state.Entity;
import soc.qase.tools.vecmath.Vector3f;

/**
 * This class stores the information about the enemies in the game.
 * @author Piotr Gwizdała
 */
public class EnemyInfo implements GameObject {
	
	/**
	 * When enemy info is older than this, it is being removed.
	 */
	public static final int MAX_ENEMY_INFO_AGE = (int) (200);
	
	/**
	 * The enemie's entity
	 */
	public Entity ent;
	
	/**
	 * Last position of the enemy (in last frame before update)
	 */
	private Vector3f lastPos = null;
	
	/**
	 * Last frame number at which the enemy information has been updated.
	 */
	public long lastUpdateFrame = 0L;
	
	/**
	 * Last prediction error of enemy position predicting.
	 */
	public float lastPredictionError = Float.MAX_VALUE;
	
	/**
	 * The position that the enemy is predicted to reach in the next frame.
	 */
	public Vector3f predictedPos = null;
	
	//tuned value = 35
	/**
	 * The height of the agent. It is used to tell whether some part of enemies
	 * body is visible from given point.
	 */
	public static int agentsHeight = 35;

        /**
         * Creates the new enemy info
         * @param ent the opponents entity object
         * @param frame the frame at which it was observed
         */
	public EnemyInfo(Entity ent, long frame) {
		this.ent = ent.deepCopy();
		this.lastUpdateFrame = frame-1;
		this.updateEnemyInfo(ent, frame);
	}

	@Override
	public Vector3f getObjectPosition() {
		return ent.getObjectPosition();
	}

        /**
         * @return the position of the opponent's head
         */
	public Vector3f getPositionHead() {
		Vector3f r = new Vector3f(ent.getObjectPosition());
		r.z += agentsHeight/2;
		return r;
	}

        /**
         * @return the position of enemies feet
         */
	public Vector3f getPositionFeet() {
		Vector3f r = new Vector3f(ent.getObjectPosition());
		r.z -= agentsHeight/2;
		return r;
	}

	@Override
	public String toDetailedString() {
		return "Enemy name: "+ent.getName()+"\n"+
				"last update frame: "+lastUpdateFrame+"\n"+
				"position: "+ent.getObjectPosition()+"\n"+
				"last position: "+lastPos+"\n"+
				"predicted position: "+predictedPos+"\n"+
				"last prediction error: "+lastPredictionError+"\n"+
				"movement: "+getMovementDistance()+"\n"+
				"gun: "+CommFun.getGunName(ent.getWeaponInventoryIndex())+"\n"+
				"gun nr: "+ent.getWeaponInventoryIndex()+"\n"+
				"entity: "+ent.getCategory()+"."+ent.getType()+"."+ent.getSubType()+"\n"+
				"skin: "+ent.getSkin()+"\n"+
				"active: "+ent.getActive()+"\n"+
				"origin: "+ent.getOrigin()+"\n"+
				"old origin: "+ent.getOldOrigin();
//				"effects string: "+getEffectsList();
	}

        /**
         * Updates enemy info
         * @param e the entity with which we want to update it
         * @param frameNumber the frame number at which we update
         * @return if this enemy info cannot be updated with given entity - false.
         * If updated successfully, returns true.
         */
	public boolean updateEnemyInfo(Entity e, long frameNumber) {
		//If it is not the same entity, we exit
		if (ent.getNumber() != e.getNumber()) return false;
		if ( ! ent.getName().equalsIgnoreCase(e.getName())) return false;
		

		//new last position is the current position (will be used next time).
		lastPos = ent.getObjectPosition();
		//we copy the whole entity
		ent = e.deepCopy();
		//if we suspect the opponent is dead, we mark it as inactive (some QASE bug ?)
		if (CommFun.getGunName(ent.getWeaponInventoryIndex()).equals("UNKNOWN"))
			ent.setActive(false);
		//we set the new last update time
		lastUpdateFrame = frameNumber;
		
		//we calculate last prediction error
		lastPredictionError = getPredictionError();

		//We predict the position
		predictedPos = predictPositionBasingOnMovement();
		
		return true;
		
	}
	
	/**
	 * 
	 * @return The predicted enemy position in next frame
	 */
	private Vector3f predictPositionBasingOnMovement() {
		Vector3f movement = CommFun.getMovementBetweenVectors(lastPos, getObjectPosition());
		Vector3f ret = CommFun.cloneVector(getObjectPosition());
		ret.add(movement);
		return ret;
	}
	
	/**
	 * The error of the prediction
	 * @return
	 */
	private float getPredictionError() {
		if (predictedPos == null) return Float.MAX_VALUE;
		return CommFun.getDistanceBetweenPositions(getObjectPosition(), predictedPos);
	}
	
	/**
	 * The movement between last position and current position 
	 * @return
	 */
	private float getMovementDistance() {
		if (lastPos == null) return Float.MAX_VALUE;
		return CommFun.getDistanceBetweenPositions(lastPos, getObjectPosition());
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ent.getName();
	}
	
	/**
	 * Calculates the age of the information
	 * @param currentFrame
	 * @return
	 */
	private long getInfoAge(long currentFrame) {
		return currentFrame - lastUpdateFrame;
	}
	
	/**
	 * Checks if the information is out of date
	 * @param currentFrame the current frame in the game
	 * @return true if information is out of date
	 */
	public boolean isOutdated(long currentFrame) {
		return (getInfoAge(currentFrame) > MAX_ENEMY_INFO_AGE);
	}
	
	/**
	 * Returns the position of the best visible part of the enemy to shoot at.
	 * @param bot the bot that is looking at the enemy.
	 * @return the position of such part (it is emenie's head, feet or body).
	 */
	public Vector3f getBestVisibleEnemyPart(MapBotBase bot) {
		Vector3f botPos = new Vector3f(bot.getBotPosition());
		botPos.z += agentsHeight / 2;
		if (ent.isCrouching()) {
			if ( bot.getBsp().isVisible(botPos, getPositionFeet())) return getPositionFeet();
		}
		if ( bot.getBsp().isVisible(botPos, getObjectPosition())) return getObjectPosition();
		else if ( bot.getBsp().isVisible(botPos, getPositionFeet())) {
			return getPositionFeet();
		}
		else if ( bot.getBsp().isVisible(botPos, getPositionHead())) {
			return getPositionHead();
		}
		else return null;
	}
	
}
