import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import jason.runtime.*;

public class App{
	
	public static Thread getThreadByName(String threadName) {
	    for (Thread t : Thread.getAllStackTraces().keySet()) {
	        if (t.getName().equals(threadName)) return t;
	    }
	    return null;
	}
	
	public static void replaceSelected(String replaceWith, String filename, String mode, String cost) {
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
	        		inputStr += "\tenvironment: jasonenv.SUMOEnv(\"" + mode + "\",\"" + replaceWith + "\", \"" + cost + "\")";
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
		Scanner reader = new Scanner(System.in);
		System.out.println("Number of simulations:");
		int n = reader.nextInt();
		System.out.println("Mode (--braess/--no-braess):");
		String mode = reader.next();
		String cost = "";
		String c = "";
		String agent = "";
		while(!c.toLowerCase().equals("n")) {
			System.out.println("Number of agents:");
			String nAg = reader.next();
			agent += nAg + "-";
			System.out.println("Time gain using the new route (>0 && <1)");
			nAg = reader.next();
			agent += nAg + ",";
			System.out.println("Want to add more? (y/n):");
			c = reader.next();
		}
		agent = agent.substring(0, agent.length() - 1);
		reader.close();
		replaceSelected(agent, "bdi.mas2j", mode, cost);
		for(int i = 0; i < n; i ++) {
			System.out.println("Running " + i+1 + " simulation...");
			RunJasonProject.main(args);
			Thread t = getThreadByName("MAS-Launcher");
			t.join();
		}
	
	}
}
