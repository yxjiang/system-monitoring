package sysmon.monitor.crawler;

import java.util.Map;

import org.hyperic.sigar.Mem;
import org.hyperic.sigar.SigarException;

import sysmon.common.metadata.MemoryMetadata;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Crawl the information about system memory.
 * @author yexijiang
 *
 */
public class MemoryCrawler extends Crawler<MemoryMetadata> {

	private Mem mem;

	public MemoryCrawler(String crawlerName) {
		super(crawlerName);
	}

	@Override
	public String getCrawlerType() {
		return "memory";
	}

	@Override
	protected void updateStaticMetaData() {
		try {
			mem = sigarProxy.getMem();
		} catch (SigarException e) {
			e.printStackTrace();
		}
		this.staticMetaData.addProperty("totalMemory", mem.getTotal());
	}

	@Override
	protected void fetchDynamicMetaDataHelper(JsonObject newMetaData) {
		try {
			mem = sigarProxy.getMem();
		} catch (SigarException e) {
			e.printStackTrace();
		}
		
		MemoryMetadata memoryMetadata = new MemoryMetadata();
		memoryMetadata.setUsed(mem.getUsed());
		memoryMetadata.setActualUsed(mem.getActualUsed());
		memoryMetadata.setActualFree(mem.getActualFree());
		memoryMetadata.setFreePercent(mem.getFreePercent());
		memoryMetadata.setActualFree(mem.getActualFree());
		memoryMetadata.setRam(mem.getRam());
		memoryMetadata.setUsedPercent(mem.getUsedPercent());
		memoryMetadata.setFree(mem.getFree());
		memoryMetadata.setTotal(mem.getTotal());
		
		this.metadataObject = memoryMetadata;
		
		Map<Object, Object> map = mem.toMap();
		for(Map.Entry<Object, Object> entry : map.entrySet()) {
			newMetaData.addProperty(entry.getKey().toString(), entry.getValue().toString());
		}
		
	}

}
