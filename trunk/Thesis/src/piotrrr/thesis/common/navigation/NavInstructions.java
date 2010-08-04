package piotrrr.thesis.common.navigation;

import soc.qase.state.PlayerMove;
import soc.qase.tools.vecmath.Vector3f;

/**
 * This class expresses the explicit instructions on how the bot should move
 * in the next frame. It contains information like in which direction it 
 * should move and what speed and where should it aim.
 * @author Piotr Gwizda³a
 */
public class NavInstructions {

	/**
	 * The movement direction.
	 */
	public Vector3f moveDir;
	/**
	 * The aiming direction.
	 */
	public Vector3f aimDir;
	
	/**
	 * The following field is set according to PlayerMove 
	 * class from QASE library.
	 */
	public int postureState;
	
	/**
	 * The following field is set according to PlayerMove 
	 * class from QASE library.
	 */
	public int walkState;
	
	/**
	 * Constructor
	 * @param movDir move direction
	 * @param aimDir aiming direction
	 * @param velocity speed of movement
	 * @param postureState posture state
	 * @param walkState walking state
	 * @see PlayerMove
	 */
	public NavInstructions(Vector3f movDir, Vector3f aimDir,
			int postureState, int walkState) {
		this.moveDir = movDir;
		this.aimDir = aimDir;
		this.postureState = postureState;
		this.walkState = walkState;
	}	
}
