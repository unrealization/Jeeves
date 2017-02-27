package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

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
	public Permissions[] permissions()
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
	 * @param message The message received
	 * @param arguments The list of parameters
	 */
	public abstract void execute(IMessage message, String[] arguments);
}
