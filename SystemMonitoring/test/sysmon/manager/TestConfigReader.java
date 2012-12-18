package sysmon.manager;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

public class TestConfigReader {

	@Test
	public void testGetAlertsConfig() {
		JsonArray arr = ConfigReader.getAlertsConfig();
		if(arr == null) {
			return;
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(arr));
	}
}
