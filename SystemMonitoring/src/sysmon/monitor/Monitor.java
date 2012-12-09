package sysmon.monitor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import sysmon.monitor.crawler.Crawler;
import sysmon.util.IPUtil;

import com.google.gson.JsonObject;

/**
 * The monitor that fetches variant kinds of metadata from the machine.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public class Monitor {
	
	private String ipAddress;
	private long moniterInterval = 1;	//	In seconds
	private long metaDataSendingInterval = 1;	//	In seconds
	private Map<String, CrawlerWorker> crawlers;
	private JsonObject assembledStaticMetaData;
	private JsonObject assembledDynamicMetaData;
	
	public Monitor(long monitoringInterval, long metaDataSendingInterval) {
		this.ipAddress = IPUtil.getFirstAvailableIP();
		this.crawlers = new HashMap<String, CrawlerWorker>();
		this.assembledStaticMetaData = new JsonObject();
		this.assembledDynamicMetaData = new JsonObject();
		setMonitorInterval(monitoringInterval);
		setMetaDataSendingInterval(metaDataSendingInterval);
	}
	
	public Monitor() {
		this(1, 1);
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
		for(Map.Entry<String, CrawlerWorker> entry : crawlers.entrySet()) {
			this.assembledStaticMetaData.add(entry.getKey(), entry.getValue().getCrawler().getStaticMetaData());
		}
	}
	
	public JsonObject getDynamicMetaData() {
		synchronized(assembledStaticMetaData) {
			return assembledDynamicMetaData;
		}
	}
	
	/**
	 * Assemble the meta data crawled by all the crawlers.
	 * @return
	 */
	public JsonObject assembleDynamicMetaData() {
		JsonObject newAssembledMetaData = new JsonObject();
		Date newDate = new Date();
		newAssembledMetaData.addProperty("timestamp", newDate.getTime() / 1000);
		for(Map.Entry<String, CrawlerWorker> entry : crawlers.entrySet()) {
			newAssembledMetaData.add(entry.getKey(), entry.getValue().getCrawler().getDynamicMetaData());
		}
		
		return newAssembledMetaData;
	}
	
	
	/**
	 * MonitorWork continuously fetch the dynamic metadata using a specified Crawler.
	 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
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
	 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
	 *
	 */
	class MetadataMessageSender implements Runnable{
		
		private String brokerIPAddress;
		private BrokerService broker;
		private MessageProducer metaDataProducer;
		private Session metaDataSession;
		
		public MetadataMessageSender() {
			this.brokerIPAddress = ipAddress;
			createBroker();
			try {
				initMetaDataStreamService();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		
		private void createBroker() {
			broker = new BrokerService();
			broker.setBrokerName("testBroker");
			try {
				broker.setPersistent(false);
				broker.setUseJmx(false);
				broker.addConnector("tcp://" + this.brokerIPAddress + ":32100");
				broker.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void initMetaDataStreamService() throws JMSException {
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + this.brokerIPAddress + ":32100");
			Connection connection = connectionFactory.createConnection();
			connection.start();
			metaDataSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			                                                      
			Topic topic = metaDataSession.createTopic("metaData");
			metaDataProducer = metaDataSession.createProducer(topic);
			metaDataProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		}
		
		public void sendMonitoredData() throws Exception{
			TextMessage message = metaDataSession.createTextMessage();
			JsonObject assembledDynamicMetaData = assembleDynamicMetaData();
			message.setText(assembledDynamicMetaData.toString());
			
			String correlateionID = UUID.randomUUID().toString();
			message.setJMSCorrelationID(correlateionID);
			this.metaDataProducer.send(message);
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					sendMonitoredData();
					Thread.sleep(metaDataSendingInterval * 1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
