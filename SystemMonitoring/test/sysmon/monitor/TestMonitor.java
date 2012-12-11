package sysmon.monitor;

import sysmon.monitor.crawler.Crawler;
import sysmon.monitor.crawler.DummyCrawler;
import sysmon.util.GlobalParameters;
import sysmon.util.IPUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestMonitor {
	
	public static void test() {
		String managerBrokerAddress = "tcp://" + IPUtil.getFirstAvailableIP() + ":" + GlobalParameters.MANAGER_COMMAND_PORT;
		Monitor m = new Monitor(managerBrokerAddress);
		for(int i = 0; i < 6; ++i) {
			Crawler crawler = new DummyCrawler("dummy" + i);
			m.addCrawler(crawler);
		}
		
		m.start();
	}
	
	public static void main(String[] args) {
		test();
	}
}
