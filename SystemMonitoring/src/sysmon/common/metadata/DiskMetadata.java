package sysmon.common.metadata;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DiskMetadata {

  private String type;
  private FS[] fileSystems;

  public DiskMetadata(List<FS> fileSystems) {
    this.type = "disk";
    this.fileSystems = new FS[fileSystems.size()];
    for (int i = 0; i < fileSystems.size(); ++i)
      this.fileSystems[i] = fileSystems.get(i);
  }

  public FS[] getFileSystems() {
    return fileSystems;
  }

  public void setFileSystems(FS[] fileSystems) {
    this.fileSystems = fileSystems;
  }

  public JsonObject getJson() {
    JsonObject metadata = new JsonObject();
    JsonArray fileSystemArr = new JsonArray();
    for (FS fs : fileSystems)
      fileSystemArr.add(fs.getJson());
    metadata.add("file-systems", fileSystemArr);
    return metadata;
  }

  @Override
  public String toString() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(this.getJson());
  }

  /**
   * The file system metadata.
   * 
   * @author yexijiang
   */
  public static class FS {
    private String devName;
    private String dirName;
    private String typeName;
    private String sysTypeName;
    private long totalMB;
    private long usedMB;
    private double usedPercentage;

    public FS(String devName, String dirName, String typeName,
        String sysTypeName, long totalMB, long usedMB, double usedPercentage) {
      super();
      this.devName = devName;
      this.dirName = dirName;
      this.typeName = typeName;
      this.sysTypeName = sysTypeName;
      this.totalMB = totalMB;
      this.usedMB = usedMB;
      this.usedPercentage = usedPercentage;
    }

    public String getDevName() {
      return devName;
    }

    public void setDevName(String devName) {
      this.devName = devName;
    }

    public String getDirName() {
      return dirName;
    }

    public void setDirName(String dirName) {
      this.dirName = dirName;
    }

    public String getTypeName() {
      return typeName;
    }

    public void setTypeName(String typeName) {
      this.typeName = typeName;
    }

    public String getSysTypeName() {
      return sysTypeName;
    }

    public void setSysTypeName(String sysTypeName) {
      this.sysTypeName = sysTypeName;
    }

    public long getTotalMB() {
      return totalMB;
    }

    public void setTotalMB(long totalMB) {
      this.totalMB = totalMB;
    }

    public long getUsedMB() {
      return usedMB;
    }

    public void setUsedMB(long usedMB) {
      this.usedMB = usedMB;
    }

    public double getUsedPercentage() {
      return usedPercentage;
    }

    public void setUsedPercentage(double usedPercentage) {
      this.usedPercentage = usedPercentage;
    }

    public JsonObject getJson() {
      JsonObject fsObj = new JsonObject();
      fsObj.addProperty("devName", devName);
      fsObj.addProperty("dirName", dirName);
      fsObj.addProperty("typeName", typeName);
      fsObj.addProperty("sysTypeName", sysTypeName);
      fsObj.addProperty("totalMB", totalMB / 1024);
      fsObj.addProperty("usedMB", usedMB / 1024);
      fsObj.addProperty("usedPercentage", usedPercentage);
      return fsObj;
    }

  }

}
