package me.unrealization.jeeves.bot;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import me.unrealization.jeeves.interfaces.BotConfig;

public class ClientConfig implements BotConfig
{
	private HashMap<String, String> config = new HashMap<String, String>();

	public ClientConfig() throws ParserConfigurationException, SAXException, IOException
	{
		this.loadConfig();
	}

	@Override
	public void loadConfig(String fileName) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setIgnoringComments(true);
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		File xmlFile = new File(fileName);
		Document doc = docBuilder.parse(xmlFile);

		Element docRoot = (Element)doc.getElementsByTagName("config").item(0);
		NodeList clientConfigList = docRoot.getChildNodes();

		for (int configIndex = 0; configIndex < clientConfigList.getLength(); configIndex++)
		{
			Node configNode = clientConfigList.item(configIndex);

			if (configNode.getNodeType() != Node.ELEMENT_NODE)
			{
				continue;
			}

			Element configItem = (Element)configNode;
			String configName = configItem.getNodeName();
			String configValue = configItem.getTextContent();
			this.config.put(configName, configValue);
		}
	}

	public void loadConfig() throws ParserConfigurationException, SAXException, IOException
	{
		this.loadConfig("clientConfig.xml");
	}

	@Override
	public void saveConfig(String fileName) throws ParserConfigurationException, TransformerException
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		Element docRoot = doc.createElement("config");
		doc.appendChild(docRoot);

		Set<String> configKeySet = this.config.keySet();
		String[] configKeyList = configKeySet.toArray(new String[configKeySet.size()]);

		for (int keyIndex = 0; keyIndex < configKeyList.length; keyIndex++)
		{
			Element setting = doc.createElement(configKeyList[keyIndex]);
			setting.setTextContent(this.config.get(configKeyList[keyIndex]));
			docRoot.appendChild(setting);
		}

		DOMSource domSource = new DOMSource(doc);
		File xmlFile = new File(fileName);
		StreamResult result = new StreamResult(xmlFile);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(domSource, result);

		/*result = new StreamResult(System.out);
		transformer.transform(domSource, result);*/
	}

	public void saveConfig() throws ParserConfigurationException, TransformerException
	{
		this.saveConfig("clientConfig.xml");
	}

	@Override
	public String getValue(String serverId, String key)
	{
		return this.getValue(key);
	}

	public String getValue(String key)
	{
		if (this.config.containsKey(key) == false)
		{
			return "";
			//throw new Exception("Unknown key " + key);
		}

		String value = this.config.get(key);
		return value;
	}

	@Override
	public void setValue(String serverId, String key, String value)
	{
		this.setValue(key, value);
	}

	public void setValue(String key, String value)
	{
		this.config.put(key, value);
	}

	@Override
	public String[] getKeyList(String serverId)
	{
		return this.getKeyList();
	}

	public String[] getKeyList()
	{
		Set<String> keySet = this.config.keySet();
		String[] keyList = keySet.toArray(new String[keySet.size()]);
		return keyList;
	}

	@Override
	public boolean hasKey(String serverId, String key)
	{
		return this.hasKey(key);
	}

	public boolean hasKey(String key)
	{
		String[] keyList = this.getKeyList();

		for (int keyIndex = 0; keyIndex < keyList.length; keyIndex++)
		{
			if (keyList[keyIndex].equals(key))
			{
				return true;
			}
		}

		return false;
	}
}
