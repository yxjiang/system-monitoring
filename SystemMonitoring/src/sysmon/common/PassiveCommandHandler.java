package sysmon.common;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import sysmon.util.IPUtil;
import sysmon.util.Out;

/**
 * The PassiveCommandServiceHandler receives the commands passively and then responses.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public abstract class PassiveCommandHandler extends	CommandHandler {

	protected String ipAddress;
	protected String brokerAddress;
	protected String servicePort;

	
	public PassiveCommandHandler(String servicePort) {
		super();
		this.ipAddress = IPUtil.getFirstAvailableIP();
		this.servicePort = servicePort;
		this.brokerAddress = "tcp://" + ipAddress + ":" + servicePort;
		createCommandServiceBroker();
		try {
			initCommandService();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the command service broker. 
	 */
	private void createCommandServiceBroker() {
		broker = new BrokerService();
		broker.setBrokerName("commandBroker");
		try {
			broker.setPersistent(false);
			broker.setUseJmx(false);
			broker.addConnector(brokerAddress);
			broker.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void initCommandService() throws JMSException {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerAddress);
		Connection connection = connectionFactory.createConnection();
		connection.start();

		commandServiceSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		commandServiceTopic = commandServiceSession.createTopic("command");
		
		commandProducer = commandServiceSession.createProducer(null);
		commandProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		
		commandConsumer = commandServiceSession.createConsumer(commandServiceTopic);
		commandConsumer.setMessageListener(this);

	}

}
