package piotrrr.thesis.common.entities;

/**
 * Class that encapsulates EntityType 
 * and double precision floating point number.
 * @author Piotr Gwizda³a
 */
public class EntityTypeDoublePair {
	
	public double d;
	
	public EntityType t;
	
	public EntityTypeDoublePair(EntityType type, double d) {
		t = type;
		this.d = d;
	}

}
