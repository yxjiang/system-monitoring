package sysmon.manager;

import java.util.Map;

import sysmon.manager.MonitoringManager.CollectorProfile;

/**
 * Assign a given monitor to proper collector.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public abstract class MonitorAssigner {
	
	protected Map<String, CollectorProfile> collectorsProfiles;
	
	public MonitorAssigner(Map<String, CollectorProfile> collectorsProfiles) {
		this.collectorsProfiles = collectorsProfiles;
	}
	
	/**
	 * Assign a given monitor to collector.
	 * @param monitor
	 * @return
	 */
	public abstract String assignMonitorToCollector(String monitorName);
}
