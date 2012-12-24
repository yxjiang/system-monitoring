package sysmon.collector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import sysmon.collector.alert.CpuUsageAlert;
import sysmon.common.InitiativeCommandHandler;
import sysmon.common.PassiveCommandHandler;
import sysmon.common.metadata.MachineMetadata;
import sysmon.util.GlobalParameters;
import sysmon.util.IPUtil;
import sysmon.util.Out;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The collector collects the metadata sent by the assigned monitors.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public class Collector {
	private Out out;
	private String managerBrokerAddress;
	
	private int metadataStreamCapacity;
	private String collectorIPAddress;
	private String collectorCommandBrokerAddress;
	private Map<String, MonitorProfile> monitorsAddresses;
	private CollectorCommandSender commandSender;
	private CollectorCommandReceiver commandReceiver;
	private CEPStream cepStream;
	
	public Collector(String managerBrokerAddress, int capacity) {
		this.out = new Out();
		this.metadataStreamCapacity = capacity;
		this.collectorIPAddress = IPUtil.getFirstAvailableIP();
		this.collectorCommandBrokerAddress = "tcp://" + this.collectorIPAddress + ":" + GlobalParameters.COLLECTOR_COMMAND_PORT;
		this.managerBrokerAddress = managerBrokerAddress;
		this.monitorsAddresses = new HashMap<String, MonitorProfile>();
		this.commandSender = new CollectorCommandSender(this.managerBrokerAddress);
		this.commandReceiver = new CollectorCommandReceiver(GlobalParameters.COLLECTOR_COMMAND_PORT);
	}
	
	public void start() {
		commandSender.registerToManager();
		out.println("Registered to manager. Start service at " + collectorCommandBrokerAddress);
	}
	
	/**
	 * Receive the collector response information from manager.
	 * Initialize all the alert monitors.
	 * @param collectorResponseJson
	 */
	private void initAlertMonitors(JsonArray alertsConfig) {
		this.cepStream = new CEPStream(alertsConfig);
		Thread cepThread = new Thread(cepStream);
		cepThread.start();
	}
	
	/**
	 * Records the detailed information about a monitor.
	 *
	 */
	class MonitorProfile {
		public String monitorIPAddress;
//		public String monitorCommandBrokerAddress;
		public String staticMetadata;
		public long secondSinceLastAccess;
		
		public MonitorProfile(String monitorIPAddress, String staticMetadata) {
			super();
			this.monitorIPAddress = monitorIPAddress;
//			this.monitorCommandBrokerAddress = monitorCommandBrokerAddress;
			this.staticMetadata = staticMetadata;
			this.secondSinceLastAccess = 0;
		}
		
	}
	
	/**
	 * Receive the commands and response. 
	 *
	 */
	class CollectorCommandReceiver extends PassiveCommandHandler {

		public CollectorCommandReceiver(String servicePort) {
			super(servicePort);
		}

		@Override
		public void onMessage(Message commandMessage) {
			if(commandMessage instanceof TextMessage) {
				String commandJson;
				try {
					commandJson = ((TextMessage) commandMessage).getText();
					JsonObject jsonObj = (JsonObject)jsonParser.parse(commandJson);
					String type = jsonObj.get("type").getAsString();
					if(type.equals("monitor-enroll")) {
						String enrollMonitorIPAddress = jsonObj.get("machineIPAddress").getAsString();
						out.println(enrollMonitorIPAddress + " come to enroll.");
						JsonObject staticMetadataObj = jsonObj.get("staticMetadata").getAsJsonObject();
						out.println("Static meta-data:" + staticMetadataObj.toString());
						MonitorProfile monitorProfile = new MonitorProfile(enrollMonitorIPAddress, staticMetadataObj.toString());
						monitorsAddresses.put(enrollMonitorIPAddress, monitorProfile);
					}
//					else if(type.equals("metadata")) {	//	receive metadata from monitor
//						String monitorName = jsonObj.get("machineIPAddress").getAsString();
//						Out.println("Recieve data from [" + monitorName + "]");
//						Out.println(jsonObj.toString());
//					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
			else if(commandMessage instanceof ObjectMessage) {	//	receive metadata
				ObjectMessage objMessage = (ObjectMessage)commandMessage;
				try {
					MachineMetadata machineMetadata = (MachineMetadata)objMessage.getObject();
					cepStream.cepService.getEPRuntime().sendEvent(machineMetadata);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}

		}
		
	}
	
	/**
	 * Send the commands to the manager or monitors.
	 *
	 */
	class CollectorCommandSender extends InitiativeCommandHandler {

		public CollectorCommandSender(String remoteBrokerAddress) {
			super(remoteBrokerAddress);
		}
		
		/**
		 * Register to manager.
		 * @throws JMSException 
		 */
		private void registerToManager() {
			TextMessage registerCommandMessage;
			try {
				registerCommandMessage = commandServiceSession.createTextMessage();
				JsonObject commandJson = new JsonObject();
				commandJson.addProperty("type", "collector-registration");
				commandJson.addProperty("collectorIPAddress", collectorIPAddress);
				commandJson.addProperty("collectorBrokerAddress", collectorCommandBrokerAddress);
				String correlateionID = UUID.randomUUID().toString();
				registerCommandMessage.setJMSCorrelationID(correlateionID);
				registerCommandMessage.setJMSReplyTo(this.commandServiceTemporaryQueue);
				registerCommandMessage.setText(commandJson.toString());
				commandProducer.send(registerCommandMessage);
			} catch (JMSException e) {
				out.error("Register to manager failed.");
			}

		}

		@Override
		public void onMessage(Message commandMessage) {
			if(commandMessage instanceof TextMessage) {
				/*
				 * If success, receive {type: "monitor-registration-response", value: "success"}
				 */
				try {
					String commandJson = ((TextMessage) commandMessage).getText();
					out.println(commandJson);
					JsonObject jsonObj = (JsonObject)jsonParser.parse(commandJson);
					if(jsonObj.get("type").getAsString().equals("collector-registration-response") && 
							jsonObj.get("value").getAsString().equals("success")) {
						JsonElement configElement = jsonObj.get("alertsConfig");
						if(configElement != null) {
							JsonArray configJsonArray = configElement.getAsJsonArray();
							initAlertMonitors(configJsonArray);
						}
						out.println("Registration successfully.");
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	/**
	 * Complex stream handler.
	 *
	 */
	class CEPStream implements Runnable{
		private EPServiceProvider cepService;
		
		CEPStream(JsonArray alertsConfig) {
			Configuration config = new Configuration();
			config.addEventTypeAutoName("sysmon.common.metadata");
			cepService = EPServiceProviderManager.getDefaultProvider(config);
			setAlertMonitors(alertsConfig);
		}
		
		/**
		 * Set the alerts based on the received alert configuration.
		 * @param collectorRegisterResponse
		 */
		private void setAlertMonitors(JsonArray alertsConfig) {
			for(JsonElement alert : alertsConfig) {
				JsonObject alertJson = (JsonObject)alert;
				String alertName = alertJson.get("type").getAsString();
				JsonObject parameters = alertJson.get("parameters").getAsJsonObject();
				if(alertName.equals("CpuUsageAlert")) {
					String timeWindowStr = parameters.get("timeWindow").getAsJsonObject().get("value").getAsString();
					String idleTimeAlertThresholdStr = parameters.get("idleTimeAlertThreshold").getAsJsonObject().get("value").getAsString();
					new CpuUsageAlert(cepService, Integer.parseInt(timeWindowStr), Float.parseFloat(idleTimeAlertThresholdStr));
				}
				out.println("Add alert [" + alertJson.get("type").getAsString() + "].");
			}
			
		}

		@Override
		public void run() {
			
		}
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("usage: collector manager-ip [time-window]");
			System.out.println("\tmanager-ip\tThe IP address of manager.");
			System.out.println("\ttime-window\tThe time length in seconds cached.");
			System.exit(1);
		}
		String managerBrokerAddress = "tcp://" + args[0] + ":" + GlobalParameters.MANAGER_COMMAND_PORT;
		int capacity = 0;
		if(args.length >= 2) {
			try{
				capacity = Integer.parseInt(args[1]);
			} catch(NumberFormatException e) {
				capacity = 60;
			}
			if(capacity < 0) {
				capacity = 60;
			}
		}
				
		Collector c = new Collector(managerBrokerAddress, capacity);
		c.start();
	}
	
}
