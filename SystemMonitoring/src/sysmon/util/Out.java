package sysmon.util;

import java.util.Date;

public class Out {
	public static void println(String message) {
		System.out.println("[" + new Date() + "] " + message);
	}
}
