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

    public static int dialogsCount = 0;
    private static int maxDialogsCount = 10;

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
        if (dialogsCount >= maxDialogsCount) return;
        dialogsCount++;
        MyPopUpDialog mdb = new MyPopUpDialog(title, message, type);
        Thread t = new Thread(mdb);
        t.setName("dialogBox");
        t.start();
    }

    public void run() {
        JOptionPane.showMessageDialog(
                null,
                message,
                title,
                type);
    }
}
