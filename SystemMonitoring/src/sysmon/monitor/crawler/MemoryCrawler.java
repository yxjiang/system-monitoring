package sysmon.monitor.crawler;

import java.util.Map;

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.SigarException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Crawl the information about system memory.
 * @author yexijiang
 *
 */
public class MemoryCrawler extends Crawler {

	private Mem mem;
	private Gson gson;

	public MemoryCrawler(String crawlerName) {
		super(crawlerName);
	}

	@Override
	public String getCrawlerType() {
		return "memory";
	}

	@Override
	protected void updateStaticMetaData() {
		gson = new Gson();
		try {
			mem = sigar.getMem();
		} catch (SigarException e) {
			e.printStackTrace();
		}
		this.staticMetaData.addProperty("total-memory", mem.getTotal());
	}

	@Override
	protected void fetchDynamicMetaDataHelper(JsonObject newMetaData) {
		Map<Object, Object> map = mem.toMap();
		for(Map.Entry<Object, Object> entry : map.entrySet()) {
			newMetaData.addProperty(entry.getKey().toString(), entry.getValue().toString());
		}
	}

}
