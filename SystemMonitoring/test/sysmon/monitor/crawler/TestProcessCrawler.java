package sysmon.monitor.crawler;

import org.junit.Before;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class TestProcessCrawler extends TestCrawler{

	@Before
	public void setup() {
		gson = new GsonBuilder().setPrettyPrinting().create();
		c = new ProcessCrawler("process");
	}
	
}
