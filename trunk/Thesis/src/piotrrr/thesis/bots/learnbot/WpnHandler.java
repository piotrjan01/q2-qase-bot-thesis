/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.learnbot;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import soc.qase.tools.vecmath.Vector3f;
import weka.classifiers.Classifier;
import weka.classifiers.trees.M5P;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author piotrrr
 */
public class WpnHandler {

    Classifier xLearner = new M5P();
    Classifier yLearner = new M5P();
    Classifier zLearner = new M5P();
    Instances xInst, yInst, zInst;
    static Logger log = Logger.getLogger(WpnHandler.class.getName());

    public void learn(WpnExamples examples) throws Exception {
        //wpn,d0,d1,bx,by,bz,hx,hy,hz
        Random r = new Random();

        xInst = examples.instances.resample(r);
        yInst = examples.instances.resample(r);
        zInst = examples.instances.resample(r);

        xInst.deleteAttributeAt(8);
        xInst.deleteAttributeAt(7);
        xInst.setClassIndex(6);

        yInst.deleteAttributeAt(8);
        yInst.deleteAttributeAt(6);
        yInst.setClassIndex(6);

        zInst.deleteAttributeAt(6);
        zInst.deleteAttributeAt(7);
        zInst.setClassIndex(6);

        log.info("Learning x.");
        xLearner.buildClassifier(xInst);
        log.info("Learning y.");
        yLearner.buildClassifier(yInst);
        log.info("Learning z.");
        zLearner.buildClassifier(zInst);
        log.info("Learning finished.");


        //To get smaller memmory use
        log.info("Cleaning up the temporary instances");
        xInst.delete();
        yInst.delete();
        zInst.delete();

    }

    public Vector3f classify(int wpn, double dist0, double dist1, Vector3f enemyMovDir) throws Exception {
        //wpn,d0,d1,bx,by,bz,hx,hy,hz        

        double x = classifyForGivenClassAttr(wpn, dist0, dist1, enemyMovDir, xInst, xLearner);
        double y = classifyForGivenClassAttr(wpn, dist0, dist1, enemyMovDir, yInst, yLearner);
        double z = classifyForGivenClassAttr(wpn, dist0, dist1, enemyMovDir, zInst, zLearner);

        return new Vector3f(x, y, z);
    }

    private double classifyForGivenClassAttr(int wpn, double dist0, double dist1, Vector3f enemyMovDir, Instances insts, Classifier cl) throws Exception {

        double[] vals = new double[7];

        vals[0] = wpn;
        vals[1] = dist0;
        vals[2] = dist1;
        vals[3] = enemyMovDir.x;
        vals[4] = enemyMovDir.y;
        vals[5] = enemyMovDir.z;

        Instance i = new Instance(1, vals);
        insts.add(i);
        return cl.classifyInstance(i);
    }

    /**
     * Testing
     * @param args
     */
    public static void main(String[] args) {

        WpnHandler wh = new WpnHandler();
        WpnExamples we = new WpnExamples();

        try {

            we.readExamplesFromFile("testing-set.csv");
            log.info("Nr of examples read: " + we.instances.numInstances());
            wh.learn(we);

            log.info("Trying to classify some stuff...");
            for (int i = 0; i < 10; i++) {
                Vector3f res = wh.classify(10, 1000, 1010, new Vector3f(10, 0, 0));
                log.info("x=" + res.x + " y=" + res.y + " z=" + res.z);
            }


        } catch (Exception ex) {
            Logger.getLogger(WpnHandler.class.getName()).log(Level.SEVERE, "Exception during test!11", ex);
        }
    }
}
