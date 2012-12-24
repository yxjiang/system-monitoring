package sysmon.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Out {
	
	static {
		PropertyConfigurator.configure("./log4j.properties");
		globalLogger = LogManager.getLogger("Global");
	}
	
	private static Logger globalLogger;
	
	public Out() {

	}
	
	public void println(String message) {
//		System.out.println("[" + new Date() + "] " + message);
		globalLogger.trace(message);
	}
	
	public void error(String message) {
//		System.err.println("[" + new Date() + "]" + message);
		globalLogger.error(message);
	}
}
