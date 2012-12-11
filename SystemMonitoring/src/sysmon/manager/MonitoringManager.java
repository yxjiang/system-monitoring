package sysmon.manager;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import sysmon.common.PassiveCommandHandler;
import sysmon.util.GlobalParameters;
import sysmon.util.Out;

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
	private Map<String, CollectorProfile> collectors;
	
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
		this.collectors = new HashMap<String, CollectorProfile>();
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
		public CollectorProfile(String collectorIPAddress,
				String collectorBrokerAddress) {
			super();
			this.collectorIPAddress = collectorIPAddress;
			this.collectorBrokerAddress = collectorBrokerAddress;
			this.secondSinceLastConnected = 0;
		}
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
					Out.println(commandJson);
					//	monitor registration event
					String eventType = jsonObj.get("type").getAsString();
					if(eventType.equals("monitor-registration")) {
						String monitorName = jsonObj.get("machine-name").getAsString();
						JsonObject responseJson = new JsonObject();
						responseJson.addProperty("type", "monitor-registration-response");
						responseJson.addProperty("value", "success");
						String firstCollectorCommandBrokerAddress = null;
						for(Map.Entry<String, CollectorProfile> entry : collectors.entrySet()) {
							firstCollectorCommandBrokerAddress = entry.getValue().collectorBrokerAddress;
							break;
						}
						Out.println("Assign " + monitorName + " to " + firstCollectorCommandBrokerAddress);
						responseJson.addProperty("collectorCommandBrokerAddress", firstCollectorCommandBrokerAddress);
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
						collectors.put(collectorIPAddress, profile);
						JsonObject responseJson = new JsonObject();
						responseJson.addProperty("type", "collector-registration-response");
						responseJson.addProperty("value", "success");
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
	
}
