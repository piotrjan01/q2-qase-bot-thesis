/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package piotrrr.thesis.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Writes to a file.
 * Overwriting !!!!!!
 * @author piotrrr
 */
public class FileLogger {

	public FileLogger(String fname) {
		logFile = fname;
	}

	protected String logFile = null;

	protected File f = null;

	protected Writer out = null;

	public void addToLog(String toAdd) {
		try {
			if (f == null) {
				f = new File(logFile);
				out = new BufferedWriter(new FileWriter(f, false));
			}

			out.write(toAdd);
			out.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}

