package piotrrr.thesis.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import soc.qase.ai.waypoint.WaypointMap;
import soc.qase.ai.waypoint.WaypointMapGenerator;

/**
 * This is a simple tool that facilitates creating WaypointMaps from recorded demos
 * in DM2 format.
 * @author Piotr Gwizda³a
 */
public class MapFromDemo {

	/**
	 * Main method of the tool. Starts interactive command-line dialog.
	 * @param args
	 */
	public static void main(String[] args) {
		prn("Current directory: "+System.getProperty("user.dir"));
		prn("Please enter a path\\name of the DM2 file: ");
		String path = readString();
		WaypointMap map = WaypointMapGenerator.generate(path, 0.05f);
		prn("Demo read. There are approximately "+map.getAllNodes().length*20+" nodes in demo.");
		prn("Enter the filename, where the generated map shall be saved: ");
		String saveMap = readString();
		boolean done = false;
		while (!done) {
			prn("Please enter the number of nodes to be generated or \n" +
					"the percent of nodes to be generated (e.g. 100 for 100 nodes or 0.5 for 50%): ");
			float nodes = Float.parseFloat(readString());
			try {
				map = WaypointMapGenerator.generate(path, nodes);
				done = true;
			}
			catch (Exception e) {
				prn("Caught exception: "+e.getMessage());
				done = false;
			}
		}
		map.saveMap(saveMap);
		prn("Done!");
	}
	
	/**
	 * Used to print the requests on the screen.
	 * @param s
	 */
	static void prn(Object s) {
		System.out.println(s.toString());
	}
	
	/**
	 * Used to read the string from standard input.
	 * @return
	 */
	static String readString() {
		String s;
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		try {
			s = br.readLine();
		}
		catch (Exception e) {
			prn("error reading input!");
			return readString();
		}
		return s;
	}
	
}
