/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.bots.tuning;

/**
 *
 * @author piotrrr
 */
public interface OptProcess extends Runnable {

    public void stopProcess();

    public void unblockProcess();

}
