package me.unrealization.jeeves.interfaces;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public interface BotConfig
{
	/**
	 * Load the configuration
	 * @param fileName The XML file to load
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void loadConfig(String fileName) throws ParserConfigurationException, SAXException, IOException;

	/**
	 * Save the configuration
	 * @param fileName The file to save the configuration to
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public void saveConfig(String fileName) throws ParserConfigurationException, TransformerException;

	/**
	 * Get a configuration value for a specific Discord server
	 * @param serverId The id of the Discord server
	 * @param key The configuration key to query
	 * @return A string or array of strings containing the configuration values
	 */
	public Object getValue(String serverId, String key);

	/**
	 * Set a configuration value for a specific Discord server
	 * @param serverId The id of the Discord server
	 * @param key The configuration key to set
	 * @param value The value 
	 */
	public void setValue(String serverId, String key, Object value);

	/**
	 * Get the list of available configuration keys for the given Discord server
	 * @param serverId The id of the Discord server
	 * @return A list of key names for the server configuration
	 */
	public String[] getKeyList(String serverId);

	/**
	 * Check if a given configuration key is available on a specific Discord server
	 * @param serverId The id of the Discord server
	 * @param key The key to check for
	 * @return True if the key is available, false if it is not
	 */
	public boolean hasKey(String serverId, String key);
}
