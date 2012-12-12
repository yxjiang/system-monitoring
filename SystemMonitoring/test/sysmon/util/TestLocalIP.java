package sysmon.util;

import java.net.SocketException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class TestLocalIP {
	
	@Test
	public void testRetrieveIPAddresses() {
		try {
			List<String> list = IPUtil.retrieveIPAddresses();
			for(String str : list) {
				System.out.println("IP:" + str);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	@Ignore
	@Test
	public void testLocalIP() {
		System.out.println(IPUtil.getFirstAvailableIP());
	}
}
