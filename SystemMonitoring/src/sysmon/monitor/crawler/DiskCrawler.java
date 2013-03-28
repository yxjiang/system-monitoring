package sysmon.monitor.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemMap;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.SigarException;

import sysmon.common.metadata.DiskMetadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Fetch the metadata related to the disk utilization
 * @author yexijiang
 *
 */
public class DiskCrawler extends Crawler<DiskMetadata>{

	public DiskCrawler(String crawlerName) {
		super(crawlerName);
	}

	@Override
	public String getCrawlerType() {
		return "disk";
	}

	@Override
	protected void updateStaticMetaData() {
		//	everything is dynamic
	}

	@Override
	protected void fetchDynamicMetaDataHelper(JsonObject newMetaData) {
		try {
			FileSystem[] fsList = sigarProxy.getFileSystemList();
			FileSystemMap fsMap = new FileSystemMap();
			fsMap.init(fsList);
			Set<Map.Entry<String, FileSystem>> entrySet = fsMap.entrySet();
			List<DiskMetadata.FS> fsMetadataList = new ArrayList<DiskMetadata.FS>();
			for (Map.Entry<String, FileSystem> fsEntry : entrySet) {
				String fsDirName = fsEntry.getKey();
				FileSystem fs = fsEntry.getValue();
				if (!fsMap.isMounted(fsDirName)) 
					continue;
				FileSystemUsage fsUsage = sigarProxy.getFileSystemUsage(fsDirName);
				DiskMetadata.FS fsMetadata = new DiskMetadata.FS(fs.getDevName(), fs.getDirName(), fs.getTypeName(), 
						fs.getSysTypeName(), fsUsage.getTotal(), fsUsage.getUsed(), fsUsage.getUsePercent());
				fsMetadataList.add(fsMetadata);
			}
			this.metadataObject = new DiskMetadata(fsMetadataList);
		} catch (SigarException e) {
			e.printStackTrace();
		}
	}

}
