package piotrrr.thesis.testing;

import piotrrr.thesis.bots.smartbot.ReferenceBot;
import piotrrr.thesis.bots.tuning.NavConfig;

public class SmartBotMock extends ReferenceBot {

	public SmartBotMock() {
		super("MOCK", "none");
		nConfig = new NavConfig();
	}
	
	float health = 20;
	
	float armor = 30;
	
	@Override
	public float getBotHealth() {
		return health;
	}
	
	@Override
	public float getBotArmor() {
		return armor;
	}

	public void setHealth(float health) {
		this.health = health;
	}

	public void setArmor(float armor) {
		this.armor = armor;
	}
	
	@Override
	public float getAmmunitionState(int gunIndex) {
		// TODO Auto-generated method stub
		return 0.7f;
	}

}
