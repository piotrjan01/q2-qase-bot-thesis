package piotrrr.thesis.bots.tuning;

import java.lang.reflect.Field;



public class Config {
		
	public float getParameter(String name) throws Exception {
		for (Field f : this.getClass().getDeclaredFields()) {
			if (f.getName().equals(name)) return (Float)f.get(this);
		}
		throw new Exception("Unknown field name!");
	}

	public float getParameterMax(String name) throws Exception {
		name = name+"_MAX";
		return getParameter(name);
	}

	public float getParameterMin(String name) throws Exception {
		name = name+"_MIN";
		return getParameter(name);
	}

	public boolean isParameterInteger(String name) throws Exception {
		for (Field f : this.getClass().getDeclaredFields()) {
			if (f.getName().equals(name)) {
				return (f.getType().equals(Integer.class) || f.getType().equals(int.class));
			}
		}
		throw new Exception("Unknown field name!");
	}
		

}
