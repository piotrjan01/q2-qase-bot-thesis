package piotrrr.thesis.bots;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;


/**
 * The class encapsulating the basic configuration constants for the whole application.
 * Also contains methods to write and read it from the disk.
 * @author piotrrr
 */
public class AppConfig {

    /**
     * The name of the config file that will be used to save or read it
     */
    public static String configFileName = "config.cnf";
    /**
     * Path to Q2 main directory without slash at the end
     */
    public static String quakePath = "H:\\workspace\\inzynierka\\testing-env\\quake2-3_21\\quake2";
    /**
     * Maps dir with slash at the end
     */
    public static String botMapsDir = "H:\\workspace\\inzynierka\\SmartBot\\botmaps\\";
    /**
     * The name of a skin for a bot to use in game
     */
    public static String skinName = "male/voodoo";
    /**
     * Another skin name for a bot to use in game
     */
    public static String altSkinName = "female/voodoo";
    /**
     * The IP of the Q2 server
     */
    public static String serverIP = "127.0.0.1";
    /**
     * The port to use with the server
     */
    public static int serverPort = 27910;

    /**
     * Writes the config to the config file
     */
    public static void writeConfig() {

        try {
            PrintWriter pw = new PrintWriter(new FileWriter(configFileName));
            pw.println(quakePath);
            pw.println(botMapsDir);
            pw.println(skinName);
            pw.println(altSkinName);
            pw.println(serverIP);
            pw.println(serverPort);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the config from a file (if a file exists)
     */
    public static void readConfig() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(configFileName));
            quakePath = br.readLine();
            botMapsDir = br.readLine();
            skinName = br.readLine();
            altSkinName = br.readLine();
            serverIP = br.readLine();
            serverPort = Integer.parseInt(br.readLine());
            br.close();

            System.setProperty("QUAKE2", AppConfig.quakePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
