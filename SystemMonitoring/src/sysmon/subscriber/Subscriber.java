package sysmon.subscriber;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import sysmon.util.GlobalParameters;
import sysmon.util.IPUtil;

/**
 * Subscriber is used to subscribe the metadata sent to multiple collectors.
 * 
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 * 
 */
public class Subscriber {

	final private String brokerUrl;
	final private String destination;
	private BrokerService broker;
	private List<String> inputBrokerAddresses;
	private List<SubscribeWorker> workerList;

	public Subscriber(List<String> inputBrokerAddresses) {
		this.brokerUrl = "tcp://" + IPUtil.getFirstAvailableIP() + ":" + GlobalParameters.SUBSCRIBE_COMMAND_PORT;
		this.destination = "command";
		this.inputBrokerAddresses = inputBrokerAddresses;
		this.workerList = new ArrayList<SubscribeWorker>();
		createBroker();
		startWorkers();
	}

	private void createBroker() {
		broker = new BrokerService();
		broker.setBrokerName("testBroker");
		try {
			broker.setPersistent(false);
			broker.setUseJmx(false);
			broker.addConnector(brokerUrl);
			broker.start();
			System.out.println("Create gather broker:" + brokerUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startWorkers() {
		for (final String brokerAddress : inputBrokerAddresses) {
			this.workerList.add(new SubscribeWorker(brokerAddress, brokerUrl, destination));
		}
	}

	public void stop() {
		for(SubscribeWorker worker : this.workerList) {
			try {
				worker.stop();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		try {
			broker.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * SubscribeWorker is in charge of receiving message from one collector.
	 * 
	 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
	 * 
	 */
	class SubscribeWorker implements MessageListener {

		private String inputBrokerAddress;
		private String outputBrokerAddress;
		private String outputDestination;

		private MessageConsumer consumer;
		private MessageProducer producer;
		private Session inputSession;
		private Connection inputConnection;
		private Session outputSession;
		private Connection outputConnection;

		public SubscribeWorker(String inputBrokerAddress,
				String outputBrokerAddress, String outputDestination) {
			this.inputBrokerAddress = inputBrokerAddress;
			this.outputBrokerAddress = outputBrokerAddress;
			this.outputDestination = outputDestination;
			try {
				init();
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}

		private void init() throws JMSException {
			ConnectionFactory inputConnectionFactory = new ActiveMQConnectionFactory(
					inputBrokerAddress);
			inputConnection = inputConnectionFactory.createConnection();
			inputConnection.start();

			inputSession = inputConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Topic inputTopic = inputSession.createTopic("command");
			consumer = inputSession.createConsumer(inputTopic);
			consumer.setMessageListener(this);

			ConnectionFactory outputConnectionFactory = new ActiveMQConnectionFactory(
					outputBrokerAddress);
			outputConnection = outputConnectionFactory.createConnection();
			outputConnection.start();

			outputSession = outputConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Topic outputTopic = outputSession.createTopic(outputDestination);
			producer = outputSession.createProducer(outputTopic);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		}
		
		public void stop() throws JMSException {
			producer.close();
			consumer.close();
			outputSession.close();
			outputConnection.close();
			inputSession.close();
			inputConnection.close();
		}

		@Override
		public void onMessage(Message message) {
			try {
				producer.send(message); // redistribute the messages
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}

}
