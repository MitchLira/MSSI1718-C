import jason.runtime.*;
import java.lang.ThreadGroup;

public class App{
	
	public static Thread getThreadByName(String threadName) {
	    for (Thread t : Thread.getAllStackTraces().keySet()) {
	        if (t.getName().equals(threadName)) return t;
	    }
	    return null;
	}
	
	public static void main(String[] args) throws InterruptedException {
		RunJasonProject arg = new RunJasonProject();
		arg.main(args);
		Thread t = getThreadByName("MAS-Launcher");
		t.join();
		System.out.println("ALOALOALOALOALOALOALOALOALOALOALOALOALOALOALOALOALOALOALOALOALOALOALOALOALOALOALO");
		arg.main(args);
	}
}
