package sysmon.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import sysmon.common.PassiveCommandHandler;
import sysmon.util.GlobalParameters;
import sysmon.util.Out;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/**
 * MonitoringManager is in charge of the overall management of the monitoring.
 * It handles the registration of monitors and collectors, 
 * route the data transferring, and conduct fault tolerant tasks. 
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public class MonitoringManager {
	
	private static MonitoringManager instance;
	
	private ManagerPassiveCommandHandler passiveCommandHandler;
	private Map<String, CollectorProfile> collectorsProfile;
	private JsonArray alertJsonConfig;
	
	/**
	 * Get the singleton of monitoring manager.
	 * @return
	 */
	public static MonitoringManager getInstance() {
		if(instance == null) 
			return new MonitoringManager();
		else
			return instance;
	}
	
	private MonitoringManager() {
		this.collectorsProfile = new HashMap<String, CollectorProfile>();
		this.alertJsonConfig = ConfigReader.getAlertsConfig();
		if(this.alertJsonConfig == null) {
			Out.println("Config file config.xml cannot be found!");
			System.exit(1);
		}
		else {
			Out.println("Read config file.");
		}
		this.passiveCommandHandler = new ManagerPassiveCommandHandler(GlobalParameters.MANAGER_COMMAND_PORT);
	}
	
	/**
	 * CollectorProfile contains the basic information about a registered collector.
	 *
	 */
	public class CollectorProfile {
		public String collectorIPAddress;
		public String collectorBrokerAddress;
		public long secondSinceLastConnected;
		public Set<String> monitorSet;
		
		public CollectorProfile(String collectorIPAddress,
				String collectorBrokerAddress) {
			super();
			this.collectorIPAddress = collectorIPAddress;
			this.collectorBrokerAddress = collectorBrokerAddress;
			this.secondSinceLastConnected = 0;
			this.monitorSet = new HashSet<String>();
		}
		
	}
	
	/**
	 * Assign the new registered monitor to collector with least load.
	 * @param monitorName
	 * @return
	 */
	private String assignCollector(String monitorName) {
		String assignedCollector = null;
		CollectorProfile assignedCollectorProfile = null;
		int leastLoad = Integer.MAX_VALUE;
		
		for(Map.Entry<String, CollectorProfile> entry : collectorsProfile.entrySet()) {
			int collectorLoad = entry.getValue().monitorSet.size();
			if(collectorLoad < leastLoad) {
				assignedCollector = entry.getKey();
				assignedCollectorProfile = entry.getValue();
				leastLoad = collectorLoad;
			}
			if(collectorLoad == 0)
				break;
		}
		assignedCollectorProfile.monitorSet.add(monitorName);
		collectorsProfile.put(assignedCollector, assignedCollectorProfile);
		Out.println("Assign [" + monitorName + "] to [" + assignedCollectorProfile.collectorBrokerAddress + "]");
		return assignedCollectorProfile.collectorBrokerAddress;
	}
	
	/**
	 * The handler to receive commands for manager.
	 *
	 */
	class ManagerPassiveCommandHandler extends PassiveCommandHandler {
		
		public ManagerPassiveCommandHandler(String servicePort) {
			super(servicePort);
			Out.println("Start command service at " + this.brokerAddress);
		}

		@Override
		public void onMessage(Message commandMessage) {
			
			if(commandMessage instanceof TextMessage) {
				/*
				 * If success, return {type: "monitor-registration-response", value: "success"}
				 */
				String commandJson;
				try {
					TextMessage responseMessage = this.commandServiceSession.createTextMessage();
					commandJson = ((TextMessage) commandMessage).getText();
					JsonObject jsonObj = (JsonObject)jsonParser.parse(commandJson);
					//	monitor registration event
					String eventType = jsonObj.get("type").getAsString();
					if(eventType.equals("monitor-registration")) {
						String monitorName = jsonObj.get("machineIPAddress").getAsString();
						JsonObject responseJson = new JsonObject();
						responseJson.addProperty("type", "monitor-registration-response");
						responseJson.addProperty("value", "success");
						
						//	find an available collector
						String assignedCollectorBrokerAddress = assignCollector(monitorName);
						responseJson.addProperty("collectorCommandBrokerAddress", assignedCollectorBrokerAddress);
						responseMessage.setJMSCorrelationID(commandMessage.getJMSCorrelationID());
						responseMessage.setText(responseJson.toString());
						this.commandProducer.send(commandMessage.getJMSReplyTo(), responseMessage);
						Out.println("Monitor [" + monitorName + "] registered.");
					}
					else if(eventType.equals("collector-registration")) {
						/*
						 * If success, return {type: "collector-registration-response", value: "success"}
						 */
						String collectorIPAddress = jsonObj.get("collectorIPAddress").getAsString();
						String collectorBrokerAddress = jsonObj.get("collectorBrokerAddress").getAsString();
						CollectorProfile profile = new CollectorProfile(collectorIPAddress, collectorBrokerAddress);
						collectorsProfile.put(collectorIPAddress, profile);
						JsonObject responseJson = new JsonObject();
						responseJson.addProperty("type", "collector-registration-response");
						responseJson.addProperty("value", "success");
						responseJson.add("alertsConfig", alertJsonConfig);
						
						responseMessage.setJMSCorrelationID(commandMessage.getJMSCorrelationID());
						responseMessage.setText(responseJson.toString());
						this.commandProducer.send(commandMessage.getJMSReplyTo(), responseMessage);
						Out.println("Collector [" + collectorIPAddress + ":] registered.");
					}

				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		MonitoringManager manager = MonitoringManager.getInstance();
	}
	
}
