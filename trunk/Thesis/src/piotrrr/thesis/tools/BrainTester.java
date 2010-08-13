/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import piotrrr.thesis.bots.rlbot.RLBot;
import piotrrr.thesis.bots.rlbot.RLCombatModule;
import piotrrr.thesis.bots.rlbot.rl.Action;
import pl.gdan.elsy.qconf.Brain;

/**
 *
 * @author piotrrr
 */
public class BrainTester {

    public static void main(String[] args) {
        Action[] actions = Action.getAllActionsArray();
        Brain brain;
        int hiddenLayerNeurons = 5;
        int[] hiddenLayers = new int[actions.length];
        for (int i = 0; i < hiddenLayers.length; i++) {
            hiddenLayers[i] = hiddenLayerNeurons;
        }
        RLBot bot = new RLBot("tmp", "");
        RLCombatModule cm = new RLCombatModule(bot);
//        brain = new Brain(cm, actions, hiddenLayers);
        brain = new Brain(cm, actions);
        try {
            brain.load("RLBot-0-b1");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(brain.toString());
        brain.printStats();

//        brain.

    }
}
