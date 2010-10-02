/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.learnbot;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Random;
import org.apache.log4j.Logger;
import soc.qase.tools.vecmath.Vector3f;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdditiveRegression;
import weka.classifiers.trees.M5P;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author piotrrr
 */
public class WpnHandler implements Serializable {

    Classifier xLearner = new AdditiveRegression(new M5P());
    Classifier yLearner = new AdditiveRegression(new M5P());
    Classifier zLearner = new AdditiveRegression(new M5P());
    
    Instances xInst, yInst, zInst;
    static Logger log = Logger.getLogger(WpnHandler.class);

    public String evaluate(WpnExamples examples) throws Exception {
        //wpn,d0,d1,bx,by,bz,hx,hy,hz
        Random r = new Random();

        Instances xDs, yDs, zDs;

        xDs = examples.instances.resample(r);
        yDs = examples.instances.resample(r);
        zDs = examples.instances.resample(r);

        xDs.deleteAttributeAt(8);
        xDs.deleteAttributeAt(7);
        xDs.setClassIndex(6);

        yDs.deleteAttributeAt(8);
        yDs.deleteAttributeAt(6);
        yDs.setClassIndex(6);

        zDs.deleteAttributeAt(6);
        zDs.deleteAttributeAt(7);
        zDs.setClassIndex(6);

        Evaluation ex, ey, ez;

        String ret = "";

        log.info("Evaluating x.");
        ex = new Evaluation(xDs);
        ex.evaluateModel(xLearner, xDs);
        log.info(ex.toSummaryString());
        ret += "X: "+ex.toSummaryString();
        double rmse = ex.rootMeanSquaredError();

        log.info("Evaluating y.");
        ey = new Evaluation(yDs);
        ey.evaluateModel(yLearner, yDs);
        log.info(ey.toSummaryString());
        ret += "Y: "+ey.toSummaryString();
        rmse += ey.rootMeanSquaredError();

        log.info("Evaluating z.");
        ez = new Evaluation(zDs);
        ez.evaluateModel(zLearner, zDs);
        log.info(ez.toSummaryString());
        ret += "Z: "+ez.toSummaryString();
        rmse += ez.rootMeanSquaredError();
        
        log.info("Evaluating finished.");


        //To get smaller memmory use
        log.info("Cleaning up the temporary instances");
        xDs.delete();
        yDs.delete();
        zDs.delete();

        String nr = NumberFormat.getInstance().format((rmse*rmse)/3.0);
        return "\nClasses avg mean squared error: " +nr+"\n\n"+ret;

    }

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
        //insts.add(i);   <------------------------------- ?
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
            log.error("Exception during test!11", ex);
        }
    }

    @Override
    public String toString() {
        String ret = this.getClass().getSimpleName() + "\n";
        try {
            ret += "Learner used: " + xLearner.getClass().getSimpleName();
        } catch (Exception ex) {
            ret += "Something is wrong with the learner..." + ex.getMessage();
            log.error("Field not found?", ex);
        }
        return ret;
    }
}
