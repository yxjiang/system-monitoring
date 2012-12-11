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

import sysmon.common.InitiativeCommandHandler;
import sysmon.common.MetadataBuffer;
import sysmon.common.PassiveCommandHandler;
import sysmon.common.ThreadSafeMetadataBuffer;
import sysmon.util.GlobalParameters;
import sysmon.util.IPUtil;
import sysmon.util.Out;

import com.google.gson.JsonObject;

/**
 * The collector collects the metadata sent by the assigned monitors.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public class Collector {
	
	private String managerBrokerAddress;
	
	private int metadataStreamCapacity;
	private String collectorIPAddress;
	private String collectorBrokerAddress;
	private Map<String, MonitorProfile> monitorsAddresses;
	private CollectorCommandSender commandSender;
	private CollectorCommandReceiver commandReceiver;
	
	public Collector(String managerBrokerAddress, int capacity) {
		this.metadataStreamCapacity = capacity;
		this.collectorIPAddress = IPUtil.getFirstAvailableIP();
		this.collectorBrokerAddress = "tcp://" + this.collectorIPAddress + ":" + GlobalParameters.COLLECTOR_COMMAND_PORT;
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
		public String monitorDataBrokerAddress;
//		public String monitorCommandBrokerAddress;
		public String staticMetadata;
		public long secondSinceLastAccess;
		public MetadataBuffer<JsonObject> metadataBuffer;
		private MetadataReceiveHandler metadataHandler;
		
		public MonitorProfile(String monitorIPAddress, String monitorDataBrokerAddress, 
				String staticMetadata) {
			super();
			this.monitorIPAddress = monitorIPAddress;
			this.monitorDataBrokerAddress = monitorDataBrokerAddress;
//			this.monitorCommandBrokerAddress = monitorCommandBrokerAddress;
			this.staticMetadata = staticMetadata;
			this.secondSinceLastAccess = 0;
			this.metadataBuffer = new ThreadSafeMetadataBuffer<JsonObject>(metadataStreamCapacity);
			this.metadataHandler = new MetadataReceiveHandler(this.monitorDataBrokerAddress);
		}
		
		
		/**
		 *	MetadataReceiverHandler is in charge of receive the message from the target monitors. 
		 *
		 */
		class MetadataReceiveHandler implements MessageListener {

			private Session metaDataSession;
			private MessageConsumer metaDataConsumer;
			
			public MetadataReceiveHandler(String monitorBroker) {
				try {
					initMetadataReceiveService();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
			
			private void initMetadataReceiveService() throws JMSException {
				ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(monitorDataBrokerAddress);
				Out.println(monitorDataBrokerAddress);
				Connection connection = connectionFactory.createConnection();
				connection.start();
				metaDataSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				                                                      
				Topic topic = metaDataSession.createTopic("metaData");
				metaDataConsumer = metaDataSession.createConsumer(topic);
				metaDataConsumer.setMessageListener(this);
			}
			
			@Override
			public void onMessage(Message metadataMessage) {
				if(metadataMessage instanceof TextMessage) {
					String strJsonObj;
					try {
						strJsonObj = ((TextMessage)metadataMessage).getText();
						Out.println("Receive data:" + strJsonObj);
					} catch (JMSException e) {
						e.printStackTrace();
					}
					
					JsonObject metadataObj = new JsonObject();
					metadataBuffer.insert(metadataObj);
				}
			}
			
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
					if(jsonObj.get("type").getAsString().equals("monitor-enroll")) {
						String enrollMonitorDataBrokerAddress = jsonObj.get("monitorDataBrokerAddress").getAsString();
						String enrollMonitorIPAddress = jsonObj.get("monitorIPAddress").getAsString();
						Out.println(enrollMonitorIPAddress + " come to enroll.");
						JsonObject staticMetadataObj = jsonObj.get("staticMetadata").getAsJsonObject();
						MonitorProfile monitorProfile = new MonitorProfile(enrollMonitorIPAddress, enrollMonitorDataBrokerAddress, staticMetadataObj.toString());
						monitorsAddresses.put(enrollMonitorIPAddress, monitorProfile);
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
			commandJson.addProperty("collectorBrokerAddress", collectorBrokerAddress);
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
