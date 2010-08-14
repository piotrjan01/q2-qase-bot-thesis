/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package piotrrr.thesis.gui;

import javax.swing.JOptionPane;

/**
 *
 * @author piotrrr
 */
public class MyPopUpDialog implements Runnable {

    public static final int error = JOptionPane.ERROR_MESSAGE;
    public static final int warning = JOptionPane.WARNING_MESSAGE;
    public static final int info = JOptionPane.INFORMATION_MESSAGE;
    String title;
    String message;
    int type;

    public MyPopUpDialog(String title, String message, int type) {
        this.title = title;
        this.message = message;
        this.type = type;
    }

    public static void showMyDialogBox(String title, String message, int type) {
        MyPopUpDialog mdb = new MyPopUpDialog(title, message, type);
        new Thread(mdb).start();
    }

    public void run() {
        JOptionPane.showMessageDialog(
                null,
                message,
                title,
                type);
    }
}
