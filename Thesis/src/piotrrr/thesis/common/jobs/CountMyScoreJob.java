/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.common.jobs;

import java.util.Vector;
import piotrrr.thesis.bots.botbase.BotBase;

/**
 *
 * @author Piotr Gwizdała
 */
public class CountMyScoreJob extends Job {

    int score = 0;

    public CountMyScoreJob(BotBase bot) {
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
                String killer = "";
		for (String cmd : commands) {
			if (cmd.contains(" was ") && cmd.contains(" by ")) {
                            killer = cmd.substring(cmd.indexOf(" by ")+4);
			    if (killer.indexOf("'s") != -1)
					killer = killer.substring(0, killer.indexOf("'s"));
			}
                        else if (cmd.contains("rocket") && cmd.contains("ate")) {
                            killer = cmd.substring(cmd.indexOf(" ate ")+5);
			    if (killer.indexOf("'s") != -1)
					killer = killer.substring(0, killer.indexOf("'s"));
                        }
                        else if (cmd.contains("rocket") && cmd.contains("dodged")) {
                            killer = cmd.substring(cmd.indexOf(" dodged ")+8);
			    if (killer.indexOf("'s") != -1)
					killer = killer.substring(0, killer.indexOf("'s"));
                        }
                        if (killer.equals(bot.getBotName())) score++;
		}

	}

     public int getBotScore() {
         return score;
     }



}
