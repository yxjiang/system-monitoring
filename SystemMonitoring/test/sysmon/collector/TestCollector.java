package sysmon.collector;

import sysmon.util.GlobalParameters;
import sysmon.util.IPUtil;

public class TestCollector {
	public static void test() {
		String managerBrokerAddress = "tcp://" + IPUtil.getFirstAvailableIP() + ":" + GlobalParameters.MANAGER_COMMAND_PORT;
		int capacity = 60;
		Collector c = new Collector(managerBrokerAddress, capacity);
		c.start();
	}
	
	public static void main(String[] args) {
		test();
	}
}
