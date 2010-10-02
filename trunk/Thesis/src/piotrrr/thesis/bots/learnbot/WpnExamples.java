/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.bots.learnbot;

import java.io.File;
import java.io.FileNotFoundException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import soc.qase.tools.vecmath.Vector3f;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

/**
 *
 * @author piotrrr
 */
public class WpnExamples {

    Instances instances = null;
    private Logger log = Logger.getLogger(WpnExamples.class);

    public void readExamplesFromFile(String src) throws Exception {
        if (instances != null) {
            log.warn("Reading new instances, but old instances was not null!");
        }
        CSVLoader loader = new CSVLoader();
        File f = new File(src);
        if ( ! f.exists() ) throw new FileNotFoundException(src);
        loader.setSource(f);
        instances = DataSource.read(loader);
        log.debug("Read examples: "+instances.numInstances());
    }

    public void appendExamplesFromFile(String src) throws Exception {
        CSVLoader loader = new CSVLoader();
        File f = new File(src);
        if ( ! f.exists() ) throw new FileNotFoundException(src);
        loader.setSource(f);
        Instances inst = DataSource.read(loader);

        int i = 0;
        for (i = 0; i < inst.numInstances(); i++) {
            instances.add(inst.instance(i));
        }
        log.info("Appended " + i + " instances.");
    }

    public void addInstance(int wpn, double dist0, double dist1, Vector3f enemeyMovDir, Vector3f fireDir) {
        //"wpn,dist0,dist1,emx,emy,emz,fdx,fdy,fdz"
        double[] vals = new double[9];
        vals[0] = wpn;
        vals[1] = dist0;
        vals[2] = dist1;
        vals[3] = enemeyMovDir.x;
        vals[4] = enemeyMovDir.y;
        vals[5] = enemeyMovDir.z;
        vals[6] = fireDir.x;
        vals[7] = fireDir.y;
        vals[8] = fireDir.z;

        Instance i = new Instance(1, vals);
        instances.add(i);

    }

    /**
     * For testing
     * @param args
     */
    public static void main(String [] args) {
        Logger log = Logger.getLogger(WpnExamples.class);
        BasicConfigurator.configure();
        WpnExamples ex = new WpnExamples();
        try {
            ex.readExamplesFromFile("testing-set.csv");
            log.info("Nr of instances read: "+ex.instances.numInstances());
            ex.appendExamplesFromFile("testing-set.csv");
            log.info("Nr of instances after appending: "+ex.instances.numInstances());
            ex.addInstance(10, 10, 10, new Vector3f(10, 22, 12), new Vector3f(10, 22, 12));
            ex.addInstance(10, 10, 10, new Vector3f(10, 22, 12), new Vector3f(10, 22, 12));
            ex.addInstance(10, 10, 10, new Vector3f(10, 22, 12), new Vector3f(10, 22, 12));
            log.info("Nr of instances after adding: "+ex.instances.numInstances());
        } catch (Exception ex1) {
            log.error("Error when reading the file", ex1);
        }
    }
}
