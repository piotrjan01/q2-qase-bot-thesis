package piotrrr.thesis.common.jobs;

import java.util.Vector;

import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.DamageMsgsParser;
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
        String[] res = DeathMsgsParser.parseMessage(cmd);
        System.out.print(CommFun.getArrayAsString(res));
    }

    private void logKillsToStats() {
        Vector<String> commands = bot.getMessages("");
        if (commands == null) {
            return;
        }
        for (String cmd : commands) {
            String[] res = DeathMsgsParser.parseMessage(cmd);

            if (res[0] != null && (!res[0].equals(res[1]))) {
                BotStatistic.getInstance().addKill(
                        bot.getFrameNumber(),
                        res[0],
                        res[1],
                        res[2]);

                System.out.println("msg> " + cmd);
            }
            else if (DamageMsgsParser.parseMessage(cmd, bot.getFrameNumber())) {
                System.out.print(".");
            }
            else {
//                System.out.println("unknown-msg> " + cmd);
            }
        }

    }
}
