/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import piotrrr.thesis.bots.learnbot.LearnBot;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.gui.AppConfig;
import piotrrr.thesis.gui.OptimizationFrame;

/**
 *
 * @author piotrrr
 */
public class OptimizationRunner {

    private LinkedList<Process> childProcesses = new LinkedList<Process>();
    private static OptimizationRunner instance = null;
    protected BotStatistic gameStats;
    protected OptProcess optProcess = null;
    Thread optThread = null;

    private OptimizationRunner() {
    }

    public void runOptimization(int timescale, int iterations, int maxScore, String mapName) {
        optProcess = new RandOptProcess(timescale, iterations, maxScore, mapName);
        optThread = new Thread(optProcess);
        optThread.start();
    }

    public void stopOptimization() {
        if (optProcess != null) {
            optProcess.stopProcess();
        }
        if (optThread == null) {
            clearProcesses();
            return;
        }

        optThread.interrupt();
        optThread = null;
        clearProcesses();
    }

    public LearnBot connectLearnBot(int nr, int port) {
        LearnBot bot = null;
        bot = new LearnBot("LearnBot" + "-" + nr, AppConfig.altSkinName);
        bot.connect(AppConfig.serverIP, port);
        return bot;
    }

    public void runLocalQ2Server(int port, String mapName) {
        String timescale = "" + AppConfig.timeScale;
        String quakeExec = "quake2.exe";
        String cmd = AppConfig.quakePath + "\\" + quakeExec + " +set dedicated 1 +set deathmatch 1 +map " +
                mapName + " +set cheats 1 +set port " + port + " +set timescale " + timescale +
                " +set rcon_password qwe123" + " +set dmflags 1544";
        String batName = "run" + port + ".bat";
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(batName));
            pw.println("cd " + AppConfig.quakePath);
            pw.println(cmd);
            pw.println();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Process p = Runtime.getRuntime().exec(batName);
            childProcesses.add(p);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void clearProcesses() {
        for (Process p : childProcesses) {
            p.destroy();
        }
        childProcesses.clear();
    }

    public static OptimizationRunner getInstance() {
        if (instance == null) {
            instance = new OptimizationRunner();
        }
        return instance;
    }

    public void handleIteratinResults(EvalResults res) {
        OptimizationFrame.getOptFrameInstance().addResults(res);
    }
}
