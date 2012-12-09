package sysmon.monitor.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.JsonObject;

/**
 * The dummy crawler that always return the same fetched data.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public class DummyCrawler extends Crawler{

	public DummyCrawler(String crawlerName) {
		super(crawlerName);
	}
	
	@Override
	protected void updateStaticMetaData() {
		this.staticMetaData.addProperty("dummy-static-property", "dummy-static-value");
	}

	@Override
	protected void fetchDynamicMetaDataHelper(JsonObject newDynamicMetaData) {
		//	open a file and then close
		File file = new File("/home/yxjiang/.vimrc");
		file.canRead();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		newDynamicMetaData.addProperty("dummy-property", "dummy-value");
	}

	@Override
	public String getCrawlerType() {
		return "dummy";
	}

}
