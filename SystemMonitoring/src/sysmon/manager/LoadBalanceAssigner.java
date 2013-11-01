package sysmon.manager;

import java.util.Map;

import sysmon.manager.MonitoringManager.CollectorProfile;
import sysmon.util.Out;

/**
 * Assign the monitor to the least loaded collector.
 * 
 * @author yexijiang
 * 
 */
public class LoadBalanceAssigner extends MonitorAssigner {

  public LoadBalanceAssigner(Map<String, CollectorProfile> collectorsProfiles) {
    super(collectorsProfiles);
  }

  @Override
  public String assignMonitorToCollector(String monitorName) {
    String assignedCollector = null;
    CollectorProfile assignedCollectorProfile = null;
    int leastLoad = Integer.MAX_VALUE;

    synchronized (this.collectorsProfiles) {
      for (Map.Entry<String, CollectorProfile> entry : collectorsProfiles
          .entrySet()) {
        int collectorLoad = entry.getValue().monitorSet.size();
        if (collectorLoad < leastLoad) {
          assignedCollector = entry.getKey();
          assignedCollectorProfile = entry.getValue();
          leastLoad = collectorLoad;
        }
        if (collectorLoad == 0)
          break;
      }
      assignedCollectorProfile.monitorSet.add(monitorName);
      collectorsProfiles.put(assignedCollector, assignedCollectorProfile);
    }

    out.println("Assign [" + monitorName + "] to ["
        + assignedCollectorProfile.collectorBrokerAddress + "]");
    return assignedCollectorProfile.collectorBrokerAddress;
  }

}
