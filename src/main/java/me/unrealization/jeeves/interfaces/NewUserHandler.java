package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.UserJoinEvent;

public interface NewUserHandler
{
	/**
	 * React to a new user joining a Discord server
	 * @param event The new user event that needs handling
	 */
	public void newUserHandler(UserJoinEvent event);
}
