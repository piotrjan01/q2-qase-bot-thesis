package piotrrr.thesis.common.entities;

import soc.qase.ai.waypoint.WaypointItem;
import soc.qase.state.Entity;

/**
 * Enumerates entity types recognized by program.
 * @author Piotr Gwizda³a
 */
public enum EntityType {
	
	WEAPON, HEALTH, ARMOR, PLAYER, AMMO, UNKNOWN;
	
	/**
	 * Returns the entity type of the given entity.
	 * @param e entity
	 * @return e's type.
	 */
	public static EntityType getEntityType(Entity e) {
		return getEntityType(e.getCategory(), e.getType(), e.getSubType());
	}
	
	public static EntityType getEntityType(WaypointItem i) {
		return getEntityType(i.getCategory(), i.getType(), i.getSubType());
	}
	
	/**
	 * Returns the entity type of the given characterizing strings.
	 * @param cat the category like in QASE
	 * @param type type of entity like in QASE 
	 * @param subtype subtype of entity like in QASE
	 * @return
	 * 
	 * @see Entity
	 */
	public static EntityType getEntityType(String cat, String type, String subtype) {
		try {
			if (cat.equals(Entity.CAT_PLAYERS)) return EntityType.PLAYER;
			
			if (cat.equals(Entity.CAT_WEAPONS)) return EntityType.WEAPON;
			
			if (type.equals(Entity.TYPE_HEALTH)
					|| type.equals(Entity.TYPE_INVULNERABILITY) || 
					type.equals(Entity.TYPE_MEGAHEALTH)) return EntityType.HEALTH;
			
			if (type.equals(Entity.TYPE_ARMOR)) return EntityType.ARMOR;
			
			if ((type.equals(Entity.TYPE_AMMO) || type.equals(Entity.TYPE_AMMOPACK))) return EntityType.AMMO;
			
		}
		catch (NullPointerException e) {
			return UNKNOWN;
		}
		
		return UNKNOWN;
	}
	
	
}

