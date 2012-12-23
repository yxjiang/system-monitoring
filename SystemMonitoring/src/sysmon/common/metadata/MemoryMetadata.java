package sysmon.common.metadata;

import java.io.Serializable;

import com.google.gson.JsonObject;

public class MemoryMetadata implements Serializable{
	private String type;
	private long used;
	private long actualUsed;
	private double freePercent;
	private long actualFree;
	private long ram;
	private double usedPercent;
	private long free;
	private long total;
	
	public MemoryMetadata() {
		type = "memory";
	}

	public long getUsed() {
		return used;
	}

	public void setUsed(long used) {
		this.used = used;
	}

	public long getActualUsed() {
		return actualUsed;
	}

	public void setActualUsed(long actualUsed) {
		this.actualUsed = actualUsed;
	}

	public double getFreePercent() {
		return freePercent;
	}

	public void setFreePercent(double freePercent) {
		this.freePercent = freePercent;
	}

	public long getActualFree() {
		return actualFree;
	}

	public void setActualFree(long actualFree) {
		this.actualFree = actualFree;
	}

	public long getRam() {
		return ram;
	}

	public void setRam(long ram) {
		this.ram = ram;
	}

	public double getUsedPercent() {
		return usedPercent;
	}

	public void setUsedPercent(double usedPercent) {
		this.usedPercent = usedPercent;
	}

	public long getFree() {
		return free;
	}

	public void setFree(long free) {
		this.free = free;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}
	
	public JsonObject getJson() {
		JsonObject jsonObj = new JsonObject();
		
		jsonObj.addProperty("type", type);
		jsonObj.addProperty("used", used);
		jsonObj.addProperty("actualUsed", actualUsed);
		jsonObj.addProperty("freePercent", freePercent);
		jsonObj.addProperty("actualFree", actualFree);
		jsonObj.addProperty("ram", ram);
		jsonObj.addProperty("usedPercent", usedPercent);
		jsonObj.addProperty("free", free);
		jsonObj.addProperty("total", total);
		
		return jsonObj;
	}
	
}
