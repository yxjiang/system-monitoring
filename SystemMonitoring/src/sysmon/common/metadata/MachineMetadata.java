package sysmon.common.metadata;

import java.io.Serializable;

public class MachineMetadata implements Serializable{
	
	private long timestamp;
	private String machineIP;
	private CpuMetadata cpu;
	
	public MachineMetadata(long timestamp, String machineIP) {
		super();
		this.timestamp = timestamp;
		this.machineIP = machineIP;
		this.cpu = null;
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
	
	
}
