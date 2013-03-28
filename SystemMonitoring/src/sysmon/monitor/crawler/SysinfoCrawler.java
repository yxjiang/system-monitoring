package sysmon.monitor.crawler;

import org.hyperic.sigar.SysInfo;
import com.google.gson.JsonObject;

/**
 * Fetch the information of system architecture and machine name.
 * @author yexijiang
 *
 */
public class SysinfoCrawler extends Crawler {

	public SysinfoCrawler(String crawlerName) {
		super(crawlerName);
	}

	@Override
	public String getCrawlerType() {
		return "sysinfo";
	}

	@Override
	protected void updateStaticMetaData() {
		SysInfo sysinfo = new SysInfo();
		String architecture = sysinfo.getArch();
		String machineName = sysinfo.getName();
		String description = sysinfo.getDescription();
		this.staticMetaData.addProperty("architecture", architecture);
		this.staticMetaData.addProperty("machineName", machineName);
		this.staticMetaData.addProperty("description", description);
	}

	@Override
	protected void fetchDynamicMetaDataHelper(JsonObject newMetaData) {
		//	there is no static metadata
	}

}
