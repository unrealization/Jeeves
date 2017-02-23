package interfaces;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public interface BotConfig
{
	public void loadConfig(String fileName) throws ParserConfigurationException, SAXException, IOException;
	public void saveConfig(String fileName) throws ParserConfigurationException, TransformerException;
	public String getValue(String serverId, String key);
	public void setValue(String serverId, String key, String value);
	public String[] getKeyList(String serverId);
	public boolean hasKey(String serverId, String key);
}
