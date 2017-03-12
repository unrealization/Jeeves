package me.unrealization.jeeves.dataLists;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.interfaces.BotConfig;

public class EdsmUserList implements BotConfig
{
	private HashMap<String, Object> config = new HashMap<String, Object>();

	public EdsmUserList() throws ParserConfigurationException, SAXException
	{
		this.loadConfig();
	}

	@Override
	public void loadConfig(String fileName) throws ParserConfigurationException, SAXException
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setIgnoringComments(true);
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		File xmlFile = new File(fileName);
		Document doc;

		try
		{
			doc = docBuilder.parse(xmlFile);
		}
		catch (IOException e)
		{
			Jeeves.debugException(e);
			this.config = new HashMap<String, Object>();
			return;
		}

		NodeList users = doc.getElementsByTagName("user");

		for (int userIndex = 0; userIndex < users.getLength(); userIndex++)
		{
			Element user = (Element)users.item(userIndex);
			String userId = user.getAttribute("userId");
			NodeList itemChildNodes = user.getChildNodes();
			Object configValue;

			if (itemChildNodes.getLength() == 0)
			{
				configValue = "";
			}
			else if (itemChildNodes.getLength() == 1)
			{
				configValue = user.getTextContent().trim();
			}
			else
			{
				List<String> tmpList = new ArrayList<String>();

				for (int itemIndex = 0; itemIndex < itemChildNodes.getLength(); itemIndex++)
				{
					Node itemNode = itemChildNodes.item(itemIndex);

					if (itemNode.getNodeType() != Node.ELEMENT_NODE)
					{
						continue;
					}

					tmpList.add(itemNode.getTextContent().trim());
				}

				configValue = tmpList;
			}

			this.config.put(userId, configValue);
		}
	}

	public void loadConfig() throws ParserConfigurationException, SAXException
	{
		this.loadConfig("edsmUserList.xml");
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
			Element user = doc.createElement("user");
			Attr userId = doc.createAttribute("userId");
			userId.setValue(configKeyList[keyIndex]);
			user.setAttributeNode(userId);
			Object configItem = this.config.get(configKeyList[keyIndex]);

			if (configItem.getClass() == String.class)
			{
				user.setTextContent((String)configItem);
			}
			else
			{
				List<String> configItemList = Jeeves.listToStringList((List<?>)configItem);

				for (int itemIndex = 0; itemIndex < configItemList.size(); itemIndex++)
				{
					Element item = doc.createElement("entry");
					item.setTextContent((String)configItemList.get(itemIndex));
					user.appendChild(item);
				}
			}

			docRoot.appendChild(user);
		}

		DOMSource domSource = new DOMSource(doc);
		File xmlFile = new File(fileName);
		StreamResult result = new StreamResult(xmlFile);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(domSource, result);
	}

	public void saveConfig() throws ParserConfigurationException, TransformerException
	{
		this.saveConfig("edsmUserList.xml");
	}

	@Override
	public Object getValue(String serverId, String key)
	{
		return this.getValue(key);
	}

	public Object getValue(String key)
	{
		if (this.config.containsKey(key) == false)
		{
			return "";
		}

		Object value = this.config.get(key);
		return value;
	}

	@Override
	public void setValue(String serverId, String key, Object value)
	{
		this.setValue(key, value);
	}

	public void setValue(String key, Object value)
	{
		this.config.put(key, value);
	}

	@Override
	public void removeValue(String serverId, String key)
	{
		this.removeValue(key);
	}

	public void removeValue(String key)
	{
		this.config.remove(key);
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
