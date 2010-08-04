package piotrrr.thesis.testing;

import soc.qase.state.Entity;

public class EntityMock extends Entity {
	
	String cat, type, subtype;
	
	public EntityMock(String cat, String type, String subtype) {
		this.cat = cat; this.type = type; this.subtype = subtype;
	}
	
	@Override
	public String getCategory() {
		return cat;
	}
	
	@Override
	public String getType() {
		return type;
	}
	
	@Override
	public String getSubType() {
		return subtype;
	}
	
	@Override
	public boolean isWeaponEntity() {
		return true;
	}
	
	@Override
	public int getInventoryIndex() {
		return 16;
	}

}
