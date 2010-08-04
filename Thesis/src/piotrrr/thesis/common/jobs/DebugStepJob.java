package piotrrr.thesis.common.jobs;

import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.gui.MainFrame;

/**
 * This job reports bot's state changes.
 * @author Piotr Gwizdała
 */
public class DebugStepJob extends Job {
	
	static final int MF_REPORTING_STEP = 3;
	
	long pauseFrame;
	
	MainFrame mf;
	
	public DebugStepJob(BotBase bot, MainFrame mf) {
		super(bot);
		pauseFrame = Long.MAX_VALUE;
		this.mf = mf;
	}
	
	@Override
	public void run() {
		if (bot.botPaused) return;
		if (bot.getFrameNumber() >= pauseFrame) {
			bot.botPaused = true;
			mf.updateDisplayedInfo();
			pauseFrame = Long.MAX_VALUE;
		}
		//update displayed info with given period:
		if (bot.getFrameNumber() % MF_REPORTING_STEP == 0)
			mf.updateDisplayedInfo();
	}
	
	public void pauseIn(long steps) {
		pauseFrame = bot.getFrameNumber() + steps;
		if (bot.botPaused) bot.botPaused = false;
	}

}
