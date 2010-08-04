package piotrrr.thesis.common.jobs;

import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.tools.Dbg;

/**
 * Dummy job, that repeats some debug info with given period.
 * @author Piotr Gwizda³a
 */
public class GeneralDebugTalk extends Job {
	
	public boolean active = true;
	
	long lastFrame;
	
	long lastAddingFrame;
	
	int period;
	
	String toSay = "";

	public GeneralDebugTalk(BotBase bot, int period) {
		super(bot);
		this.period = period;
		lastFrame = bot.getFrameNumber();
		lastAddingFrame = lastFrame;
	}
	
	@Override
	public void run() {
		if (! active) return;
		if (bot.botPaused) return;
		if (bot.getFrameNumber() - lastFrame < period ) return;
		lastFrame = bot.getFrameNumber();
		
		float h = bot.getBotHealth();
		float a = bot.getBotArmor();
		
		String say = toSay+"  H="+h+" A="+a;
		bot.say(say);
		toSay = "";
		
	}
	
	public void addToLog(String s) {
		if (! active) return;
		double frame = (bot.getFrameNumber()-lastAddingFrame)/10.0;
		lastAddingFrame = bot.getFrameNumber();
		Dbg.prn(bot.getBotName()+":\t"+frame+">\tBOT SAYS: "+s);
		toSay += s+" :: ";
	}

}
