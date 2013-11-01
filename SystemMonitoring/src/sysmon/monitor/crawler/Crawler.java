package sysmon.monitor.crawler;

import org.hyperic.sigar.Humidor;
import org.hyperic.sigar.SigarProxy;

import com.google.gson.JsonObject;

/**
 * The crawler defines the operations a specific crawler should do.
 * 
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 * 
 */
public abstract class Crawler<T> {

  protected String crawlerName;
  protected JsonObject staticMetaData;
  protected SigarProxy sigarProxy;
  protected T metadataObject;

  public Crawler(String crawlerName) {
    this.crawlerName = crawlerName;
    this.staticMetaData = new JsonObject();
    this.sigarProxy = Humidor.getInstance().getSigar();
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
   * Fetch the dynamic meta data and fill into metadata object.
   */
  public void updateDynamicMetaData() {
    JsonObject newDynamicMetaData = new JsonObject();
    newDynamicMetaData.addProperty("type", this.crawlerName);
    fetchDynamicMetaDataHelper(newDynamicMetaData);
  }

  /**
   * Update the dynamic meta data, also keep the object version copy.
   */
  protected abstract void fetchDynamicMetaDataHelper(JsonObject newMetaData);

  public JsonObject getStaticMetaData() {
    return staticMetaData;
  }

  /**
   * Return the meta data in POJO format.
   * 
   * @return
   */
  public T getMetadataObject() {
    return metadataObject;
  }
}
