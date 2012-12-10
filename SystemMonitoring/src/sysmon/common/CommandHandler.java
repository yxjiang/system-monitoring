package sysmon.common;

import javax.jms.Connection;
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

import com.google.gson.JsonParser;

import sysmon.util.IPUtil;

/**
 * Handle the commands.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public abstract class CommandHandler implements MessageListener{
	
	protected BrokerService broker;
	protected Session commandServiceSession;
	protected MessageProducer commandProducer;
	protected MessageConsumer commandConsumer;
	protected Topic commandServiceTopic;
	protected JsonParser jsonParser;
	
	public CommandHandler() {
		this.jsonParser = new JsonParser();
	}
	
	public void init() {
		try {
			initCommandService();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initialize the command service.
	 * @throws JMSException
	 */
	protected abstract void initCommandService() throws JMSException;
	
}