package sysmon.common;

import java.util.Date;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import sysmon.monitor.crawler.CPUCrawler;
import sysmon.monitor.crawler.Crawler;
import sysmon.monitor.crawler.MemoryCrawler;

import com.google.gson.JsonObject;

public class TestThreadSafeMetadataBuffer {
	
	private ThreadSafeMetadataBuffer buffer;
	
	@Before
	public void init() {
		buffer = new ThreadSafeMetadataBuffer(600);
	}

	@Ignore
	@Test
	public void testInsert() {
		JsonObject testJson = new JsonObject();
		Crawler cpuCrawler = new CPUCrawler("cpu");
		Crawler memoryCrawler = new MemoryCrawler("memory");
		JsonObject cpuJson = cpuCrawler.getDynamicMetaData();
		JsonObject memoryJson = memoryCrawler.getDynamicMetaData();
		testJson.addProperty("timestamp", new Date().getTime() / 1000);
		testJson.add("cpu", cpuJson);
		testJson.add("memory", memoryJson);
		buffer.insert(testJson.toString());
		String queryStmt = "select * from MachineData";
		buffer.query(queryStmt);
	}
	
	@Test
	public void testBatchInsert() {
		Crawler cpuCrawler = new CPUCrawler("cpu");
		Crawler memoryCrawler = new MemoryCrawler("memory");
		
		while(true) {
			
			JsonObject testJson = new JsonObject();
			testJson.addProperty("timestamp", new Date().getTime() / 1000);
			JsonObject cpuJson = cpuCrawler.getDynamicMetaData();
			JsonObject memoryJson = memoryCrawler.getDynamicMetaData();
			testJson.add("cpu", cpuJson);
			testJson.add("memory", memoryJson);
			buffer.insert(testJson.toString());
			String queryStmt = "select count(*) from MachineData";
			buffer.query(queryStmt);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
