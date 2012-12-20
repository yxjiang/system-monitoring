package sysmon.manager;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sysmon.util.GlobalParameters;
import sysmon.util.Out;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Read the config file of manager.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public class ConfigReader {
	
	private static Document doc;
	
	/**
	 * Get the configuration about how to assign monitors to collectors.
	 * @return
	 */
	public static JsonObject getCollectorAssignConfig() {
		JsonObject assignConfigObj = new JsonObject();
		
		try {
			Document doc = readConfigFile();
			if(doc == null) {
				return null;
			}
			Element root = doc.getDocumentElement();
			NodeList nodeList = root.getElementsByTagName("monitorAssignStrategy");
			for(int i = 0; i < nodeList.getLength(); ++i) {
				Node strategyNode = nodeList.item(i);
				if(strategyNode instanceof Element) {
					Element strategyElement = (Element)strategyNode;
					String strategyType = strategyElement.getAttribute("type");
					assignConfigObj.addProperty("strategy", strategyType);
					break;
				}
			}
			return assignConfigObj;
		} catch (ParserConfigurationException e) {
			Out.println("When reading config file. " + e.getMessage());
		} catch (SAXException e) {
			Out.println("When reading config file. " + e.getMessage());
		} catch (IOException e) {
			Out.println("When reading config file. " + e.getMessage());
		} 
		
		return null;
	}
	
	/**
	 * Get the configuration about alert setting on collectors.
	 * @return
	 */
	public static JsonArray getAlertsConfig() {
		JsonArray alertsConfigArray = new JsonArray();
		
		try {
			Document doc = readConfigFile();
			if(doc == null) {
				return null;
			}
			Element root = doc.getDocumentElement();
			NodeList nodeList = root.getElementsByTagName("alert");
			for(int i = 0; i < nodeList.getLength(); ++i) {
				Node alertNode = nodeList.item(i);
				if(alertNode instanceof Element) {
					Element alertElement = (Element)alertNode;
					String alertType = alertElement.getAttribute("type");
					JsonObject alertJson = new JsonObject();
					alertJson.addProperty("type", alertType);
					NodeList parameters = alertElement.getElementsByTagName("parameter");
					JsonObject parameterObj = new JsonObject();
					for(int j = 0; j < parameters.getLength(); ++j) {
						JsonObject parameterJson = new JsonObject();
						Element paramElement = (Element)parameters.item(j);
						String parameterName = paramElement.getAttribute("name");
						String parameterValue = paramElement.getAttribute("value");
						parameterJson.addProperty("name", parameterName);
						parameterJson.addProperty("value", parameterValue);
						parameterObj.add(parameterName, parameterJson);
					}
					alertJson.add("parameters", parameterObj);
					alertsConfigArray.add(alertJson);
				}
			}
			return alertsConfigArray;
		} catch (ParserConfigurationException e) {
			Out.println("When reading config file. " + e.getMessage());
		} catch (SAXException e) {
			Out.println("When reading config file. " + e.getMessage());
		} catch (IOException e) {
			Out.println("When reading config file. " + e.getMessage());
		} 
		return null;
	}
	
	/**
	 * Read the xml file.
	 * @param filepath
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static Document readConfigFile() throws ParserConfigurationException, SAXException, IOException {
		if(doc != null) {
			return doc;
		}
		
		File fmlFile = new File(GlobalParameters.ALERT_CONFIG_FILE_PATH);
		if(fmlFile.exists() == false) {
			return null;
		}
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fmlFile);
		doc.getDocumentElement().normalize();
		return doc;
	}
	
}
