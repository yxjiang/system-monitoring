package sysmon.subscriber;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import sysmon.common.metadata.MachineMetadata;
import sysmon.util.GlobalParameters;
import sysmon.util.IPUtil;

import com.google.gson.JsonObject;

/**
 * Subscribe the message from given collector.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public class TestSubscriber implements MessageListener{
	
	private static long count;
	private MessageConsumer consumer;
	
	public TestSubscriber(String gatheredIP, List<String> collectorIPList) {
		try {
			init(gatheredIP, collectorIPList);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void init(String gatheredIP, List<String> collectorIPList) throws JMSException {
		Subscriber sub = new Subscriber(collectorIPList);
		
		String brokerAddress = "tcp://" + gatheredIP + ":" + GlobalParameters.SUBSCRIBE_COMMAND_PORT;
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerAddress);
		Connection connection = connectionFactory.createConnection();
		connection.start();
		System.out.println("Connect to " + brokerAddress);
		
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic topic = session.createTopic("command");
		consumer = session.createConsumer(topic);
		consumer.setMessageListener(this);
	}
	
	@Override
	public void onMessage(Message message) {
		if(message instanceof ObjectMessage) {
			ObjectMessage objectMessage = (ObjectMessage)message;
			try {
				MachineMetadata machineMetadata = (MachineMetadata)objectMessage.getObject();
				JsonObject jsonObj = machineMetadata.getJson();
				String ip = jsonObj.get("machineIP").getAsString();
				System.out.println("[" + new Date() + "] receive new message from [" + ip + "].");
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
//		String collectorIP = "131.94.130.167";
		String collectorIP = IPUtil.getFirstAvailableIP();
		List<String> list = new ArrayList<String>();
		list.add("tcp://131.94.130.167:" + GlobalParameters.COLLECTOR_COMMAND_PORT);
		list.add("tcp://192.168.0.108:" + GlobalParameters.COLLECTOR_COMMAND_PORT);
		TestSubscriber subTest = new TestSubscriber(collectorIP, list);
		
	}
	
}
