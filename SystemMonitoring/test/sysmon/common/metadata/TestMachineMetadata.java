package sysmon.common.metadata;

import java.util.Date;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import sysmon.util.IPUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestMachineMetadata {

	private Random rnd;
	private Gson gson;
	
	@Before
	public void init() {
		rnd = new Random();
		gson = new GsonBuilder().setPrettyPrinting().create();
	}
	
	@Test
	public void testGetJson() {
		CpuMetadata.Core[] cores1 = new CpuMetadata.Core[4];
		for(int j = 0; j < 4; ++j) {
			cores1[j] = new CpuMetadata.Core(rnd.nextFloat() % 1, rnd.nextFloat() % 1, rnd.nextFloat() % 1, 0.2f);
		}
		CpuMetadata event1 = new CpuMetadata(cores1);
		MachineMetadata machineMetadata1 = new MachineMetadata(new Date().getTime() / 1000, IPUtil.getFirstAvailableIP());
		machineMetadata1.setCpu(event1);
		System.out.println(gson.toJson(machineMetadata1));
	}
}
