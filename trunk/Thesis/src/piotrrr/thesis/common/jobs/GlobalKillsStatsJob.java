package piotrrr.thesis.common.jobs;

import java.util.Vector;

import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.stats.BotStatistic;

public class GlobalKillsStatsJob extends Job {

	public GlobalKillsStatsJob(BotBase bot) {
		super(bot);
	}
	
	
	@Override
	public void run() {
		super.run();
                logKillsToStats();
	}
	

        private void logKillsToStats() {
		Vector<String> commands = bot.getMessages("");
		if (commands == null) return;
		for (String cmd : commands) {
			if (cmd.contains(" was ") && cmd.contains(" by ")) {
                            String victim = cmd.substring(0, cmd.indexOf(" was "));
                            String killer = cmd.substring(cmd.indexOf(" by ")+4);
			    if (killer.indexOf("'s") != -1)
					killer = killer.substring(0, killer.indexOf("'s"));
                            BotStatistic.getInstance().addKill(
                                    bot.getFrameNumber(),
                                    killer,
                                    victim,
                                    CommFun.getGunName(cmd)
                                );
                            if (CommFun.getGunName(cmd).equals(CommFun.getGunName("xxx")))
                                System.out.println("Unrecognized kill log: "+cmd);
			}
                        else if (cmd.contains("rocket") && cmd.contains("ate")) {
                            String victim = cmd.substring(0, cmd.indexOf(" ate "));
                            String killer = cmd.substring(cmd.indexOf(" ate ")+5);
			    if (killer.indexOf("'s") != -1)
					killer = killer.substring(0, killer.indexOf("'s"));
                            BotStatistic.getInstance().addKill(
                                    bot.getFrameNumber(),
                                    killer,
                                    victim,
                                    CommFun.getGunName(cmd)
                                );
                            if (CommFun.getGunName(cmd).equals(CommFun.getGunName("xxx")))
                                System.out.println("Unrecognized kill log: "+cmd);
                        }
                        else if (cmd.contains("rocket") && cmd.contains("dodged")) {
                            String victim = cmd.substring(0, cmd.indexOf(" almost "));
                            String killer = cmd.substring(cmd.indexOf(" dodged ")+8);
			    if (killer.indexOf("'s") != -1)
					killer = killer.substring(0, killer.indexOf("'s"));
                            BotStatistic.getInstance().addKill(
                                    bot.getFrameNumber(),
                                    killer,
                                    victim,
                                    CommFun.getGunName(cmd)
                                );
                            if (CommFun.getGunName(cmd).equals(CommFun.getGunName("xxx")))
                                System.out.println("Unrecognized kill log: "+cmd);
                        }
		}

	}
	

}
