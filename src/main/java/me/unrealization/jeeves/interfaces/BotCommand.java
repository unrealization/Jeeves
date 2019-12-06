package me.unrealization.jeeves.interfaces;

import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Permission;

public abstract class BotCommand
{
	/**
	 * Get information about the command
	 * @return A short text on what the command does
	 */
	public abstract String getHelp();

	/**
	 * Get information about the parameters the command can handle
	 * @return A short info on the parameters the command accepts
	 */
	public abstract String getParameters();

	/**
	 * Get the permissions required to run the command
	 * @return The list of permissions required to be allowed to run the command, or null if no permissions are needed
	 */
	public Permission[] permissions()
	{
		return null;
	}

	/**
	 * Check if the command can only be run by the bot owner
	 * @return True if the command can only be run by the bot owner, otherwise false
	 */
	public boolean owner()
	{
		return false;
	}

	/**
	 * Execute the command
	 * @param message The message received from Discord
	 * @param argumentString The argument string for the command
	 */
	public abstract void execute(Message message, String argumentString);
}
