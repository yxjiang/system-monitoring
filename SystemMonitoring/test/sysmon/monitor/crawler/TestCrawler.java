package sysmon.monitor.crawler;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public abstract class TestCrawler {
	
	protected Gson gson;
	protected Crawler c;
	
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
