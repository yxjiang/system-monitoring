package sysmon.monitor.crawler;

import org.hyperic.sigar.Sigar;

import com.google.gson.JsonObject;

/**
 * The crawler defines the operations a specific crawler should do.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public abstract class Crawler<T>{
	
	protected String crawlerName;
	protected JsonObject staticMetaData;
	protected JsonObject dynamicMetaData;
	protected Sigar sigar;
	protected T metadataObject;
	
	public Crawler(String crawlerName) {
		this.crawlerName = crawlerName;
		this.staticMetaData = new JsonObject();
		this.dynamicMetaData = new JsonObject();
		this.sigar = new Sigar();
		updateStaticMetaData();
		updateDynamicMetaData();
	}
	
	public String getCrawlerName() {
		return this.crawlerName;
	}
	
	public abstract String getCrawlerType();
	
	/**
	 * Fetch the static meta data and fill it to staticMetaData.
	 */
	protected abstract void updateStaticMetaData();
	
	
	/*
	 * Fetch the dynamic meta data.
	 */
	public void updateDynamicMetaData() {
		JsonObject newDynamicMetaData = new JsonObject();
		newDynamicMetaData.addProperty("type", this.crawlerName);
		fetchDynamicMetaDataHelper(newDynamicMetaData);
		synchronized (dynamicMetaData) {
			this.dynamicMetaData = newDynamicMetaData;
		}
	}
	
	/**
	 * Update the dynamic meta data, also keep the object version copy.
	 */
	protected abstract void fetchDynamicMetaDataHelper(JsonObject newMetaData);
	
	public JsonObject getStaticMetaData() {
		return staticMetaData;
	}
	
	public JsonObject getDynamicMetaData() {
		synchronized(dynamicMetaData	) {
			return this.dynamicMetaData;
		}
	}
	
	/**
	 * Return the meta data in POJO format.
	 * @return
	 */
	public T getMetadataObject() {
		return metadataObject;
	}
}
