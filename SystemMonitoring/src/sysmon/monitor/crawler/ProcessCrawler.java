package sysmon.monitor.crawler;

import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.SigarException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Crawl the information about running processes.
 * @author yexijiang
 *
 */
public class ProcessCrawler extends Crawler{

	public ProcessCrawler(String crawlerName) {
		super(crawlerName);
	}

	@Override
	public String getCrawlerType() {
		return "process";
	}

	@Override
	protected void updateStaticMetaData() {
		//	there is no statistic metadata for process, everything is changing
	}
	


	@Override
	protected void fetchDynamicMetaDataHelper(JsonObject newMetaData) {
		JsonArray procsJson = new JsonArray();
		try {
			long[] pids = sigarProxy.getProcList();
			for(int i = 0; i < pids.length; ++i) {
				procsJson.add(getProcInfo(pids[i]));
			}
		} catch (SigarException e) {
			e.printStackTrace();
		}	
		newMetaData.add("process-details", procsJson);
	}
	
	/**
	 * Get the detailed process info for given pid.
	 * @param pid
	 * @throws SigarException 
	 */
	private JsonObject getProcInfo(long pid) throws SigarException {
		JsonObject procDetailJson = new JsonObject();
		ProcState procState = sigarProxy.getProcState(pid);
		procDetailJson.addProperty("pid", pid);
		procDetailJson.addProperty("name", procState.getName());
		procDetailJson.addProperty("state", procState.getState());
		procDetailJson.addProperty("numThreads", procState.getThreads());
		return procDetailJson;
	}
	
}
