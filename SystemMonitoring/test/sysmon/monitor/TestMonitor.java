package sysmon.monitor;

import sysmon.monitor.crawler.Crawler;
import sysmon.monitor.crawler.DummyCrawler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestMonitor {
	
	public static void test() {
		
		Monitor m = new Monitor();
		for(int i = 0; i < 6; ++i) {
			Crawler crawler = new DummyCrawler("dummy" + i);
			m.addCrawler(crawler);
		}
		
		m.start();
		
//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		
//		while(true) {
//			
//			String str = gson.toJson(m.getDynamicMetaData());
//			System.out.println(str);
//			
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
	}
	
	public static void main(String[] args) {
		test();
	}
}
