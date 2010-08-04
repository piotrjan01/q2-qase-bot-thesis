package piotrrr.thesis.common.entities;

import piotrrr.thesis.common.GameObject;
import soc.qase.state.Entity;
import soc.qase.tools.vecmath.Vector3f;

/**
 * The class that encapsulates the Entity object with some Double value.
 * Is comparable.
 * @author Piotr Gwizdała
 */
public class EntityDoublePair implements Comparable<EntityDoublePair>, GameObject {
	
	public Entity ent = null;
	
	public double dbl = 0.0;
	
	public EntityDoublePair(Entity e, double dbl) {
		this.ent = e;
		this.dbl = dbl;
	}

	@Override
	public int compareTo(EntityDoublePair o) {
		if (this.dbl > o.dbl) return 1;
		if (this.dbl < o.dbl) return -1;
		return 0;
	}

	@Override
	public Vector3f getObjectPosition() {
		return ent.getObjectPosition();
	}
	
	@Override
	public String toString() {
		return ent.toString();
	}

	@Override
	public String toDetailedString() {
		return "Number: "+dbl+"\n\nEntity:"+ent.toDetailedString();
	}

}
