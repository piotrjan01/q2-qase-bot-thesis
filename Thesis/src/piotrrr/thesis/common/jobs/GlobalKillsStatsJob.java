package piotrrr.thesis.common.jobs;

import java.util.Vector;

import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.DeathMsgsParser;
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

    public static void main(String[] arg) {
        String cmd = "xxx couldn't hide from yyy's BFG";
        String [] res = DeathMsgsParser.parseMessage(cmd);
        System.out.print("[ ");
        for (String s : res)
            System.out.print(s+" ");
        System.out.print("]\n");
    }

    private void logKillsToStats() {
        Vector<String> commands = bot.getMessages("");
        if (commands == null) {
            return;
        }
        for (String cmd : commands) {
            String [] res = DeathMsgsParser.parseMessage(cmd);

            if (res[0] != null && ( ! res[0].equals(res[1]))) {
                BotStatistic.getInstance().addKill(
                        bot.getFrameNumber(),
                        res[0],
                        res[1],
                        res[2]);
                if (CommFun.getGunName(cmd).equals(CommFun.getGunName("xxx"))) {
                    System.out.println("Unrecognized kill log: " + cmd);
                }
            } else {
                System.out.println("msg> " + cmd);
            }
        }

    }
}
