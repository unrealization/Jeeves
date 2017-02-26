package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.UserJoinEvent;

public interface UserJoinedHandler
{
	/**
	 * React to a new user joining a Discord server
	 * @param event The user join event that needs handling
	 */
	public void userJoinedHandler(UserJoinEvent event);
}
