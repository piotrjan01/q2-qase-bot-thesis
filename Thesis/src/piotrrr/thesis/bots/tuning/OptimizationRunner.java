/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.tuning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import piotrrr.thesis.bots.botbase.BotBase;
import piotrrr.thesis.bots.learnbot.LearnBot;
import piotrrr.thesis.bots.referencebot.ReferenceBot;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.common.stats.BotStatistic;
import piotrrr.thesis.gui.AppConfig;
import piotrrr.thesis.gui.MyPopUpDialog;
import piotrrr.thesis.gui.OptimizationFrame;
import piotrrr.thesis.tools.Dbg;

/**
 *
 * @author piotrrr
 */
public class OptimizationRunner {

    private class BufferReader implements Runnable {

        BufferedReader br;

        public BufferReader(BufferedReader br) {
            this.br = br;
        }

        public void run() {
            String line;
            Thread.currentThread().setName("Q2BufferReader");
            try {
                while ((line = br.readLine()) != null) {
//                    System.out.println(line);
                    Thread.yield();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class BotConnector implements Runnable {

        BotBase bot;
        String ip;
        int port;
        public boolean connected = false;

        public BotConnector(BotBase bot, String ip, int port) {
            this.bot = bot;
            this.ip = ip;
            this.port = port;
        }

        public void run() {
            bot.setName(bot.getBotName());
            bot.connect(ip, port);
            connected = true;
            System.out.println("BotConnector: Bot " + bot.getBotName() + " connected!");
        }

        public void kill() {
        }
    }
    private HashMap<Integer, Process> childProcesses = new HashMap<Integer, Process>();
    private static OptimizationRunner instance = null;
    protected BotStatistic gameStats;
    protected TuningProcessBase optProcess = null;
    Thread optThread = null;

    private OptimizationRunner() {
    }

    public void runOptimization(int timescale, int iterations, int maxScore, String mapName, String alg, int repetitions, double tau) {

        if (alg.equals("Hill Climbing step decreasing")) {
            optProcess = new HillClimbingWithStepDecrease(timescale, iterations, maxScore, mapName, repetitions);
        } else if (alg.equals("Hill Climbing")) {
            optProcess = new HillClimbingProcess(timescale, iterations, maxScore, mapName, repetitions);
        } else if (alg.equals("No Change")) {
            optProcess = new NoChangeProcess(timescale, iterations, maxScore, mapName, repetitions);
        } else if (alg.equals("Simulated Annealing")) {
            optProcess = new SimulatedAnnealingProcess(timescale, iterations, maxScore, mapName, repetitions);
        } else if (alg.equals("Gradient Hill Climbing")) {
            optProcess = new HillClimbingWithGradient(timescale, iterations, maxScore, mapName, repetitions);
        } else if (alg.equals("Random search")) {
            optProcess = new LocalizedRandomSearch(timescale, iterations, maxScore, mapName, repetitions);
        }
        else {
            MyPopUpDialog.showMyDialogBox("Error", "Unknown algorithm: " + alg, MyPopUpDialog.error);
            return;
        }
        optProcess.tauThreshold = tau;
        
        optThread = new Thread(optProcess);
        optThread.setPriority(Thread.MIN_PRIORITY);
        optThread.setName("OptimizationProcess");
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

    public void unblockProcess() {
        if (optProcess == null) {
            return;
        }
        optProcess.unblockProcess();
    }

    public LearnBot connectLearnBot(int nr, int port) {
        LearnBot bot = null;
        bot = new LearnBot("LearnBot" + "-" + nr, AppConfig.altSkinName);
        bot = (LearnBot) connect(bot, AppConfig.serverIP, port);
        return bot;
    }

    public ReferenceBot connectReferenceBot(int nr, int port) {
        ReferenceBot bot = null;
        bot = new ReferenceBot("ReferenceBot" + "-" + nr, AppConfig.skinName);
        bot = (ReferenceBot) connect(bot, AppConfig.serverIP, port);
        return bot;
    }

    private BotBase connect(BotBase bot, String ip, int port) {
        BotConnector con = new BotConnector(bot, ip, port);
        Thread t = new Thread(con);
        t.setName("BotConnector");
        t.start();
        for (int i = 0; i < 100; i++) {
            System.out.println("Bot connecting... " + i);
            sleep(10);
            if (con.connected) {
                Dbg.prn("OK: Returning connected bot.");
                return bot;
            }
        }
        Dbg.prn("Done loop");
        if (!con.connected) {
            Dbg.prn("Disconnecting bot");
            con.bot.disconnect();
            Dbg.prn("Stopping and destroying connector thread");
            try {
                t.stop();
                t.destroy();
            } catch (Throwable e) {
            }
            t = null;
            Dbg.prn("Exception: Returning null bot.");
            return null;
        }
        Dbg.prn("OK: Returning connected bot. Outside the loop.");
        return bot;
    }

    private void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void runLocalQ2Server(int port, String mapName) {
        String timescale = "" + AppConfig.timeScale;
        String quakeExec = "quake2.exe";
//        String cmd =  + " +set dedicated 1 +set deathmatch 1 +map " +
//                mapName + " +set cheats 1 +set port " + port + " +set timescale " + timescale +
//                " +set rcon_password qwe123" + " +set dmflags 1544" + " +set timeout 99999";
//        String batName = "run" + port + ".bat";
        try {
            List<String> command = new ArrayList<String>();
            command.add(AppConfig.quakePath + "\\" + quakeExec);

            command.add("+set");
            command.add("deathmatch");
            command.add("1");
            command.add("+set");
            command.add("dedicated");
            command.add("1");
            command.add("+map");
            command.add(mapName);
            command.add("+set");
            command.add("cheats");
            command.add("1");
            command.add("+set");
            command.add("port");
            command.add("" + port);
            command.add("+set");
            command.add("timescale");
            command.add("" + timescale);
            command.add("+set");
            command.add("dmflags");
            command.add("1544");
            command.add("+set");
            command.add("timeout");
            command.add("99999");

            for (String s : command) {
                System.out.print(s + " ");
            }
            System.out.println();

            ProcessBuilder builder = new ProcessBuilder(command);
            Map<String, String> environ = builder.environment();
            builder.directory(new File(AppConfig.quakePath));

            final Process pr = builder.start();
            pr.getErrorStream();
            pr.getInputStream();
            pr.getOutputStream();
            InputStream is = pr.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            Thread writer = new Thread(new BufferReader(br));
            writer.setPriority(Thread.MIN_PRIORITY);
            writer.start();

            System.out.println("Program terminated!");
            childProcesses.put(port, pr);
//            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            Process p = Runtime.getRuntime().exec(batName);

//        } catch (Exception err) {
//            err.printStackTrace();
//        }
    }

    public int runLocalQ2ServerOld(int port, String mapName) {
        String timescale = "" + AppConfig.timeScale;
        String quakeExec = "quake2.exe";
        String cmd = AppConfig.quakePath + "\\" + quakeExec + " +set dedicated 1 +set deathmatch 1 +map " +
                mapName + " +set cheats 1 +set port " + port + " +set timescale " + timescale +
                " +set rcon_password qwe123" + " +set dmflags 1544" + " +set timeout 99999";
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
            childProcesses.put(port, p);
            return childProcesses.size() - 1;
        } catch (Exception err) {
            err.printStackTrace();
            return -1;
        }
    }

    public void clearProcesses() {
        for (Process p : childProcesses.values()) {
            p.destroy();
        }
        childProcesses.clear();
    }

    public void clearProcess(int port) {
        Process p = childProcesses.get(port);
        if (p != null) {
            p.destroy();
            childProcesses.remove(port);
        }
    }

    public static OptimizationRunner getInstance() {
        if (instance == null) {
            instance = new OptimizationRunner();
        }
        return instance;
    }

    public void handleEvaluationResults(DuelEvalResults res) {
        try {
            OptimizationFrame.getOptFrameInstance().addResults(res);
            if (TuningProcessBase.debug) return; // dont save on debug
            String fileToSave = "current-opt-backup";
            System.out.println("Saving results to file: " + fileToSave);
            CommFun.saveToFile(fileToSave, OptimizationFrame.getOptFrameInstance().getOptResults());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
