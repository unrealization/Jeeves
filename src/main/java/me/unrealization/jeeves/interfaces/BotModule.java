package me.unrealization.jeeves.interfaces;

import java.util.HashMap;

import me.unrealization.jeeves.bot.Jeeves;

public abstract class BotModule
{
	protected String version = "";
	protected String[] commandList;
	protected HashMap<String, Object> defaultConfig = new HashMap<String, Object>();

	/**
	 * Get the default configuration for the module
	 * @return A hashmap with the default module configuration, or null if no configuration is needed
	 */
	final public HashMap<String, Object> getDefaultConfig()
	{
		return this.defaultConfig;
	}

	/**
	 * Get information about the module
	 * @return A text compiled from the help information of the module's commands
	 */
	final public String getHelp()
	{
		String output = "";

		for (int commandIndex = 0; commandIndex < this.commandList.length; commandIndex++)
		{
			Class<?> commandClass;

			try
			{
				commandClass = Class.forName(this.getClass().getName() + "$" + this.commandList[commandIndex]);
			}
			catch (ClassNotFoundException e)
			{
				Jeeves.debugException(e);
				continue;
			}

			BotCommand command;

			try
			{
				command = (BotCommand)commandClass.newInstance();
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				Jeeves.debugException(e);
				continue;
			}

			output += "`" + command.getClass().getSimpleName();
			String parameters = command.getParameters();

			if ((parameters != null) && (parameters.isEmpty() == false))
			{
				output += " " + parameters;
			}

			output += "`\n";
			String help = command.getHelp();

			if ((help != null) && (help.isEmpty() == false))
			{
				output += "\t" + help + "\n";
			}

			output += "\n";
		}

		return output;
	}

	/**
	 * Get the module version
	 * @return The module version, obviously
	 */
	final public String getVersion()
	{
		return this.version;
	}

	/**
	 * Get the list of commands
	 * @return The list of commands available in this module
	 */
	final public String[] getCommands()
	{
		return this.commandList;
	}

	/**
	 * Get the id of the Discord server this module is limited to
	 * @return If the module is limited to a specific Discord server its id will be returned, otherwise this should return null
	 */
	public String getDiscordId()
	{
		return null;
	}

	/**
	 * Check if the module can be disabled
	 * @return True if the module can be disabled, false otherwise
	 */
	public boolean canDisable()
	{
		return true;
	}
}
