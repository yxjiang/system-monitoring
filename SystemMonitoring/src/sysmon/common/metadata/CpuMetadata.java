package sysmon.common.metadata;

import java.io.Serializable;

public class CpuMetadata implements Serializable{
	private String type;
	private double userTime;
	private double sysTime;
	private double combinedTime;
	private double idleTime;
	private Core[] cores;

	public CpuMetadata(String type, Core[] cores) {
		super();
		this.type = type;
		this.cores = cores;
		for(Core core : cores) {
			userTime += core.getUserTime();
			sysTime += core.getSysTime();
			combinedTime += core.getCombinedTime();
			idleTime += core.getIdleTime();
		}
		userTime /= cores.length;
		sysTime /= cores.length;
		combinedTime /= cores.length;
		idleTime /= cores.length;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getUserTime() {
		return userTime;
	}

	public double getSysTime() {
		return sysTime;
	}

	public double getCombinedTime() {
		return combinedTime;
	}

	public double getIdleTime() {
		return idleTime;
	}

	public void setCore(int index, Core core) {
		this.cores[index] = core;
	}

	public String getType() {
		return this.type;
	}

	public Core[] getCores() {
		return this.cores;
	}

	public static class Core implements Serializable{
		private double userTime;
		private double sysTime;
		private double combinedTime;
		private double idleTime;

		public Core(double userTime, double sysTime, double combinedTime,
				double idleTime) {
			super();
			this.userTime = userTime;
			this.sysTime = sysTime;
			this.combinedTime = combinedTime;
			this.idleTime = idleTime;
		}

		public double getUserTime() {
			return userTime;
		}

		public void setUserTime(double userTime) {
			this.userTime = userTime;
		}

		public double getSysTime() {
			return sysTime;
		}

		public void setSysTime(double sysTime) {
			this.sysTime = sysTime;
		}

		public double getCombinedTime() {
			return combinedTime;
		}

		public void setCombinedTime(double combinedTime) {
			this.combinedTime = combinedTime;
		}

		public double getIdleTime() {
			return idleTime;
		}

		public void setIdleTime(double idleTime) {
			this.idleTime = idleTime;
		}

	}
}
