package piotrrr.thesis.common.jobs;

import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.bots.referencebot.ReferenceBot;
import piotrrr.thesis.common.fsm.StateBot;
import soc.qase.bot.BasicBot;

/**
 * This job reports bot's state changes.
 * @author Piotr Gwizda�a
 */
public class StateReporter extends Job {
	
	/**
	 * Remembers last state's name. Helps to find out state changes. 
	 */
	String lastStateName = "";
	
	StateBot bot;
	
	public boolean stateHasChanged = false;
	
	public StateReporter(BotBase bot1, StateBot bot) {
		super(bot1);
		this.bot = bot;
		lastStateName = bot.getCurrentStateName();
	}
	
	@Override
	public void run() {
		String stateName = bot.getCurrentStateName();
		if (stateName.equals(lastStateName)) {
			stateHasChanged = false;
			return;
		}
		
		stateHasChanged = true;
		
		bot.say("State changed: "+lastStateName+" -> "+stateName);
		lastStateName = stateName;
	}

}
