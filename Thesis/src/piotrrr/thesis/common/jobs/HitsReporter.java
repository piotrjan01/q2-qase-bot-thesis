package piotrrr.thesis.common.jobs;

import java.util.LinkedList;
import java.util.TreeMap;
import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.tools.Dbg;

/**
 * This job reports bot's state changes.
 * @author Piotr Gwizdała
 */
public class HitsReporter extends Job {

        static final int HITSMEMORY = 100;

        public static final TreeMap<String, LinkedList<int []>> lastHitTimes = new TreeMap<String, LinkedList<int []>>();
	
	float lastHealth;
	
	float lastArmor;
	
	public boolean stateHasChanged = false;
	
	public HitsReporter(BotBase bot) {
		super(bot);
		lastHealth = ((MapBotBase)bot).getBotHealth();
		lastArmor = ((MapBotBase)bot).getBotArmor();
                synchronized (lastHitTimes) {
                    lastHitTimes.put(bot.getBotName(), new LinkedList<int[]>());
                }
	}
	
	@Override
	public synchronized void run() {
		float h = ((MapBotBase)bot).getBotHealth();
		float a = ((MapBotBase)bot).getBotArmor();	
		if (h < lastHealth || a < lastArmor) {
                    synchronized(lastHitTimes) {
                        LinkedList<int []> hits = lastHitTimes.get(bot.getBotName());
                        hits.add(new int [] { bot.getFrameNumber(), (int)(lastHealth-h)+(int)(lastArmor-a) } );
                        if (hits.size() > HITSMEMORY)
                            hits.pollFirst();
                        //FIXME:
//                        Dbg.prn(bot.getBotName()+"@"+bot.getFrameNumber()+":\t==> BOT HIT: lost h: "+(lastHealth-h)+" lost a: "+(lastArmor-a));
                    }
		}
		lastHealth = h;
		lastArmor = a;
	}

        public static int wasHitInGivenPeriod(long from, long to, String name) {
            synchronized(lastHitTimes) {
                LinkedList<int []> hits = lastHitTimes.get(name);
//                Dbg.prn(name+" has "+hits.size()+" hits reported");
                if (hits == null) return 0;
                for (int [] l : hits) {
                    if (l[0]>=from && l[0]<= to) {
//                        Dbg.prn(name+" was hit in period ("+from+", "+to+") at time "+l);
                        return l[1];
                    }
//                    else Dbg.prn(name+" was hit at "+l[0]+" which is not within ("+from+", "+to+")");
                }
                return 0;
            }
        }

}
