package piotrrr.thesis.bots.tuning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

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
				out = new BufferedWriter(new FileWriter(f, true));
			}
			
			out.write(toAdd);
			out.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
