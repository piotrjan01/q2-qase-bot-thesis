package piotrrr.thesis.common.jobs;

import piotrrr.thesis.bots.botbase.BotBase;

/**
 * The Job is a special procedure, usually run periodically by the bot.
 * @author Piotr Gwizdała
 *
 */
public abstract class Job implements Runnable {
	
	/**
	 * The bot that will run the job.
	 */
	BotBase bot;
	
	/**
	 * The constructor
	 * @param bot the bot that will run the job
	 */
	public Job(BotBase bot) {
		this.bot = bot;
	}
	
	/**
	 * The body of the job.
	 */
	public void run() {
		
	}
	
}
