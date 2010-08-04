package piotrrr.thesis.common.navigation;

import piotrrr.thesis.common.GameObject;
import soc.qase.ai.waypoint.Waypoint;
import soc.qase.tools.vecmath.Vector3f;

public class EdgeFailure implements GameObject {
	
	public int failCount = 0;
	
	public Waypoint src, dst;
	
	public EdgeFailure(Waypoint src, Waypoint dst, int count) {
		this.src = src;
		this.dst = dst;
		this.failCount = count;
	}

	@Override
	public Vector3f getObjectPosition() {
		return dst.getObjectPosition();
	}

	@Override
	public String toDetailedString() {
		return "WP failure count: "+failCount+"\n\nSrc WP:\n"+src.toDetailedString()+"\nDst WP:\n"+dst.toDetailedString();
	}

}
