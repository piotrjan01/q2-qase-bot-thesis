package piotrrr.thesis.common.combat;

import soc.qase.tools.vecmath.Vector3f;

/**
 * The instructions on how to fire in next frame
 * @author piotrrr
 */
public class FiringInstructions {

    /**
     * The direction at which to fire
     */
    public Vector3f fireDir;
    /**
     * Whether to fire or not
     */
    public boolean doFire = true;
    /**
     * The time required to hit the target
     */
    public double timeToHit = 0;

    /**
     * 
     * @param fireDir where to fire
     */
    public FiringInstructions(Vector3f fireDir) {
        this.fireDir = fireDir;
    }

    /**
     *
     * @param fireDir where to fire
     * @param timeToHit time to hit the enemy
     */
    public FiringInstructions(Vector3f fireDir, double timeToHit) {
        this.fireDir = fireDir;
        this.timeToHit = timeToHit;
    }
}
