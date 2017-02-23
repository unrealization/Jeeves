package bot;

import interfaces.BotConfig;

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

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sx.blah.discord.handle.obj.IGuild;

public class ServerConfig implements BotConfig
{
	private HashMap<String, HashMap<String, String>> config = new HashMap<String, HashMap<String, String>>();

	public ServerConfig() throws ParserConfigurationException, SAXException, IOException
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

		NodeList servers = doc.getElementsByTagName("server");

		for (int serverIndex = 0; serverIndex < servers.getLength(); serverIndex++)
		{
			Element server = (Element)servers.item(serverIndex);
			NodeList serverConfigList = server.getChildNodes();
			String serverId = server.getAttribute("serverId");
			HashMap<String, String> config = new HashMap<String, String>();

			for (int configIndex = 0; configIndex < serverConfigList.getLength(); configIndex++)
			{
				Node configNode = serverConfigList.item(configIndex);

				if (configNode.getNodeType() != Node.ELEMENT_NODE)
				{
					continue;
				}

				Element configItem = (Element)configNode;
				String configName = configItem.getNodeName();
				String configValue = configItem.getTextContent();
				config.put(configName, configValue);
			}

			this.config.put(serverId, config);
		}
	}

	public void loadConfig() throws ParserConfigurationException, SAXException, IOException
	{
		this.loadConfig("serverConfig.xml");
	}

	@Override
	public void saveConfig(String fileName) throws ParserConfigurationException, TransformerException
	{
		Set<String> serverIdSet = this.config.keySet();
		String[] serverIdList = serverIdSet.toArray(new String[serverIdSet.size()]);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		Element docRoot = doc.createElement("config");
		doc.appendChild(docRoot);

		for (int serverIndex = 0; serverIndex < serverIdList.length; serverIndex++)
		{
			HashMap<String, String> serverConfig = this.config.get(serverIdList[serverIndex]);

			Element server = doc.createElement("server");
			Attr serverId = doc.createAttribute("serverId");
			serverId.setValue(serverIdList[serverIndex]);
			server.setAttributeNode(serverId);
			docRoot.appendChild(server);

			if (Jeeves.bot != null)
			{
				IGuild guild = Jeeves.bot.getGuildByID(serverIdList[serverIndex]);
				Comment serverName = doc.createComment(guild.getName());
				server.appendChild(serverName);
			}

			Set<String> configKeySet = serverConfig.keySet();
			String[] configKeyList = configKeySet.toArray(new String[configKeySet.size()]);

			for (int keyIndex = 0; keyIndex < configKeyList.length; keyIndex++)
			{
				Element setting = doc.createElement(configKeyList[keyIndex]);
				setting.setTextContent(serverConfig.get(configKeyList[keyIndex]));
				server.appendChild(setting);
			}
		}

		DOMSource domSource = new DOMSource(doc);
		File xmlFile = new File(fileName);
		StreamResult result = new StreamResult(xmlFile);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(domSource, result);

		result = new StreamResult(System.out);
		transformer.transform(domSource, result);
	}

	public void saveConfig() throws ParserConfigurationException, TransformerException
	{
		this.saveConfig("serverConfig.xml");
	}

	@Override
	public String getValue(String serverId, String key)
	{
		if (this.config.containsKey(serverId) == false)
		{
			return "";
		}

		HashMap<String, String> serverConfig = this.config.get(serverId);
		if (serverConfig.containsKey(key) == false)
		{
			return "";
		}

		String value = serverConfig.get(key);
		return value;
	}

	@Override
	public void setValue(String serverId, String key, String value)
	{
		HashMap<String, String> serverConfig;

		if (this.config.containsKey(serverId) == false)
		{
			serverConfig = new HashMap<String, String>();
		}
		else
		{
			serverConfig = this.config.get(serverId);
		}

		serverConfig.put(key, value);
		this.config.put(serverId, serverConfig);
	}


	@Override
	public String[] getKeyList(String serverId)
	{
		Set<String> keySet = this.config.get(serverId).keySet();
		String[] keyList = keySet.toArray(new String[keySet.size()]);
		return keyList;
	}

	@Override
	public boolean hasKey(String serverId, String key)
	{
		String[] keyList = this.getKeyList(serverId);

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
