package sysmon.monitor.crawler;

import org.junit.Before;

import com.google.gson.GsonBuilder;

public class TestDiskCrawler extends TestCrawler {
	@Before
	public void setup() {
		gson = new GsonBuilder().setPrettyPrinting().create();
		c = new DiskCrawler("disk");
	}
}
