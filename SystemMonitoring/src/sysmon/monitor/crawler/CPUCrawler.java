package sysmon.monitor.crawler;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.SigarException;

import com.google.gson.JsonObject;


/**
 * Crawl the information about system CPU.
 * @author yexijiang
 *
 */
public class CPUCrawler extends Crawler{
	
	public CPUCrawler(String crawlerName) {
		super(crawlerName);
	}

	@Override
	public String getCrawlerType() {
		return "cpu";
	}

	@Override
	protected void updateStaticMetaData() {
		try {
			CpuInfo[] cpuInfos = sigar.getCpuInfoList();
			CpuInfo firstCPU = cpuInfos[0];
			this.staticMetaData.addProperty("total-cores", firstCPU.getTotalCores());
			this.staticMetaData.addProperty("vendor", firstCPU.getVendor());
			this.staticMetaData.addProperty("model", firstCPU.getModel());
			this.staticMetaData.addProperty("Mhz", firstCPU.getMhz());
		} catch (SigarException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void fetchDynamicMetaDataHelper(JsonObject newMetaData) {
		try {
			CpuPerc[] cpuPercs = sigar.getCpuPercList();
			for(int i = 0; i < cpuPercs.length; ++i) {
				JsonObject singleCpu = new JsonObject();
				singleCpu.addProperty("user-time", cpuPercs[i].getUser());
				singleCpu.addProperty("sys-time", cpuPercs[i].getSys());
				singleCpu.addProperty("combined-time", cpuPercs[i].getCombined());
				singleCpu.addProperty("idle-time", cpuPercs[i].getIdle());
				newMetaData.add("core-" + i, singleCpu);
			}
		} catch (SigarException e) {
			e.printStackTrace();
		}
	}
	
}
