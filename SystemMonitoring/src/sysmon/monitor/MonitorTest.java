package sysmon.monitor;

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

public class MonitorTest {

  private String brokerUrl = "tcp://192.168.0.100:32100";

  private Session session;
  private String requestQueue;
  private MessageProducer producer;
  private BrokerService broker;

  private static int count = 0;

  public MonitorTest() {
    this.requestQueue = "requestQueue";
    try {
      createBroker();
      init();
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
      broker.addConnector(brokerUrl);
      broker.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void init() throws JMSException {
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
        brokerUrl);
    Connection connection = connectionFactory.createConnection();
    connection.start();

    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

    Topic topic = session.createTopic("metaData");
    producer = session.createProducer(topic);
    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
  }

  public void sendMonitoredData() throws Exception {

    TextMessage message = session.createTextMessage();
    message.setText("Test " + count++);

    String correlateionID = UUID.randomUUID().toString();
    message.setJMSCorrelationID(correlateionID);
    this.producer.send(message);
    // System.out.println("Send " + message.getText());
  }

  public void run() {
    int i = 0;
    while (true) {
      try {
        sendMonitoredData();
        Thread.sleep(1000);
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

  public static void main(String[] args) {
    MonitorTest m = new MonitorTest();
    m.run();
  }

}
