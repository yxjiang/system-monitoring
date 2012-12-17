package sysmon.esper;

import java.util.Date;
import java.util.Random;

import sysmon.common.metadata.CpuMetadata;
import sysmon.common.metadata.MachineMetadata;
import sysmon.util.IPUtil;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class Driver {
	
	public static void run() {
		Configuration config = new Configuration();
		config.addEventTypeAutoName("sysmon.common.metadata");
		EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider(config);
		String experssion = "select avg(cpu.cores[0].idleTime) as a from MachineMetadata.win:length(5)";
		EPStatement statement = epService.getEPAdministrator().createEPL(experssion);
		MyListener listener = new MyListener();
		statement.addListener(listener);
		Random rnd = new Random();
		for(int i = 0; i < 10; ++i) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			CpuMetadata.Core[] cores1 = new CpuMetadata.Core[4];
			for(int j = 0; j < 4; ++j) {
				cores1[j] = new CpuMetadata.Core(rnd.nextFloat() % 1, rnd.nextFloat() % 1, rnd.nextFloat() % 1, 0.2f);
			}
			CpuMetadata event1 = new CpuMetadata("cpu", cores1);
			MachineMetadata machineMetadata1 = new MachineMetadata(new Date().getTime() / 1000, IPUtil.getFirstAvailableIP());
			machineMetadata1.setCpu(event1);
			
			epService.getEPRuntime().sendEvent(machineMetadata1);
		}

	}
	
	public static void main(String[] args) {
		run();
	}
}
