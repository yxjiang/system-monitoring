package sysmon.common;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * The InitiativeCommandServiceHandler initiatively sends the command and waits for the response.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public abstract class InitiativeCommandHandler extends	CommandHandler {

	protected String remoteBrokerAddress;
	protected TemporaryQueue commandServiceTemporaryQueue;
	
	public InitiativeCommandHandler(String remoteBrokerAddress) {
		this.remoteBrokerAddress = remoteBrokerAddress;
		try {
			initCommandService();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void initCommandService() throws JMSException {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(remoteBrokerAddress);
		Connection connection = connectionFactory.createConnection();
		connection.start();

		commandServiceSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		commandServiceTopic = commandServiceSession.createTopic("command");
		
		commandProducer = commandServiceSession.createProducer(commandServiceTopic);
		commandProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		
		commandServiceTemporaryQueue = commandServiceSession.createTemporaryQueue();
		commandConsumer = commandServiceSession.createConsumer(commandServiceTemporaryQueue);
		
		commandConsumer.setMessageListener(this);
	}

}
