package piotrrr.thesis.bots.rlbot;

import java.util.LinkedList;
import java.util.Random;
import piotrrr.thesis.bots.rlbot.rl.Action;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.combat.*;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.stats.BotStatistic;
import pl.gdan.elsy.qconf.Brain;
import soc.qase.tools.vecmath.Vector3f;

/**
 * TODO:
 * - tylko jeden sposób strzelania? - przewidywanie z tw. sinusów.
 */
/**
 * The aiming module
 * @author Piotr Gwizdała
 */
public class RLCombatModule {

    public static final int episodeTime = 25;
    int currentEpisodeTime = 0;
    RLBot bot;

    public RLBotPerception perception;
//    double actionReward = 0;
//    int actionFireMode = Action.NO_FIRE;
    Action[] actions = Action.getAllActionsArray();
    public Brain brain;
    boolean unipolar = true;

    public RLCombatModule(RLBot bot) {
        this.bot = bot;

        int hiddenLayerNeurons = 4;
        int[] hiddenLayers = new int[actions.length];
        for (int i = 0; i < hiddenLayers.length; i++) {
            hiddenLayers[i] = hiddenLayerNeurons;
        }
        perception = new RLBotPerception(bot);
        brain = new Brain(perception, actions, hiddenLayers);

        brain.setAlpha(0.6); //learning rate
        brain.setGamma(0.3); //discounting rate
        brain.setLambda(0); //trace forgetting
//        b.setUseBoltzmann(true);
//        b.setTemperature(0.001);
        brain.setRandActions(0.1); //exploration

//        brain.setAlpha(getRandParam(0.1, 0.8, 0.1)); //learning rate
//        brain.setGamma(getRandParam(0, 0.9, 0.2)); //discounting rate
//        brain.setLambda(getRandParam(0, 0.9, 0.2)); //trace forgetting
////        b.setUseBoltzmann(true);
////        b.setTemperature(0.001);
//        brain.setRandActions(getRandParam(0.01, 0.4, 0.1)); //exploration
//        unipolar = Rand.b();

        System.out.println(bot.getBotName() + "'s RLCombatModule started: actions=" + actions.length);

    }

    double getRandParam(double from, double to, double quant) {
        int count = (int) ((to - from) / quant) + 1;
        Random r = new Random();
        return from + r.nextInt(count) * quant;
    }

    @Override
    public String toString() {
        String s = "alpha=" + brain.getAlpha() + " gamma=" + brain.getGamma() +
                "\nlambda=" + brain.getLambda() + " rand=" + brain.getRandActions() + "\n";
        return s;
    }

    public FiringDecision getFiringDecision() {
        Vector3f playerPos = bot.getBotPosition();
        EnemyInfo chosen = null;
        float chosenRisk = Float.MAX_VALUE;
        float maxDist = -1;
        float maxError = 0;
        LinkedList<EnemyInfo> eligible = new LinkedList<EnemyInfo>();
        for (EnemyInfo ei : bot.kb.enemyInformation.values()) {

            if (ei.getBestVisibleEnemyPart(bot) == null) {
                continue;
            }

            if (ei.lastUpdateFrame + bot.cConfig.maxEnemyInfoAge4Firing < bot.getFrameNumber()) {
                continue;
            }

            float dist = CommFun.getDistanceBetweenPositions(playerPos, ei.getObjectPosition());
            float error = ei.lastPredictionError;

            if (dist > maxDist) {
                maxDist = dist;
            }
            if (error > maxError) {
                maxError = error;
            }

            eligible.add(ei);
        }

        for (EnemyInfo ei : eligible) {
            float dist = CommFun.getDistanceBetweenPositions(playerPos, ei.getObjectPosition());
            float error = ei.lastPredictionError;
            float risk = dist / maxDist + error / maxError;
            if (chosenRisk > risk) {
                chosenRisk = risk;
                chosen = ei;
            }
        }

        if (chosen == null) {
            return null;
        }
        return new FiringDecision(chosen, -1);
    }

    /**
     * Returns the firing instructions
     * @param fd firing decision
     * @param bot the bot
     * @return
     */
    public FiringInstructions getFiringInstructions(FiringDecision fd) {
        if (fd == null || fd.enemyInfo == null) {
            return null;
        }

        boolean reloading = bot.getWorld().getPlayer().getPlayerGun().isCoolingDown();
        Vector3f noFiringLook = fd.enemyInfo.predictedPos == null ? fd.enemyInfo.getObjectPosition() : fd.enemyInfo.predictedPos;
        if (reloading) {
            return SimpleAimingModule.getNoFiringInstructions(bot, noFiringLook);
        }

        if (currentEpisodeTime >= episodeTime) {
            currentEpisodeTime = 0;

            bot.rewardsCount++;
            bot.totalReward += perception.getReward();

            if (BotStatistic.getInstance() != null) {
                BotStatistic.getInstance().addReward(bot.getBotName(), perception.getReward(), bot.getFrameNumber());
            }

            //choose new action and execute it
            perception.perceive(); //calls updateInputValues
            brain.count(Action.getActionsAvailability(bot)); //chooses action and updates with last reward.
            executeAction(actions[brain.getAction()]);
            perception.resetReward();
        }
        else currentEpisodeTime++;
//        return getFiringInstructionsAtHitpoint(fd, 1);
//        return getNewPredictingFiringInstructions(bot, fd, bot.cConfig.getBulletSpeedForGivenGun(bot.getCurrentWeaponIndex()));
//        switch (actionFireMode) {
//            case Action.FIRE:
//                return getFastFiringInstructions(fd, bot);
//            case Action.FIRE_PREDICTED:
        return SimpleAimingModule.getNewPredictingFiringInstructions(bot, fd, bot.cConfig.getBulletSpeedForGivenGun(bot.getCurrentWeaponIndex()));
//            default:
//            case Action.NO_FIRE:
//                return SimpleAimingModule.getNoFiringInstructions(bot, noFiringLook);
//        }

    }

    /**
     * * Point the enemy and shoot.
     * @param fd
     * @param playerPos
     * @return
     */
    static public FiringInstructions getFastFiringInstructions(
            FiringDecision fd, MapBotBase bot) {
        Vector3f to = new Vector3f(fd.enemyInfo.getBestVisibleEnemyPart(bot));
        Vector3f fireDir = CommFun.getNormalizedDirectionVector(bot.getBotPosition(), to);
//		bot.dtalk.addToLog("Fast firing.");
        return new FiringInstructions(fireDir);
    }

    private void executeAction(Action action) {
//        actionFireMode = action.getShootingMode();

        if (Action.isProhibited(action)) {
            return;
        }

        int wpind = action.actionToInventoryIndex();

        if (bot.getCurrentWeaponIndex() != wpind) {
            if (bot.botHasItem(wpind)) {
                bot.changeWeaponToIndex(wpind);
            }
//            if (bot.botHasItem(wpind)) System.out.println("chng wpn to: "+CommFun.getGunName(wpind));

        }
    }
}
