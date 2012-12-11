package sysmon.collector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import sysmon.common.InitiativeCommandHandler;
import sysmon.common.MetadataBuffer;
import sysmon.common.PassiveCommandHandler;
import sysmon.common.ThreadSafeMetadataBuffer;
import sysmon.util.GlobalParameters;
import sysmon.util.IPUtil;
import sysmon.util.Out;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The collector collects the metadata sent by the assigned monitors.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public class Collector {
	
	private String managerBrokerAddress;
	
	private int metadataStreamCapacity;
	private String collectorIPAddress;
	private String collectorCommandBrokerAddress;
	private Map<String, MonitorProfile> monitorsAddresses;
	private CollectorCommandSender commandSender;
	private CollectorCommandReceiver commandReceiver;
	
	public Collector(String managerBrokerAddress, int capacity) {
		this.metadataStreamCapacity = capacity;
		this.collectorIPAddress = IPUtil.getFirstAvailableIP();
		this.collectorCommandBrokerAddress = "tcp://" + this.collectorIPAddress + ":" + GlobalParameters.COLLECTOR_COMMAND_PORT;
		this.managerBrokerAddress = managerBrokerAddress;
		this.monitorsAddresses = new HashMap<String, MonitorProfile>();
		this.commandSender = new CollectorCommandSender(this.managerBrokerAddress);
		this.commandReceiver = new CollectorCommandReceiver(GlobalParameters.COLLECTOR_COMMAND_PORT);
	}
	
	public void start() {
		try {
			commandSender.registerToManager();
		} catch (JMSException e) {
			e.printStackTrace();
		}
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
		public MetadataBuffer<JsonObject> metadataBuffer;
		
		public MonitorProfile(String monitorIPAddress, String staticMetadata) {
			super();
			this.monitorIPAddress = monitorIPAddress;
//			this.monitorCommandBrokerAddress = monitorCommandBrokerAddress;
			this.staticMetadata = staticMetadata;
			this.secondSinceLastAccess = 0;
			this.metadataBuffer = new ThreadSafeMetadataBuffer<JsonObject>(metadataStreamCapacity);
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
						Out.println(enrollMonitorIPAddress + " come to enroll.");
						JsonObject staticMetadataObj = jsonObj.get("staticMetadata").getAsJsonObject();
						MonitorProfile monitorProfile = new MonitorProfile(enrollMonitorIPAddress, staticMetadataObj.toString());
						monitorsAddresses.put(enrollMonitorIPAddress, monitorProfile);
					}
					else if(type.equals("metadata")) {	//	receive metadata from monitor
						String monitorName = jsonObj.get("machineIPAddress").getAsString();
						Out.println("Recieve data from [" + monitorName + "]");
					}
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
		private void registerToManager() throws JMSException {
			TextMessage registerCommandMessage = commandServiceSession.createTextMessage();
			JsonObject commandJson = new JsonObject();
			commandJson.addProperty("type", "collector-registration");
			commandJson.addProperty("collectorIPAddress", collectorIPAddress);
			commandJson.addProperty("collectorBrokerAddress", collectorCommandBrokerAddress);
			String correlateionID = UUID.randomUUID().toString();
			registerCommandMessage.setJMSCorrelationID(correlateionID);
			registerCommandMessage.setJMSReplyTo(this.commandServiceTemporaryQueue);
			registerCommandMessage.setText(commandJson.toString());
			commandProducer.send(registerCommandMessage);
		}

		@Override
		public void onMessage(Message commandMessage) {
			if(commandMessage instanceof TextMessage) {
				/*
				 * If success, receive {type: "monitor-registration-response", value: "success"}
				 */
				try {
					String commandJson = ((TextMessage) commandMessage).getText();
					Out.println(commandJson);
					JsonObject jsonObj = (JsonObject)jsonParser.parse(commandJson);
					if(jsonObj.get("type").getAsString().equals("collector-registration-response") && 
							jsonObj.get("value").getAsString().equals("success")) {
						Out.println("Registration successfully.");
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	

}
