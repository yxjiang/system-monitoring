package sysmon.monitor.crawler;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class TestCPUCrawler {
	
	@Test
	public void testGetStaticMetadata() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Crawler c = new CPUCrawler("cpu");
		JsonObject staticJson = c.getStaticMetaData();
		System.out.println(staticJson.toString());
		JsonObject dynamicJson = c.getDynamicMetaData();
		System.out.println(gson.toJson(dynamicJson));
	}
	
}
