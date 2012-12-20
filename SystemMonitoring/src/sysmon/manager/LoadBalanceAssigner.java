package sysmon.manager;

import java.util.Map;

import sysmon.manager.MonitoringManager.CollectorProfile;
import sysmon.util.Out;

public class LoadBalanceAssigner extends MonitorAssigner{

	public LoadBalanceAssigner(Map<String, CollectorProfile> collectorProfiles) {
		super(collectorProfiles);
	}

	@Override
	public String assignMonitorToCollector(String monitorName) {
		String assignedCollector = null;
		CollectorProfile assignedCollectorProfile = null;
		int leastLoad = Integer.MAX_VALUE;
		
		for(Map.Entry<String, CollectorProfile> entry : collectorsProfiles.entrySet()) {
			int collectorLoad = entry.getValue().monitorSet.size();
			if(collectorLoad < leastLoad) {
				assignedCollector = entry.getKey();
				assignedCollectorProfile = entry.getValue();
				leastLoad = collectorLoad;
			}
			if(collectorLoad == 0)
				break;
		}
		assignedCollectorProfile.monitorSet.add(monitorName);
		collectorsProfiles.put(assignedCollector, assignedCollectorProfile);
		Out.println("Assign [" + monitorName + "] to [" + assignedCollectorProfile.collectorBrokerAddress + "]");
		return assignedCollectorProfile.collectorBrokerAddress;
	}

}
