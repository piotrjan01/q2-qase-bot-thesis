package piotrrr.thesis.common.combat;

import soc.qase.tools.vecmath.Vector3f;

public class FiringInstructions {

	public Vector3f fireDir;
	
	public boolean doFire = true;

        public double timeToHit = 0;

	public FiringInstructions(Vector3f fireDir) {
		this.fireDir = fireDir;
	}

        public FiringInstructions(Vector3f fireDir, double timeToHit) {
		this.fireDir = fireDir;
                this.timeToHit = timeToHit;
	}

}
