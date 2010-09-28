/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.weka;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import soc.qase.tools.vecmath.Vector3f;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdditiveRegression;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 *
 * @author piotrrr
 */
public class WekaTest {

    public static void saveToFile(String filename, Object o) throws Exception {

        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(o);

    }

    public static Object readFromFile(String fileName) throws Exception {
        Object r = null;

        FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        r = ois.readObject();

        return r;
    }

    public static void main(String[] args) {
        test2();
    }
    static AdditiveRegression ar = null;

    private static void test2() {
        try {
            ar = (AdditiveRegression) readFromFile("additive-regression-weka-model.model");
        } catch (Exception ex) {
            Logger.getLogger(WekaTest.class.getName()).log(Level.SEVERE, null, ex);
        }
//        System.out.println("AR: " + ar.toString());
        try {
            //should get -0.340244
            predictX("HYPERBLASTER", 1182, 1173, new Vector3f(-0.032718, 0.017606, 0.0054));
        } catch (Exception ex) {
            Logger.getLogger(WekaTest.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static double predictX(String wpn, double d0, double d1, Vector3f bv) throws Exception {
        //wpn,d0,d1,bx,by,bz,hx,hy,hz
        Instances uli = DataSource.read("instances-t1.csv");

        double[] vals = new double[7];
        vals[0] = uli.attribute(0).indexOfValue(wpn);
        vals[1] = d0;
        vals[2] = d1;
        vals[3] = bv.x;
        vals[4] = bv.y;
        vals[5] = bv.z;

        Instance ni = new Instance(1, vals);
        uli.add(ni);
        try {
            double pred = ar.classifyInstance(uli.lastInstance());
            return pred;
        } catch (Exception ex) {
            ex.printStackTrace();
            return Double.NaN;
        }
    }

    public static void test1() {
        //create columns
        //dist
        Attribute dist = new Attribute("distance");
        //class
        FastVector labels = new FastVector();
        labels.addElement("close");
        labels.addElement("medium");
        labels.addElement("far");
        Attribute cl = new Attribute("class", labels);
        //create columns list
        FastVector cols = new FastVector();
        cols.addElement(dist);
        cols.addElement(cl);
        //create instances
        Instances data = new Instances("test-data", cols, 0);
        //adding values
        Random r = new Random();
        for (int i = 0; i <
                300; i++) {
            int option = r.nextInt(3);
            double[] vals = new double[2];
            switch (option) {
                case 0:
                    vals[0] = r.nextDouble() * 300;
                    vals[1] = data.attribute(1).indexOfValue("close");
                    break;

                case 1:
                    vals[0] = 300 + r.nextDouble() * 300;
                    vals[1] = data.attribute(1).indexOfValue("medium");
                    break;

                case 2:
                    vals[0] = 600 + r.nextDouble() * 300;
                    vals[1] = data.attribute(1).indexOfValue("far");
                    break;

            }


            Instance inst = new Instance(1, vals);
            data.add(inst);
        }
//set class attribute

        data.setClass(data.attribute(1));
        //Classification with batch classifier
        J48 tree = new J48();
        try {
            //classify
            tree.buildClassifier(data);
            //evaluate
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(tree, data, 10, r);
            //print evaluation:
            System.out.println(eval.toSummaryString());
            System.out.println(eval.toMatrixString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
//Classifying some instances

        Instances uli = new Instances(data);
        for (int i = 0; i <
                10; i++) {
            Instance ni = new Instance(1, new double[]{r.nextDouble() * 1000, 0});
            uli.delete();
            uli.add(ni);
            try {
                double ic = tree.classifyInstance(uli.firstInstance());
                System.out.println("" + uli.firstInstance().value(0) + " -> " + uli.attribute(1).value((int) ic));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        try {
            //Print tree as if-then-else statement :)
            System.out.println(tree.toSource("DistClassifier"));
        } catch (Exception ex) {
            Logger.getLogger(WekaTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

