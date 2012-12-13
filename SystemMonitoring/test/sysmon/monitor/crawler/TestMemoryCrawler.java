package sysmon.monitor.crawler;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class TestMemoryCrawler {

	private Gson gson;
	private Crawler c;
	
	@Before
	public void init() {
		gson = new GsonBuilder().setPrettyPrinting().create();
		c = new MemoryCrawler("mem");
	}
	
	@Test
	public void testGetStaticMetadata() {
		JsonObject staticJson = c.getStaticMetaData();
		System.out.println(staticJson.toString());
	}
	
	@Test
	public void testGetDynamicMetadata() {
		JsonObject dynamicJson = c.getDynamicMetaData();
		System.out.println(gson.toJson(dynamicJson));
	}
}
