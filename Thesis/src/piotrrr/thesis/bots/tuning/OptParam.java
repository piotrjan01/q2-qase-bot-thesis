/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.bots.tuning;

import java.io.Serializable;
import java.util.Random;

/**
 *
 * @author piotrrr
 */
public class OptParam implements Serializable {

    private double value;
    private double min;
    private double max;
    private double step;

    public OptParam(double value, double min, double max, double step) {
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) throws Exception {
        if (value >= min && value <= max)
            this.value = value;
        throw new Exception("Wrong value: "+toString());
    }

    @Override
    public String toString() {
        return "Parameter. Value: "+value+" min: "+min+" max: "+max+" step: "+step;
    }

    public void setRandomValue() {
        Random r = new Random();
        double range = max-min;
        int vals = (int) (range / step);
        int rv = r.nextInt(vals);
        value = min + rv*step;
    }








}
