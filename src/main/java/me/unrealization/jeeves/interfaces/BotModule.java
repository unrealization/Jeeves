package me.unrealization.jeeves.interfaces;

import java.util.HashMap;

public interface BotModule
{
	/**
	 * Get the default configuration for the module
	 * @return A hashmap with the default module configuration, or null if no configuration is needed
	 */
	public HashMap<String, Object> getDefaultConfig();
	/**
	 * Get information about the module
	 * @return A text compiled from the help information of the module's commands
	 */
	public String getHelp();
	/**
	 * Get the module version
	 * @return The module version, obviously
	 */
	public String getVersion();
	/**
	 * Get the list of commands
	 * @return The list of commands available in this module
	 */
	public String[] getCommands();
	/**
	 * Get the id of the Discord server this module is limited to
	 * @return If the module is limited to a specific Discord server its id will be returned, otherwise this should return null
	 */
	public String getDiscordId();
	/**
	 * Check if the module can be disabled
	 * @return True if the module can be disabled, false otherwise
	 */
	public boolean canDisable();
}
