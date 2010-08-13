/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.tools;

/**
 *
 * @author piotrrr
 */
public class Timer {
    
    long t0;

    long elapsed;

    boolean paused = true;

    String name = "no-name";

    public Timer(String name) {
        this.name = name;
        paused = true;
        elapsed = 0;
    }

    public void startFromZero() {
        paused = false;
        elapsed = 0;
        t0 = System.nanoTime();
    }

    public void reset() {
        paused = true;
        elapsed = 0;
        t0 = 0;
    }

    public void resume() {
        paused = false;
        t0 = System.nanoTime();
    }

    public void pause() {
        if (paused) return;
        elapsed += System.nanoTime() - t0;
        paused = true;
    }


    public long getElapsedTime() {
        if (paused) return elapsed;
        return elapsed + System.nanoTime() - t0;
    }

    @Override
    public String toString() {
        return name+": "+(double)getElapsedTime()/1000000d+"ms";
    }

    public String toStringAsPercentOf(long time) {
        return name+": "+(int)((double)(100*getElapsedTime())/time)+"%";
    }

    @Override
    public boolean equals(Object obj) {
        Timer o = (Timer)obj;
        return (o.name.equals(name));
    }

    @Override
    public int hashCode() {
        return name.length();
    }







}
