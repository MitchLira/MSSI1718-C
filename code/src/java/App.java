import java.util.Scanner;

import jason.runtime.*;

public class App{
	
	public static Thread getThreadByName(String threadName) {
	    for (Thread t : Thread.getAllStackTraces().keySet()) {
	        if (t.getName().equals(threadName)) return t;
	    }
	    return null;
	}
	
	public static void main(String[] args) throws InterruptedException {
		Scanner reader = new Scanner(System.in);
		System.out.println("Number of simulations:");
		int n = reader.nextInt();
		reader.close();
		for(int i = 0; i < n; i ++) {
			System.out.println("Running " + i+1 + " simulation...");
			RunJasonProject.main(args);
			Thread t = getThreadByName("MAS-Launcher");
			t.join();
		}
	
	}
}
