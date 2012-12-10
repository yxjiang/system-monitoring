package sysmon.manager;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import sysmon.common.PassiveCommandHandler;
import sysmon.util.GlobalParameters;
import sysmon.util.Out;

import com.google.gson.JsonObject;


/**
 * MonitoringManager is in charge of the overall management of the monitoring.
 * It handles the registration of monitors and collectors, 
 * route the data transferring, and conduct fault tolerant tasks. 
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public class MonitoringManager {
	
	private static MonitoringManager instance;
	
	private ManagerPassiveCommandHandler passiveCommandHandler;
	
	/**
	 * Get the singleton of monitoring manager.
	 * @return
	 */
	public static MonitoringManager getInstance() {
		if(instance == null) 
			return new MonitoringManager();
		else
			return instance;
	}
	
	private MonitoringManager() {
		this.passiveCommandHandler = new ManagerPassiveCommandHandler(GlobalParameters.MANAGER_SERVICE_PORT);
		this.passiveCommandHandler.init();
	}
	
	class ManagerPassiveCommandHandler extends PassiveCommandHandler {
		
		public ManagerPassiveCommandHandler(String servicePort) {
			super(servicePort);
			Out.println("Start command service at " + this.brokerAddress);
		}

		@Override
		public void onMessage(Message commandMessage) {
			if(commandMessage instanceof TextMessage) {
				String commandJson;
				try {
					commandJson = ((TextMessage) commandMessage).getText();
					JsonObject jsonObj = (JsonObject)jsonParser.parse(commandJson);
					Out.println(commandJson);
					if(jsonObj.get("type").getAsString().equals("registration")) {
						String monitorName = jsonObj.get("machine-name").getAsString();
						Out.println(monitorName + " registered.");
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
