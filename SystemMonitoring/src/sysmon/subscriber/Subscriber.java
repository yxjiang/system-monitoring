package sysmon.subscriber;

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

	public Subscriber(List<String> inputBrokerAddresses) {
		this.brokerUrl = "tcp://" + IPUtil.getFirstAvailableIP() + ":"
				+ GlobalParameters.SUBSCRIBE_COMMAND_PORT;
		this.destination = "command";
		this.inputBrokerAddresses = inputBrokerAddresses;
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
			new SubscribeWorker(brokerAddress, brokerUrl, destination);
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
			Connection inputConnection = inputConnectionFactory.createConnection();
			inputConnection.start();

			Session inputSession = inputConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Topic inputTopic = inputSession.createTopic("command");
			consumer = inputSession.createConsumer(inputTopic);
			consumer.setMessageListener(this);

			ConnectionFactory outputConnectionFactory = new ActiveMQConnectionFactory(
					outputBrokerAddress);
			Connection outputConnection = outputConnectionFactory.createConnection();
			outputConnection.start();

			Session outputSession = outputConnection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Topic outputTopic = outputSession.createTopic(outputDestination);
			producer = outputSession.createProducer(outputTopic);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
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
