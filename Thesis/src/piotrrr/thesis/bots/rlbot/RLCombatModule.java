package piotrrr.thesis.bots.rlbot;

import java.util.LinkedList;
import java.util.Random;
import piotrrr.thesis.bots.rlbot.rl.Action;
import piotrrr.thesis.bots.mapbotbase.MapBotBase;
import piotrrr.thesis.common.combat.*;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.navigation.FuzzyGlobalNav;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.tools.Dbg;
import pl.gdan.elsy.qconf.Brain;
import soc.qase.state.PlayerGun;
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

    public static int count = 0;

    public static final int actionTime = 20;
    public Action currentAction = null;
    int currentActionTime = 0;
    int hiddenLayerNeurons = 6;
    RLBot bot;
    int lastEnemyId = -1;
    public RLBotPerception perception;
//    double actionReward = 0;
//    int actionFireMode = Action.NO_FIRE;
    Action[] actions = Action.getAllActionsArray();
    public Brain brain;
    boolean unipolar = true;

    public RLCombatModule(RLBot bot) {
        this.bot = bot;

        count++;
//        hiddenLayerNeurons = (int) getRandParam(4, 10, 2);
        int[] hiddenLayers = new int[actions.length];
        for (int i = 0; i < hiddenLayers.length; i++) {
            hiddenLayers[i] = hiddenLayerNeurons;
        }
        perception = new RLBotPerception(bot);
        brain = new Brain(perception, actions, hiddenLayers);

//        if (count == 1)
//            brain.setAlpha(0.1); //learning rate
//        else
        brain.setAlpha(0.7);
        brain.setGamma(0.3); //discounting rate
        brain.setLambda(0.2); //trace forgetting
//        b.setUseBoltzmann(true);
//        b.setTemperature(0.001);
        brain.setRandActions(0.1); //exploration

//        brain.setAlpha(getRandParam(0.4, 0.8, 0.2)); //learning rate
//        brain.setGamma(getRandParam(0.5, 0.9, 0.2)); //discounting rate
//        brain.setLambda(getRandParam(0.2, 0.9, 0.2)); //trace forgetting
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
                "\nlambda=" + brain.getLambda() + " rand=" + brain.getRandActions() + "\n"+"hl-size="+hiddenLayerNeurons+"\n";
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
        
        currentActionTime++;
        boolean reloading = bot.getWorld().getPlayer().getPlayerGun().isCoolingDown();
        Vector3f noFiringLook = fd.enemyInfo.predictedPos == null ? fd.enemyInfo.getObjectPosition() : fd.enemyInfo.predictedPos;
        if (reloading) {
            return SimpleAimingModule.getNoFiringInstructions(bot, noFiringLook);
        }

        if (currentActionTime >= actionTime || fd.enemyInfo.ent.getNumber() != lastEnemyId) {
            currentActionTime = 0;


            bot.rewardsCount++;
            bot.totalReward += perception.getReward();

            if (BotStatistic.getInstance() != null) {
                BotStatistic.getInstance().addReward(bot.getBotName(), perception.getReward(), bot.getFrameNumber());
            }

            //if the enemy changed, we end the episode
            if (fd.enemyInfo.ent.getNumber() != lastEnemyId) {
                lastEnemyId = fd.enemyInfo.ent.getNumber();
                currentAction = null;
                brain.reset();
            }
            //choose new action and execute it
            perception.perceive(); //calls updateInputValues
            brain.count(Action.getActionsAvailability(bot)); //chooses action and updates with last reward.
            currentAction = actions[brain.getAction()];
            executeAction(currentAction);

            perception.resetReward();
        }
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
        String say = "";
        say += "wpns=";
        for (int i = 7; i < 18; i++) {
            if (bot.botHasItem(i)) {
                if (bot.botHasItem(PlayerGun.getAmmoInventoryIndexByGun(i))) {
                    say += "1";
                } else {
                    say += "A";
                }
            } else {
                say += "0";
            }
        }
//        System.out.println("===================================> executing action: " + action.toString() + " (" + say + ")");

        int wpind = action.actionToInventoryIndex();

        if (bot.getCurrentWeaponIndex() != wpind) {
            if (bot.botHasItem(wpind) && !Action.isProhibited(action)) {
                bot.changeWeaponToIndex(wpind);
            }
            else bot.changeWeaponToIndex(7); //blaster
//            if (bot.botHasItem(wpind)) System.out.println("chng wpn to: "+CommFun.getGunName(wpind));

        }

        switch (action.getCombatMove()) {
            case Action.CB_RETREAT:
                bot.plan = FuzzyGlobalNav.getEnemyRetreatPlan(bot);
//                Dbg.prn(bot.getBotName() + ">----------> RETREAT!");
                break;
            case Action.CB_DISTANT:
                bot.plan = FuzzyGlobalNav.getEnemyDistPosPlan(bot, bot.fd);
//                Dbg.prn(bot.getBotName() + ">----------> FROM DISTANCE!");
                break;
            case Action.CB_CLOSER:
            default:
                bot.plan = FuzzyGlobalNav.getEnemyEngagingPlan(bot, bot.plan);
//                Dbg.prn(bot.getBotName() + ">----------> ENGAGING!");
        }

    }
}
