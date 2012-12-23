package sysmon.manager;

import java.util.Map;

import sysmon.manager.MonitoringManager.CollectorProfile;

/**
 * Assign the monitor to proper collector.
 * @author yexijiang
 *
 */
public abstract class MonitorAssigner {
	
	protected Map<String, CollectorProfile> collectorsProfiles;
	
	public MonitorAssigner(Map<String, CollectorProfile> collectorsProfiles) {
		this.collectorsProfiles = collectorsProfiles;
	}
	
	/**
	 * Assign the given monitor to proper collector.
	 * @param monitorName
	 * @return
	 */
	public abstract String assignMonitorToCollector(String monitorName);
}
