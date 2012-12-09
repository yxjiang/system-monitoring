package sysmon.util;

import org.junit.Test;

public class TestLocalIP {
	
	@Test
	public void testLocalIP() {
		System.out.println(IPUtil.getFirstAvailableIP());
	}
}
