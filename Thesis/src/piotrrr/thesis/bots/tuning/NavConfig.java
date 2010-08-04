package piotrrr.thesis.bots.tuning;


public class NavConfig extends Config {
	
	public float armorWeight = 0.2f;
	public float armorWeight_MIN = 0f;
	public float armorWeight_MAX = 1f;
	
	public float healthWeight = 0.5f;
	public float healthWeight_MIN = 0f;
	public float healthWeight_MAX = 1f;
	
	public float weaponsWeight = 0.2f;
	public float weaponsWeight_MIN = 0f;
	public float weaponsWeight_MAX = 1f;
	
	public float ammoWeight = 0.1f;
	public float ammoWeight_MIN = 0f;
	public float ammoWeight_MAX = 1f;
	
	public float distanceWeight = 0.01f;
	public float distanceWeight_MIN = 0f; 
	public float distanceWeight_MAX = 1f;
	
	public float enemyCostWeight = 0.6f;
	public float enemyCostWeight_MIN = 0.1f; 
	public float enemyCostWeight_MAX = 0.9f;
	
	public float weaponDeficiencyTolerance = 0.5f;
	public float weaponDeficiencyTolerance_MIN = 0.1f; 
	public float weaponDeficiencyTolerance_MAX = 0.9f;
	
	public static float MAX_DISTANCE = 5000f; 

	
}
