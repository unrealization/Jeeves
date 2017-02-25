package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.UserUpdateEvent;

public interface UserUpdateHandler
{
	/**
	 * React to a user update event
	 * @param event The user update event that needs handling
	 */
	public void userUpdateHandler(UserUpdateEvent event);
}
