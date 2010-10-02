/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;
import piotrrr.thesis.common.CommFun;
import piotrrr.thesis.gui.AppConfig;
import soc.qase.file.bsp.BSPParser;
import soc.qase.file.dm2.DM2Parser;
import soc.qase.state.Angles;
import soc.qase.state.Entity;
import soc.qase.state.World;
import soc.qase.tools.vecmath.Vector3f;

/**
 *
 * @author piotrrr
 */
public class DemoToCsv {

    private static BSPParser bsp = null;
    private static FileLogger fLog = null;
    private static String comma = ",";
    private static String csv = "";

    public static void main(String[] args) {

        AppConfig.readConfig();





        String demo, map;
        boolean auto = true;

        String mapPrefix = AppConfig.quakePath + "\\baseq2\\maps\\";
        String demosPath = "./";
        if (auto) {
            for (String s : CommFun.getAllFilesInDirectory(demosPath)) {
                if ( ! s.endsWith(".dm2")) continue;
                prn("Reading data from: "+s);
                demo = demosPath+s;
                csv = "data-" + s;
                saveDemoDataToCsv(demo);
            }
        } else {
            prn("Current directory: " + System.getProperty("user.dir"));
            prn("Give the path to the demo file: ");
            demo = readString();
//            prn("Give map name that was played (from folder: " + AppConfig.botMapsDir + "):");
//            map = mapPrefix + readString();
//            prn("Give csv file name (without extension): ");
            csv = "data-" + demo;
            prn("Creating csv...");
            saveDemoDataToCsv(demo);
        }
        
        prn("Done!");
    }

    public static void saveDemoDataToCsv(String demo) {
        DM2Parser dm = new DM2Parser(demo);

        World w = dm.getNextWorld();

//        prn("Loading bsp file: "+map);
//        bsp = new BSPParser(map);

        if (dm == null || w == null /*|| bsp == null*/) {
            prn("ERROR!");
            return;
        }

        Vector3f enLastPos = new Vector3f();

        while (w != null) {

            if (w.getFrame() % 1000 == 0) prn(demo+": "+w.getFrame());

            Vector opps = w.getOpponents(true);
            Vector3f pPos = new Vector3f(w.getPlayer().getPlayerMove().getOrigin());

            if (opps == null || opps.size() != 1) {
                w = dm.getNextWorld();
                continue;
            }

//            prn("" + w.getFrame() + ": " + "Got opponents: " + opps.size());
//            Entity enemy = getClosestVisible(opps, pPos);
            Entity enemy = (Entity) opps.firstElement();

            if (enemy == null) {
                w = dm.getNextWorld();
                continue;
            }



            if (w.getPlayer().getPlayerGun().isFiringOrCoolingDown()) {

                Vector3f enemyPos = enemy.getObjectPosition();
                Vector3f predPos = new Vector3f(enemyPos);
                predPos.add(CommFun.getMovementBetweenVectors(enLastPos, enemyPos));

                Vector3f enDir = CommFun.getNormalizedDirectionVector(pPos, enemyPos);
                Vector3f predEnDir = CommFun.getNormalizedDirectionVector(pPos, predPos);

                Angles ang = w.getPlayer().getPlayerGun().getAngles();
//                prn("" + pPos);
//                prn("yaw=" + ang.getYaw() + " pitch=" + ang.getPitch());

                Vector3f fireAt = new Vector3f(pPos);
                fireAt.x += (float) Math.sin(ang.getYaw());
                fireAt.y += (float) Math.cos(ang.getYaw());
                fireAt.x += (float) Math.sin(ang.getPitch());

                Vector3f fireAtRel = CommFun.getNormalizedDirectionVector(pPos, fireAt);
                fireAtRel.sub(enDir);


                Vector3f moveRel = new Vector3f(predEnDir);
                moveRel.sub(enDir);

                int wpn = w.getPlayer().getPlayerGun().getInventoryIndex();

                double dist0 = CommFun.getDistanceBetweenPositions(pPos, enemyPos);
                double dist1 = CommFun.getDistanceBetweenPositions(pPos, predPos);

                saveExamplesToFile(wpn, dist0, dist1, moveRel, fireAtRel);
            }

            enLastPos = enemy.getObjectPosition();
            w = dm.getNextWorld();

        }

    }

    protected static void saveExamplesToFile(int wpn, double dist0, double dist1, Vector3f enemyMove, Vector3f fireDir) {
        //"wpn,dist0,dist1,emx,emy,emz,fdx,fdy,fdz"

        if (fLog == null) {
            fLog = new FileLogger(csv + ".csv");
            fLog.addToLog("wpn" + comma + "dist0" + comma + "dist1" + comma +
                    "emx" + comma + "emy" + comma + "emz" + comma +
                    "fdx" + comma + "fdy" + comma + "fdz\n");
        }
        fLog.addToLog(wpn + comma + dist0 + comma + dist1 + comma +
                enemyMove.x + comma + enemyMove.y + comma + enemyMove.z + comma +
                fireDir.x + comma + fireDir.y + comma + fireDir.z + "\n");
    }

    private static Entity getClosestVisible(Vector ents, Vector3f myPos) {
        double minDist = Double.MAX_VALUE;
        Entity ret = null;
        for (Object o : ents) {
            Entity e = (Entity) o;
            double dist = CommFun.getDistanceBetweenPositions(myPos, e.getObjectPosition());
            if (dist < minDist && bsp.isVisible(myPos, e.getObjectPosition())) {
                ret = e;
                minDist = dist;
            }
        }
        return ret;
    }

    /**
     * Used to print the requests on the screen.
     * @param s
     */
    static void prn(Object s) {
        System.out.println(s.toString());
    }

    /**
     * Used to read the string from standard input.
     * @return
     */
    static String readString() {
        String s;
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        try {
            s = br.readLine();
        } catch (Exception e) {
            prn("error reading input!");
            return readString();
        }
        return s;
    }
}
