package sysmon.common.metadata;

import java.io.Serializable;

import sysmon.monitor.crawler.MemoryCrawler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class MachineMetadata implements Serializable {

  private long timestamp;
  private String machineIP;
  private CpuMetadata cpu;
  private MemoryMetadata memory;
  private DiskMetadata disk;

  public MachineMetadata(long timestamp, String machineIP) {
    super();
    this.timestamp = timestamp;
    this.machineIP = machineIP;
    this.cpu = null;
    this.memory = null;
    this.disk = null;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public String getMachineIP() {
    return machineIP;
  }

  public void setMachineIP(String machineIP) {
    this.machineIP = machineIP;
  }

  public CpuMetadata getCpu() {
    return cpu;
  }

  public void setCpu(CpuMetadata cpu) {
    this.cpu = cpu;
  }

  public MemoryMetadata getMemory() {
    return memory;
  }

  public void setMemory(MemoryMetadata memory) {
    this.memory = memory;
  }

  public DiskMetadata getDisk() {
    return disk;
  }

  public void setDisk(DiskMetadata disk) {
    this.disk = disk;
  }

  public JsonObject getJson() {
    JsonObject metadata = new JsonObject();

    metadata.addProperty("timestamp", timestamp);
    metadata.addProperty("machineIP", machineIP);
    metadata.add("cpu", cpu.getJson());
    metadata.add("memory", memory.getJson());

    return metadata;
  }

  @Override
  public String toString() {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    return gson.toJson(this.getJson());
  }

}
