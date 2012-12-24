package sysmon.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import sysmon.common.PassiveCommandHandler;
import sysmon.util.GlobalParameters;
import sysmon.util.Out;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * MonitoringManager is in charge of the overall management of the monitoring.
 * It handles the registration of monitors and collectors, route the data
 * transferring, and conduct fault tolerant tasks.
 * 
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 * 
 */
public class MonitoringManager {

	private static MonitoringManager instance;
	private Out out;

	private ManagerPassiveCommandHandler passiveCommandHandler;
	private Map<String, CollectorProfile> collectorsProfiles;
	private JsonArray alertJsonConfig;
	private MonitorAssigner monitorAssigner;

	/**
	 * Get the singleton of monitoring manager.
	 * 
	 * @return
	 */
	public static MonitoringManager getInstance() {
		if (instance == null)
			return new MonitoringManager();
		else
			return instance;
	}

	private MonitoringManager() {
		this.out = new Out();
		this.collectorsProfiles = new HashMap<String, CollectorProfile>();
		this.alertJsonConfig = ConfigReader.getAlertsConfig();
		JsonObject assignStrategy = ConfigReader.getCollectorAssignConfig();
		String strategy = assignStrategy.get("strategy").getAsString();

		if (strategy.equals("load-balance")) {
			this.monitorAssigner = new LoadBalanceAssigner(this.collectorsProfiles);
		}

		if (this.alertJsonConfig == null) {
			out.println("Config file config.xml cannot be found!");
			System.exit(1);
		} else {
			out.println("Read config file.");
		}
		this.passiveCommandHandler = new ManagerPassiveCommandHandler(
				GlobalParameters.MANAGER_COMMAND_PORT);
	}

	/**
	 * CollectorProfile contains the basic information about a registered
	 * collector.
	 * 
	 */
	public class CollectorProfile {
		public String collectorIPAddress;
		public String collectorBrokerAddress;
		public long secondSinceLastConnected;
		public Set<String> monitorSet;

		public CollectorProfile(String collectorIPAddress, String collectorBrokerAddress) {
			super();
			this.collectorIPAddress = collectorIPAddress;
			this.collectorBrokerAddress = collectorBrokerAddress;
			this.secondSinceLastConnected = 0;
			this.monitorSet = new HashSet<String>();
		}

	}

	// /**
	// * Assign the new registered monitor to collector with least load.
	// * @param monitorName
	// * @return
	// */
	// private String assignCollector(String monitorName) {
	// String assignedCollector = null;
	// CollectorProfile assignedCollectorProfile = null;
	// int leastLoad = Integer.MAX_VALUE;
	//
	// for(Map.Entry<String, CollectorProfile> entry :
	// collectorsProfiles.entrySet()) {
	// int collectorLoad = entry.getValue().monitorSet.size();
	// if(collectorLoad < leastLoad) {
	// assignedCollector = entry.getKey();
	// assignedCollectorProfile = entry.getValue();
	// leastLoad = collectorLoad;
	// }
	// if(collectorLoad == 0)
	// break;
	// }
	// assignedCollectorProfile.monitorSet.add(monitorName);
	// collectorsProfiles.put(assignedCollector, assignedCollectorProfile);
	// Out.println("Assign [" + monitorName + "] to [" +
	// assignedCollectorProfile.collectorBrokerAddress + "]");
	// return assignedCollectorProfile.collectorBrokerAddress;
	// }

	/**
	 * The handler to receive commands for manager.
	 * 
	 */
	class ManagerPassiveCommandHandler extends PassiveCommandHandler {

		public ManagerPassiveCommandHandler(String servicePort) {
			super(servicePort);
			out.println("Start command service at " + this.brokerAddress);
		}

		@Override
		public void onMessage(Message commandMessage) {
			if (commandMessage instanceof TextMessage) {

				String commandJson;
				try {
					commandJson = ((TextMessage) commandMessage).getText();
					JsonObject commandJsonObj = (JsonObject) jsonParser.parse(commandJson);
					String eventType = commandJsonObj.get("type").getAsString();
					TextMessage responseMessage = null;
					if(eventType.equals("monitor-registration")) {
						handleMonitorRegistration(commandJsonObj, commandMessage);
					}
					else if(eventType.equals("collector-registration")) {
						handleCollectorRegistration(commandJsonObj, commandMessage);
					}
					else if(eventType.equals("retrieve-collectors")) {
						handleRetrieveCollectors(commandMessage);
					}
					else if(eventType.equals("retrieve-monitors")) {
						handleRetrieveMonitors(commandMessage);
					}
					else if(eventType.equals("retrieve-monitors-by-collector")) {
						handleRetrieveMonitorsByCollector(commandJsonObj, commandMessage);
					}
					else {
						out.println("Receive unidentified command.");
					}
					
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}

		/*
		 * If success, return {type: "monitor-registration-response", value:
		 * "success"}
		 */
		private void handleMonitorRegistration(JsonObject commandJsonObj, Message commandMessage)
				throws JMSException {
			String monitorName = commandJsonObj.get("machineIPAddress").getAsString();
			JsonObject responseJson = new JsonObject();
			responseJson.addProperty("type", "monitor-registration-response");
			responseJson.addProperty("value", "success");

			// find an available collector
			String assignedCollectorBrokerAddress = monitorAssigner.assignMonitorToCollector(monitorName);
			responseJson.addProperty("collectorCommandBrokerAddress", assignedCollectorBrokerAddress);

			TextMessage responseMessage = this.commandServiceSession.createTextMessage();
			responseMessage.setJMSCorrelationID(commandMessage.getJMSCorrelationID());
			responseMessage.setText(responseJson.toString());
			this.commandProducer.send(commandMessage.getJMSReplyTo(), responseMessage);
			out.println("Monitor [" + monitorName + "] registered.");
		}

		/**
		 * If success, return {type: "collector-registration-response", value:
		 * "success"}
		 */
		private void handleCollectorRegistration(JsonObject commandJsonObj, Message commandMessage)
				throws JMSException {
			String collectorIPAddress = commandJsonObj.get("collectorIPAddress").getAsString();
			String collectorBrokerAddress = commandJsonObj.get("collectorBrokerAddress").getAsString();
			CollectorProfile profile = new CollectorProfile(collectorIPAddress, collectorBrokerAddress);
			synchronized (collectorsProfiles) {
				collectorsProfiles.put(collectorIPAddress, profile);
			}
			JsonObject responseJson = new JsonObject();
			responseJson.addProperty("type", "collector-registration-response");
			responseJson.addProperty("value", "success");
			responseJson.add("alertsConfig", alertJsonConfig);
			out.println("Collector [" + collectorIPAddress + "] registered.");
			TextMessage responseMessage = this.commandServiceSession.createTextMessage();
			responseMessage.setJMSCorrelationID(commandMessage.getJMSCorrelationID());
			responseMessage.setText(responseJson.toString());
			this.commandProducer.send(commandMessage.getJMSReplyTo(), responseMessage);
		}

		/**
		 * If success, return {type: "retrieve-collectors-response", collectors:
		 * [collectorIP_1, ..., collectorIP_n]}
		 * 
		 * @param commandMessage
		 * @throws JMSException
		 */
		private void handleRetrieveCollectors(Message commandMessage) throws JMSException {
			out.println("Receive retrieve-collectors command.");
			JsonObject responseJson = new JsonObject();
			responseJson.addProperty("type", "retrieve-collectors-response");
			JsonArray collectorArray = new JsonArray();
			synchronized (collectorsProfiles) {
				for (Map.Entry<String, CollectorProfile> collectorEntry : collectorsProfiles.entrySet()) {
					collectorArray.add(new JsonPrimitive(collectorEntry.getKey()));
				}
			}
			responseJson.add("collectors", collectorArray);

			TextMessage responseMessage = this.commandServiceSession.createTextMessage();
			responseMessage.setJMSCorrelationID(commandMessage.getJMSCorrelationID());
			responseMessage.setText(responseJson.toString());
			this.commandProducer.send(commandMessage.getJMSReplyTo(), responseMessage);
		}

		/**
		 * Retrieve the monitors assigned to a specified collector.
		 * 
		 * @param collectorIP
		 * @return
		 */
		private JsonObject getMonitorsByCollector(String collectorIP) {
			JsonObject infoJson = new JsonObject();
			infoJson.addProperty("collector", collectorIP);
			JsonArray monitorArray = new JsonArray();
			CollectorProfile collectorProfile = collectorsProfiles.get(collectorIP);
			if(collectorProfile == null) {
				return null;
			}
			for (String motnitorIP : collectorProfile.monitorSet) {
				monitorArray.add(new JsonPrimitive(motnitorIP));
			}
			infoJson.add("monitors", monitorArray);
			return infoJson;
		}

		/**
		 * If success, return {type: "retrieve-monitors-response", monitors:
		 * [monitorIP_1, ..., monitorIP_n]}
		 * 
		 * @param commandMessage
		 * @throws JMSException
		 */
		private void handleRetrieveMonitors(Message commandMessage) throws JMSException {
			out.println("Receive retrieve-monitors command.");
			JsonObject responseJson = new JsonObject();
			responseJson.addProperty("type", "retrieve-monitors-response");

			JsonArray monitorsJson = new JsonArray();
			synchronized (collectorsProfiles) {
				for (Map.Entry<String, CollectorProfile> profileEntry : collectorsProfiles.entrySet()) {
					JsonObject monitorsByCollectorJson = getMonitorsByCollector(profileEntry.getKey());
					for (JsonElement monitorJson : monitorsByCollectorJson.get("monitors").getAsJsonArray()) {
						monitorsJson.add(monitorJson);
					}
				}
			}
			responseJson.add("monitors", monitorsJson);
			TextMessage responseMessage = this.commandServiceSession.createTextMessage();
			responseMessage.setJMSCorrelationID(commandMessage.getJMSCorrelationID());
			responseMessage.setText(responseJson.toString());
			this.commandProducer.send(commandMessage.getJMSReplyTo(), responseMessage);
		}
		
		/**
		 * If success, return {type: "retrieve-monitor-by-collector"}
		 * @param commandMessage
		 * @throws JMSException
		 */
		private void handleRetrieveMonitorsByCollector(JsonObject commandJsonObj, Message commandMessage) throws JMSException {
			out.println("Receive retrieve-monitors-by-collector command.");
			
			if(!commandJsonObj.has("collector")) {
				return;
			}
			
			String collectorIP = commandJsonObj.get("collector").getAsString();
			JsonObject responseJson = getMonitorsByCollector(collectorIP);
			if(responseJson == null) {
				return;
			}
			responseJson.addProperty("type", "retrieve-monitors-by-collector-response");
			
			JsonArray monitorsJson = new JsonArray();
			synchronized (collectorsProfiles) {
				for (Map.Entry<String, CollectorProfile> profileEntry : collectorsProfiles.entrySet()) {
					JsonObject monitorsByCollectorJson = getMonitorsByCollector(profileEntry.getKey());
					for (JsonElement monitorJson : monitorsByCollectorJson.get("monitors").getAsJsonArray()) {
						monitorsJson.add(monitorJson);
					}
				}
			}
			responseJson.add("monitors", monitorsJson);
			
			TextMessage responseMessage = this.commandServiceSession.createTextMessage();
			responseMessage.setJMSCorrelationID(commandMessage.getJMSCorrelationID());
			responseMessage.setText(responseJson.toString());
			this.commandProducer.send(commandMessage.getJMSReplyTo(), responseMessage);
		}

	}

	public static void main(String[] args) {
		MonitoringManager manager = MonitoringManager.getInstance();
	}

}
