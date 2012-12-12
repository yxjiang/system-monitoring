package sysmon.monitor.crawler;

import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.google.gson.JsonObject;

public class CPUCrawler extends Crawler{
	
	private final static String cpuStaticInfoFile = "/proc/cpuinfo";
	private Sigar sigar;

	public CPUCrawler(String crawlerName) {
		super(crawlerName);
		this.sigar = new Sigar();
	}

	@Override
	public String getCrawlerType() {
		return "cpu";
	}

	@Override
	protected void updateStaticMetaData() {
		JsonObject staticMetaDataJson = new JsonObject();
//		try {
//			sigar.getCpuInfoList();
//			
//		} catch (SigarException e) {
//			e.printStackTrace();
//		}
		
		
		this.staticMetaData = staticMetaDataJson;
	}

	@Override
	protected void fetchDynamicMetaDataHelper(JsonObject newMetaData) {
		
	}
	
	public void test() {
		updateStaticMetaData();
		System.out.println(this.staticMetaData.toString());
	}
	
	public static void main(String[] args) {
		CPUCrawler c = new CPUCrawler("cpu");
		c.test();
	}

}
