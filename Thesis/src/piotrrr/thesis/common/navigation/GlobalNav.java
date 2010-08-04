package piotrrr.thesis.common.navigation;

import piotrrr.thesis.bots.mapbotbase.MapBotBase;

/**
 * The global navigation module of the MapBotBase.
 * @author Piotr Gwizdała
 * @see MapBotBase
 */
public interface GlobalNav {
	
	/**
	 * Returns the new plan that the bot should follow
	 * @param bot the bot for which the plan is being established
	 * @param oldPlan the bot's old plan
	 * @return the new plan for the bot (can be the same as the oldPlan)
	 */
	public NavPlan establishNewPlan(MapBotBase bot, NavPlan oldPlan); 
	
}
