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
			System.out.println("config.xml not exists.");
			return;
		}
		System.out.println(gson.toJson(arr));
		assertEquals(2, arr.size());
		
		/*	Test the first alert	*/
		JsonObject first = arr.get(0).getAsJsonObject();
		assertTrue(first.has("type"));
		assertEquals("CpuUsageAlert", first.get("type").getAsString());
		
		assertTrue(first.has("parameters"));
		JsonObject firstParameters = first.get("parameters").getAsJsonObject();
		
		assertTrue("timeWindow", firstParameters.has("timeWindow"));
		JsonObject timeWindowObj = firstParameters.get("timeWindow").getAsJsonObject();
		assertEquals("timeWindow", timeWindowObj.get("name").getAsString());
		assertEquals(10, timeWindowObj.get("value").getAsInt());
		
		assertTrue("idleTimeAlertThreshold", firstParameters.has("idleTimeAlertThreshold"));
		JsonObject idleTimeAlertThresholdObj = firstParameters.get("idleTimeAlertThreshold").getAsJsonObject();
		assertEquals("idleTimeAlertThreshold", idleTimeAlertThresholdObj.get("name").getAsString());
		assertEquals(0.2, idleTimeAlertThresholdObj.get("value").getAsFloat(), 0.01);

		/*	Test the second alert	*/
		JsonObject second = arr.get(1).getAsJsonObject();
		assertEquals("MemoryUsageAlert", second.get("type").getAsString());
		
		JsonObject secondParameters = second.get("parameters").getAsJsonObject();
		assertTrue("timeWindow", secondParameters.has("timeWindow"));
		
		JsonObject secondTimeWindowObj = secondParameters.get("timeWindow").getAsJsonObject();
		assertEquals("timeWindow", secondTimeWindowObj.get("name").getAsString());
		assertEquals(10, secondTimeWindowObj.get("value").getAsInt());
		
		JsonObject idleMemAlertThresholdObj = secondParameters.get("idleMemAlertThreshold").getAsJsonObject();
		assertEquals("idleMemAlertThreshold", idleMemAlertThresholdObj.get("name").getAsString());
		assertEquals(0.05, idleMemAlertThresholdObj.get("value").getAsFloat(), 0.01);
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
