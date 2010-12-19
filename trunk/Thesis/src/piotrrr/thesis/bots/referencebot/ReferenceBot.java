package piotrrr.thesis.bots.referencebot;

import piotrrr.thesis.bots.learnbot.LearnBot;
import piotrrr.thesis.bots.tuning.NavConfig;

public class ReferenceBot extends LearnBot {

    public ReferenceBot(String botName, String skinName) {
        super(botName, skinName);
        nConfig = new NavConfig();
    }
}
