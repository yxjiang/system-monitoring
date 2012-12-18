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
					JsonArray parameterArray = new JsonArray();
					for(int j = 0; j < parameters.getLength(); ++j) {
						JsonObject parameterJson = new JsonObject();
						Element paramElement = (Element)parameters.item(j);
						String parameterName = paramElement.getAttribute("name");
						String parameterValue = paramElement.getAttribute("value");
						parameterJson.addProperty("name", parameterName);
						parameterJson.addProperty("value", parameterValue);
						parameterArray.add(parameterJson);
					}
					alertJson.add("parameters", parameterArray);
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
