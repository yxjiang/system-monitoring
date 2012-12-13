package sysmon.monitor.crawler;

import org.junit.Test;

public class TestDummyCrawler{

	@Test
	public void testDummyCrawler() {
		Crawler c = new DummyCrawler("dummy-crawler");
		c.updateDynamicMetaData();
		System.out.println(c.getStaticMetaData());
		System.out.println(c.getDynamicMetaData());
	}
}
