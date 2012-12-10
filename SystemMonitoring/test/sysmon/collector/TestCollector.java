package sysmon.collector;

import sysmon.util.GlobalParameters;

public class TestCollector {
	public static void test() {
		String managerBrokerAddress = "tcp://192.168.0.100:" + GlobalParameters.MANAGER_COMMAND_PORT;
		Collector c = new Collector(managerBrokerAddress);
		c.start();
	}
	
	public static void main(String[] args) {
		test();
	}
}
