/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.common.stats;


import org.jfree.chart.labels.AbstractXYItemLabelGenerator;

import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;
import piotrrr.thesis.bots.tuning.NavConfig;

/**
 *
 * @author piotrrr
 */
public class HillClimbinIterLabelsAndTipsGenerator implements XYToolTipGenerator, XYItemLabelGenerator {

    NavConfig conf = new NavConfig();

    public HillClimbinIterLabelsAndTipsGenerator() {
        
    }

    public String generateToolTip(XYDataset ds, int ser, int cat) {
        if (ser == 0) return "";
        int x = (int) ds.getXValue(ser, cat);
        x--;
        String l = conf.getParamsName((x/2)%11);
        Number val = ds.getYValue(ser, cat);
        if (x % 2 == 0) l = "+"+l;
        else l = "-"+l;
        x++;
        return l;
    }

    public String generateLabel(XYDataset arg0, int arg1, int arg2) {
        return generateToolTip(arg0, arg1, arg2);
    }

}
