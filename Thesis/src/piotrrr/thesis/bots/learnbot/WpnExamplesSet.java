/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.bots.learnbot;

import java.io.Serializable;
import java.util.HashMap;
import org.apache.log4j.Logger;
import piotrrr.thesis.common.CommFun;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author piotrrr
 */
public class WpnExamplesSet implements Serializable {

    public HashMap<Integer, WpnExamples> examples = new HashMap<Integer, WpnExamples>();

    private static Logger log = Logger.getLogger(WpnExamplesSet.class);

    public static WpnExamplesSet readWpnExamplesSetFromFile(String file) throws Exception {
        WpnExamplesSet ret = new WpnExamplesSet();
        WpnExamples all = new WpnExamples();

        log.debug("Reading examples from file: "+file);
        all.readExamplesFromFile(file);

        for (int i=0; i<all.instances.numInstances(); i++) {
            Instance inst = all.instances.instance(i);
            int wpn = (int)inst.value(0);
            if (ret.examples.containsKey(wpn)) {
                WpnExamples we = ret.examples.get(wpn);
                we.instances.add(inst);
            }
            else {
                log.debug("Found new weapon in examples: "+wpn);
                WpnExamples we = new WpnExamples();
                we.instances = new Instances(all.instances);
                we.instances.delete();
                we.instances.add(inst);
                ret.examples.put(wpn, we);
            }
        }
        log.debug("Finished reading examples set from file");

        return ret;
    }

    @Override
    public String toString() {
        String s = this.getClass().getName()+":\n";
        s+="Examples for weapons: \n";
        for (int i : examples.keySet()) {
            s+="wpn="+CommFun.getGunName(i)+" examples-count="+examples.get(i).instances.numInstances()+"\n";
        }
        return s;
    }



}
