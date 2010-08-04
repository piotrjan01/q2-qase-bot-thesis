/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.common.fsm;

/**
 *
 * @author Piotr Gwizdała
 */
public interface StateBot {

    public String getCurrentStateName();

    public void say(String txt);

}
