package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.UserUpdateEvent;

public interface UserUpdateHandler
{
	/**
	 * React to a user update event
	 * @param serverId Since this event is not tied to a specific server the module needs to know what server to work on
	 * @param event The user update event that needs handling
	 */
	public void userUpdateHandler(String serverId, UserUpdateEvent event);
}
