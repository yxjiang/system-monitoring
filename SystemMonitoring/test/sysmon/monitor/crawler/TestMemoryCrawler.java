package sysmon.monitor.crawler;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class TestMemoryCrawler extends TestCrawler{
	@Before
	public void setup() {
		gson = new GsonBuilder().setPrettyPrinting().create();
		c = new MemoryCrawler("memory");
	}
	
}
