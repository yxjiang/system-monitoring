package sysmon.monitor.crawler;

import org.junit.Before;

import com.google.gson.GsonBuilder;

public class TestProcessCrawler extends TestCrawler{

	@Before
	public void init() {
		gson = new GsonBuilder().setPrettyPrinting().create();
		c = new ProcessCrawler("process");
	}
	
}
