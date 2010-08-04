package piotrrr.thesis.common.jobs;

import java.util.Vector;

import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.combat.EnemyInfo;

/**
 * The job that implements bot's perception and reaction do basic commands given
 * by defined commander.
 * 
 * Available commands:
 * disc - tells the bot to disconnect and terminate it's program.
 * die - tells the bot to respawn.
 * 
 * TODO: list the commands in the comment!
 * 
 * @author Piotr Gwizda�a
 */
public class BasicCommands extends Job {
	
	/**
	 * The name of the commander
	 */
	String commanderName = "";

	/**
	 * Constructor
	 * @param bot the bot to use the Job
	 */
	public BasicCommands(BotBase bot) {
		super(bot);
	}
	
	/**
	 * Constructor
	 * @param bot the bot to use the job
	 * @param commanderName the name of the commander. Only his/hers commands will be
	 * valid.
	 */
	public BasicCommands(BotBase bot, String commanderName) {
		super(bot);
		this.commanderName = commanderName;
	}
	
	@Override
	public void run() {
		super.run();
		Vector<String> commands = bot.getMessages(commanderName+": ");
		if (commands == null) return;
		for (String cmd : commands) {
			handleCommand(cmd);
		}
	}
	
	public void handleCommand(String cmd) {
		cmd = cmd.trim();
		
		if (cmd.equals("disc")) {
			bot.say("Bye, bye!");
			bot.disconnect();
		}
		else if (cmd.equals("die")) {
			bot.consoleCommand("kill");
		}
		else if (cmd.startsWith("cmd ")) {
			String todo = cmd.substring(4);
			bot.say("Passing the command to console: "+todo);
			bot.consoleCommand(todo);
		}
		else {
			try {
				
				//We cover the field casting it to MapBotBase
				MapBotBase bot = (MapBotBase)this.bot;
			
				if (cmd.equals("pausebot")) {
					bot.say("pause = "+(!bot.botPaused));
					bot.botPaused = ! bot.botPaused;
				}
				
				else if (cmd.equals("nofire")) {
					bot.say("noFire = "+( ! bot.noFire));
					bot.noFire = ! bot.noFire;
				}
				else if (cmd.equals("nomove")) {
					bot.say("noMove = "+( ! bot.noMove));
					bot.noMove = ! bot.noMove;
				}
				else if (cmd.startsWith("forcewpn ")) {
					int wpn = Integer.parseInt(cmd.substring(9));
					bot.say("forced weapon = "+bot.forcedweapon);
					bot.forcedweapon = wpn;
				}
				else if (cmd.startsWith("sah ")) {
					int height = Integer.parseInt(cmd.substring(4));
					bot.say("agentsHeight = "+height);
					EnemyInfo.agentsHeight = height;
				}
				else {
					bot.say("I don't get this command: "+cmd);
				}
			}
			catch (ClassCastException e) {
				bot.say("This command is not for me... ClassCastException!");
			}
		}
	}

}
