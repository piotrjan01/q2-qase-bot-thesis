package piotrrr.thesis.common.jobs;

import piotrrr.thesis.bots.botbase.BotBase;

/**
 * Used to call some methods, when the application is interrupted by 
 * for instance CTR+C
 * @author Piotr Gwizdała
 */
public class ShutdownJob extends Job {

	public ShutdownJob(BotBase bot) {
		super(bot);
	}
	
	@Override
	public void run() {
		bot.disconnect();
	}

}
