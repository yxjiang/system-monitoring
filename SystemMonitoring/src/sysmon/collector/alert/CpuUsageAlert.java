package sysmon.collector.alert;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/**
 * Generate the alert when CPU usage is higher than a threshold for a certain time.
 * @author yexijiang
 *
 */
public class CpuUsageAlert {

	private static double idleTimeAlertThreshold = 0.1;
	private static int timeWindow = 10;
	
	private final EPServiceProvider epService;
	private CpuUsageListener listener;
	
	public CpuUsageAlert(EPServiceProvider epService) {
		this(epService, timeWindow, idleTimeAlertThreshold);
	}
	
	public CpuUsageAlert(EPServiceProvider epService, int timeWindow, double idleTimeAlertThreshold) {
		this.epService = epService;
		String queryExpress = "select machineIP, avg(cpu.idleTime) as avg from MachineMetadata.win:length(" 
				+ timeWindow + ") group by machineIP";
		EPStatement epStatement = this.epService.getEPAdministrator().createEPL(queryExpress);
		this.listener = new CpuUsageListener(idleTimeAlertThreshold);
		epStatement.addListener(this.listener);
	}
	
	/**
	 * The event listener used by CpuUsageAlert.
	 * @author yexijiang
	 *
	 */
	static class CpuUsageListener implements UpdateListener{
		
		private double idleTimeAlertThreshold;
		
		public CpuUsageListener(double idleTimeAlertThreshold) {
			this.idleTimeAlertThreshold = idleTimeAlertThreshold;
		}

		@Override
		public void update(EventBean[] newEvents, EventBean[] oldEvents) {
			EventBean event = newEvents[0];
			if(Double.parseDouble(event.get("avg").toString()) < idleTimeAlertThreshold)
				System.out.println("Machine [" + event.get("machineIP") + "], CPU is busy. Idle time:" + event.get("avg") + "%");
		}
		
	}
}
