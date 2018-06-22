import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;

import jason.runtime.*;

public class App{
	
	static String mode;
	static String agent;
	static int numberOfSimulations;
	
	public static Thread getThreadByName(String threadName) {
	    for (Thread t : Thread.getAllStackTraces().keySet()) {
	        if (t.getName().equals(threadName)) return t;
	    }
	    return null;
	}
	
	public static void replaceSelected(String replaceWith, String filename, String mode) {
	    try {
	        // input the file content to the StringBuffer "input"
	        BufferedReader file = new BufferedReader(new FileReader(filename));
	        String line;
	        ArrayList<String> inputBuffer = new ArrayList<String>();

	        while ((line = file.readLine()) != null) {
	            inputBuffer.add(line);
	        }
	        String inputStr = "";
	        for(int i = 0; i < inputBuffer.size(); i++) {
	        	if(inputBuffer.get(i).contains("environment")) {
	        		inputStr += "\tenvironment: jasonenv.SUMOEnv(\"" + mode + "\",\"" + replaceWith + "\")";
	        	} 
	        	else
	        		inputStr += inputBuffer.get(i);
	        	inputStr += "\n";
	        }

	        file.close();
	        
	        // write the new String with the replaced line OVER the same file
	        FileOutputStream fileOut = new FileOutputStream(filename);
	        fileOut.write(inputStr.getBytes());
	        fileOut.close();

	    } catch (Exception e) {
	        System.out.println("Problem reading file.");
	    }
	}
	
	public static void main(String[] args) throws InterruptedException {
		replaceSelected(agent, "bdi.mas2j", mode);
		for(int i = 0; i < numberOfSimulations; i ++) {
			System.out.println("Running " + (i+1) + " simulation...");
			RunJasonProject.main(args);
			Thread t = getThreadByName("MAS-Launcher");
			t.join();
		}
	
	}
}
