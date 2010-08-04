package piotrrr.thesis.common;

import soc.qase.tools.vecmath.Vector3f;

/**
 * Common functions that may be used by bots.
 * @author Piotr Gwizdała
 */
public class CommFun {
	
	/**
	 * Returns the distance between the given positions.
	 * @param from position from
	 * @param to position to
	 * @return the distance between the given positions.
	 */
	public static float getDistanceBetweenPositions(Vector3f from, Vector3f to) {
		Vector3f distance = new Vector3f();
		distance.sub(from, to);
		return distance.length();
	}
	
	/**
	 * Gets the normalized vector with the root in "from" and directed towards "to"
	 * @param from - the position of the beginning of the vector
	 * @param to - the point at which vector will direct.
	 * @return the directing vector with length = 1 (normalized). 
	 */
	public static Vector3f getNormalizedDirectionVector(Vector3f from, Vector3f to) {
		Vector3f result = new Vector3f(to.x-from.x,	to.y-from.y, to.z - from.z);
		result.normalize();
		return result;
	}
	
	/**
	 * Multiplies the given vector by given scalar
	 * @param v
	 * @param scalar
	 * @return
	 */
	public static Vector3f multiplyVectorByScalar(Vector3f v, float scalar) {
		return new Vector3f(v.x*scalar, v.y*scalar,v.z*scalar);
	}
	
	/**
	 * Gets the predicted movement vector based on two positions.
	 * @param from - beginning of the movement
	 * @param to - actual position of the movement
	 * @return the vector that added to position from, would end at position to (returns to - from)
	 */
	public static Vector3f getMovementBetweenVectors(Vector3f from, Vector3f to) {
		return new Vector3f((to.x-from.x), (to.y-from.y), (to.z - from.z));
	}
	
	/**
	 * Returns true if the given positions are on the same height with predefined tolerance.
	 * @param pos1
	 * @param pos2
	 * @return
	 */
	public static boolean areOnTheSameHeight(Vector3f pos1, Vector3f pos2) {
		float tolerance = 20.0f;
		if (Math.abs(pos1.z-pos2.z) < tolerance) return true;
		return false;
	}

        /**
         *
         * @param ind the index of the gun in bots inventory
         * @return the gun's name / or ammunition name
         */
	public static String getGunName(int ind) {
		switch (ind) {
			case 7: return "BLASTER";
			case 8: return "SHOTGUN";
			case 9: return "SUPER_SHOTGUN";
			case 10: return "MACHINEGUN";
			case 11: return "CHAINGUN";
			case 12: return "GRENADES";
			case 13: return "GRENADE_LAUNCHER";
			case 14: return "ROCKET_LAUNCHER";
			case 15: return "HYPERBLASTER";
			case 16: return "RAILGUN";
			case 17: return "BFG10K";
			case 18: return "SHELLS";
			case 19: return "BULLETS";
			case 20: return "CELLS";
			case 21: return "ROCKETS";
			case 22: return "SLUGS";
			default: return "UNKNOWN";
		}
	}

        public static String getGunName(String message) {
		if (message.contains("super shotgun")) {
                    return "super shoutgun";
                }
                else if (message.contains("machinegunned")) {
                    return "machinegun";
                }
                else if (message.contains("blasted")) {
                    return "blaster";
                }
                else if (message.contains("gunned down")) {
                    return "shotgun";
                }
                else if (message.contains("railed")) {
                    return "railgun";
                }
                else if (message.contains("hyperblaster")) {
                    return "hyperblaster";
                }
                else if (message.contains("chaingun")) {
                    return "chaingun";
                }
                else if (message.contains("rocket")) {
                    return "rocket launcher";
                }
                else {
                    return "other";
                }

	}

	
	public static Vector3f cloneVector(Vector3f v) {
		Vector3f ret = new Vector3f();
		ret.x = v.x; ret.y = v.y; ret.z = v.z;
		return ret;
	}

}
