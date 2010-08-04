package piotrrr.thesis.common.jobs;

import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.tools.Dbg;
import soc.qase.tools.vecmath.Vector3f;

/**
 * This job detects when the bot got stuck.
 * @author Piotr Gwizda�a
 */
public class StuckDetector extends Job {
	
	public static final float EPSILON = 60;
	
	public boolean isStuck = false;
	
	long lastFrame;
	
	int period;
	
	/**
	 * Remembers last bot's position. 
	 */
	Vector3f lastPos = null;
	
	Vector3f lastlastPos = null;
	
	public StuckDetector(BotBase bot, int period) {
		super(bot);
		this.period = period;
		this.lastFrame = bot.getFrameNumber();
		//lastPos = ((MapBotBase)bot).getBotPosition();
		//lastlastPos = lastPos;
	}
	
	@Override
	public void run() {
		if (bot.getFrameNumber() - lastFrame < period ) {
			isStuck = false;
			return;
		}
		lastFrame = bot.getFrameNumber();
		
		//We don't make stuck detection when paused :)
		if (bot.botPaused || bot.noMove) return;
		
		
		Vector3f pos;
		try {
			pos = bot.getBotPosition();
		}
		catch (NullPointerException e) {
			Dbg.err("Can't read bot position yet");
			return;
		}
		
		if (lastPos == null) {
			lastPos = pos;
			return;
		}
		
		if (lastlastPos == null) {
			lastlastPos = lastPos;
			lastPos = pos;
			return;
		}
		
		float distance = CommFun.getDistanceBetweenPositions(pos, lastPos);
		distance += CommFun.getDistanceBetweenPositions(lastPos, lastlastPos);
		distance += CommFun.getDistanceBetweenPositions(pos, lastlastPos);
		lastlastPos = lastPos;
		lastPos = pos;
		
		if (distance <= EPSILON) {
			((MapBotBase)bot).dtalk.addToLog("I'm stuck!");
			isStuck = true;
		}
		else isStuck = false;
		
	}

}
