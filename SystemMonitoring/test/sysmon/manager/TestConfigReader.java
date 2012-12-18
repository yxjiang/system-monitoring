package sysmon.manager;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class TestConfigReader {
	
	private Gson gson;
	
	@Before
	public void init() {
		gson = new GsonBuilder().setPrettyPrinting().create();
	}

	@Test
	public void testGetAlertsConfig() {
		JsonArray arr = ConfigReader.getAlertsConfig();
		if(arr == null) {
			return;
		}
//		System.out.println(gson.toJson(arr));
		assertEquals(2, arr.size());
		JsonObject first = arr.get(0).getAsJsonObject();
		assertTrue(first.has("type"));
		assertEquals("CpuUsageAlert", first.get("type").getAsString());
		
		assertTrue(first.has("parameters"));
		JsonArray firstParameters = first.get("parameters").getAsJsonArray();
		assertEquals("timeWindow", firstParameters.get(0).getAsJsonObject().get("name").getAsString());
		assertEquals(10, firstParameters.get(0).getAsJsonObject().get("value").getAsInt());
		assertEquals("idleTimeAlertThreshold", firstParameters.get(1).getAsJsonObject().get("name").getAsString());
		assertEquals(0.2, firstParameters.get(1).getAsJsonObject().get("value").getAsFloat(), 0.01);
		
		JsonObject second = arr.get(1).getAsJsonObject();
		assertEquals("MemoryUsageAlert", second.get("type").getAsString());
		JsonArray secondParameters = second.get("parameters").getAsJsonArray();
		assertEquals("timeWindow", secondParameters.get(0).getAsJsonObject().get("name").getAsString());
		assertEquals(10, secondParameters.get(0).getAsJsonObject().get("value").getAsInt());
		assertEquals("idleMemAlertThreshold", secondParameters.get(1).getAsJsonObject().get("name").getAsString());
		assertEquals(0.05, secondParameters.get(1).getAsJsonObject().get("value").getAsFloat(), 0.01);
	}
	
	@Test
	public void testGetCollectorAssignConfig() {
		JsonObject obj = ConfigReader.getCollectorAssignConfig();
		if(obj == null) {
			return;
		}
//		System.out.println(gson.toJson(obj));
		assertTrue(obj.has("strategy"));
		assertEquals("load-balance", obj.get("strategy").getAsString());
	}
	
}
