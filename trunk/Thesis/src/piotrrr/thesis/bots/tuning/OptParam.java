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

    public OptParam(OptParam original) {
        this.value = original.value;
        this.min = original.min;
        this.max = original.max;
        this.step = original.step;
    }

    public double getValue() {
        return value;
    }

    public boolean setValue(double value) {
        if (value >= min && value <= max) {
            this.value = value;
            return true;
        }
        else {
            if (value > max) this.value = max;
            if (value < min) this.value = min;
            return false;
        }
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

    public boolean inc() {
        if (value >= max) {
            value = max;
            return false;
        }
        value += step;
        if (value > max) value = max;
        return true;
    }

    public boolean dec() {
        if (value <= min) {
            value = min;
            return false;
        }
        value -= step;
        if (value < min) value = min;
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OptParam) {
            OptParam p = (OptParam) obj;
            return p.value == value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.min) ^ (Double.doubleToLongBits(this.min) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.max) ^ (Double.doubleToLongBits(this.max) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.step) ^ (Double.doubleToLongBits(this.step) >>> 32));
        return hash;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public double getStep() {
        return step;
    }

    











}
