package sysmon.monitor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import sysmon.common.InitiativeCommandHandler;
import sysmon.common.metadata.CpuMetadata;
import sysmon.common.metadata.DiskMetadata;
import sysmon.common.metadata.MachineMetadata;
import sysmon.common.metadata.MemoryMetadata;
import sysmon.monitor.crawler.CPUCrawler;
import sysmon.monitor.crawler.Crawler;
import sysmon.monitor.crawler.MemoryCrawler;
import sysmon.util.GlobalParameters;
import sysmon.util.IPUtil;
import sysmon.util.Out;

import com.google.gson.JsonObject;

/**
 * The monitor that fetches variant kinds of metadata from the machine.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public class Monitor {
	
	private Out out;
	private String managerBrokerAddress;
	private String machinerIPAddress;
	private long moniterInterval = 1;	//	In seconds
	private long metaDataSendingInterval = 1;	//	In seconds
	private Map<String, CrawlerWorker> crawlers;
	private JsonObject assembledStaticMetaData;
	private JsonObject assembledDynamicMetaData;
	private MonitorCommandSender commandSender;
	
	private String collectorCommandBrokerAddress;
	private Boolean collectorCommandBrokerAddressAvailable = false;
	
	public Monitor(String managerBrokerAddress, long monitoringInterval, long metaDataSendingInterval) {
		this.managerBrokerAddress = managerBrokerAddress;
		this.machinerIPAddress = IPUtil.getFirstAvailableIP();
		this.crawlers = new HashMap<String, CrawlerWorker>();
		this.assembledStaticMetaData = new JsonObject();
		this.assembledDynamicMetaData = new JsonObject();
		setMonitorInterval(monitoringInterval);
		setMetaDataSendingInterval(metaDataSendingInterval);
		this.commandSender = new MonitorCommandSender(this.managerBrokerAddress);
	}
	
	public Monitor(String managerBrokerAddress) {
		this(managerBrokerAddress, 1, 1);
	}
	
	public void setMonitorInterval(long second) {
		this.moniterInterval = second;
	}
	
	public void setMetaDataSendingInterval(long second) {
		this.metaDataSendingInterval = second;
	}
	
	/**
	 * Start the monitor.
	 */
	public void start() {
		assembleStaticMetaData();
		startMonitorWorkers();
		commandSender.registerToManager();
		MetadataMessageSender metadataSender = new MetadataMessageSender();
		Thread metaDataSenderThread = new Thread(metadataSender);
		metaDataSenderThread.start();
	}
	
	/**
	 * Add a crawler to the monitor.
	 * @param crawler
	 */
	public void addCrawler(Crawler crawler) {
		CrawlerWorker crawlerWorker = new CrawlerWorker(crawler, this.moniterInterval * 1000);
		this.crawlers.put(crawler.getCrawlerName(), crawlerWorker);
	}
	
	/**
	 * Start all monitor workers to crawl the meta data.
	 */
	private void startMonitorWorkers() {
		for(Map.Entry<String, CrawlerWorker> entry : this.crawlers.entrySet()) {
			Thread thread = new Thread(entry.getValue());
			thread.start();
		}
	}
	
	/**
	 * Assemble the static metadata from each crawler.
	 */
	private void assembleStaticMetaData() {
		this.assembledDynamicMetaData.addProperty("machine-name", this.machinerIPAddress);
		for(Map.Entry<String, CrawlerWorker> entry : crawlers.entrySet()) {
			this.assembledStaticMetaData.add(entry.getKey(), entry.getValue().getCrawler().getStaticMetaData());
		}
	}
	
	public JsonObject getDynamicMetaData() {
		synchronized(assembledStaticMetaData) {
			return assembledDynamicMetaData;
		}
	}
	
//	/**
//	 * Assemble the meta data crawled by all the crawlers.
//	 * @return
//	 */
//	public JsonObject assembleDynamicMetaData() {
//		JsonObject newAssembledMetaData = new JsonObject();
//		Date newDate = new Date();
//		newAssembledMetaData.addProperty("timestamp", newDate.getTime() / 1000);
//		for(Map.Entry<String, CrawlerWorker> entry : crawlers.entrySet()) {
//			newAssembledMetaData.add(entry.getKey(), entry.getValue().getCrawler().getDynamicMetaData());
//		}
//		
//		return newAssembledMetaData;
//	}
	
	public MachineMetadata assembleObject() {
		Date newDate = new Date();
		MachineMetadata machineMetadata = new MachineMetadata(newDate.getTime() / 1000, this.machinerIPAddress);
		
		for(Map.Entry<String, CrawlerWorker> entry : crawlers.entrySet()) {
			Object metadataObject = entry.getValue().getCrawler().getMetadataObject();
			if(metadataObject instanceof CpuMetadata) {
				machineMetadata.setCpu((CpuMetadata)metadataObject);
			}
			else if(metadataObject instanceof MemoryMetadata) {
				machineMetadata.setMemory((MemoryMetadata)metadataObject);
			}
			else if (metadataObject instanceof DiskMetadata) {
				machineMetadata.setDisk((DiskMetadata)metadataObject);
			}
		}
		
		return machineMetadata;
	}
	
	
	/**
	 * MonitorWork continuously fetch the dynamic metadata using a specified Crawler.
	 *
	 */
	public class CrawlerWorker implements Runnable{
		
		private Crawler crawler;
		private long sleepTimeInMillisecond;
		
		public CrawlerWorker(Crawler crawler, long sleepTimeInMillisecond) {
			this.crawler = crawler;
			this.sleepTimeInMillisecond = sleepTimeInMillisecond;
		}
		
		public Crawler getCrawler() {
			return crawler;
		}

		@Override
		public void run() {
			while(true) { 
				crawler.updateDynamicMetaData();
				try {
					Thread.sleep(sleepTimeInMillisecond);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	
	/**
	 * Send the dynamic meta data to broker periodically.
	 *
	 */
	class MetadataMessageSender implements Runnable{
		
		private MessageProducer metaDataProducer;
		private Session metaDataSession;
		
		public MetadataMessageSender() {
			try {
				initMetaDataStreamService();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		
		public void initMetaDataStreamService() throws JMSException {
			//	wait for collector broker address available
			while(collectorCommandBrokerAddress == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(collectorCommandBrokerAddress);
			Connection connection = connectionFactory.createConnection();
			connection.start();
			metaDataSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			                                                      
			Topic topic = metaDataSession.createTopic("command");
			metaDataProducer = metaDataSession.createProducer(topic);
			metaDataProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		}
		
		public void sendMonitoredData() throws JMSException{
			TextMessage metadataJsonMessage = metaDataSession.createTextMessage();
			
			ObjectMessage metadataObjMessage = metaDataSession.createObjectMessage();
			metadataObjMessage.setObject(assembleObject());
			
			String correlateionID = UUID.randomUUID().toString();
			metadataJsonMessage.setJMSCorrelationID(correlateionID);
			metadataObjMessage.setJMSCorrelationID(correlateionID);
			this.metaDataProducer.send(metadataObjMessage);
			
		}
		
		@Override
		public void run() {
			while(true) {
					try {
						sendMonitoredData();
					} catch (JMSException e) {
						if(e.getMessage().equals("The Session is closed")) {
							out.println("Cannot connect to collector [" + collectorCommandBrokerAddress + "]");
						}
					}
					try {
						Thread.sleep(metaDataSendingInterval * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
		}
	}
	
	/**
	 * MonitorCommandSender is in charge of sending command to manager.
	 *
	 */
	class MonitorCommandSender extends InitiativeCommandHandler {

		public MonitorCommandSender(String commandBrokerAddress) {
			super(commandBrokerAddress);
		}

		/**
		 * Register the monitor.
		 * @throws JMSException 
		 */
		private void registerToManager() {
			TextMessage registerCommandMessage;
			try {
				registerCommandMessage = commandServiceSession.createTextMessage();
				JsonObject commandJson = new JsonObject();
				commandJson.addProperty("type", "monitor-registration");
				commandJson.addProperty("machineIPAddress", machinerIPAddress);
				String correlateionID = UUID.randomUUID().toString();
				registerCommandMessage.setJMSCorrelationID(correlateionID);
				registerCommandMessage.setJMSReplyTo(this.commandServiceTemporaryQueue);
				registerCommandMessage.setText(commandJson.toString());
				commandProducer.send(registerCommandMessage);
			} catch (JMSException e) {
				out.error("Register to manager failed.");
				System.exit(1);
			}
		}
		
		/**
		 * Enroll to an assigned collector.
		 * @throws JMSException
		 */
		private void monitorEnroll() throws JMSException {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(collectorCommandBrokerAddress);
			Connection connection = connectionFactory.createConnection();
			connection.start();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			Topic topic = session.createTopic("command");
			MessageProducer producer = session.createProducer(topic);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("type", "monitor-enroll");
			jsonObj.addProperty("machineIPAddress", machinerIPAddress);
			jsonObj.add("staticMetadata", assembledStaticMetaData);
			TextMessage enrollCommandMessage = session.createTextMessage();
			enrollCommandMessage.setText(jsonObj.toString());
			producer.send(enrollCommandMessage);
		}
		
		@Override
		public void onMessage(Message commandMessage) {
			if(commandMessage instanceof TextMessage) {
				/*
				 * If success, receive {type: "monitor-registration-response", value: "success"}
				 */
				try {
					String commandJson = ((TextMessage) commandMessage).getText();
					JsonObject jsonObj = (JsonObject)jsonParser.parse(commandJson);
					if(jsonObj.get("type").getAsString().equals("monitor-registration-response") && 
							jsonObj.get("value").getAsString().equals("success")) {
						out.println(commandJson);
						collectorCommandBrokerAddress = jsonObj.get("collectorCommandBrokerAddress").getAsString();
						out.println("Intend to enroll to " + collectorCommandBrokerAddress);
						monitorEnroll();
						out.println("Registration successfully.");
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
				
			}
		}

	}
	
	public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("usage: monitor manager-IP");
			System.out.println("\tmanager-ip\tThe IP address of manager.");
			System.exit(1);
		}
		String managerBrokerAddress = "tcp://" + args[0] + ":" + GlobalParameters.MANAGER_COMMAND_PORT;
		Monitor m = new Monitor(managerBrokerAddress);
		Crawler cpuCrawler = new CPUCrawler("cpu");
		Crawler memoryCrawler = new MemoryCrawler("memory");
		m.addCrawler(cpuCrawler);
		m.addCrawler(memoryCrawler);
		m.start();
	}
	
	
}
