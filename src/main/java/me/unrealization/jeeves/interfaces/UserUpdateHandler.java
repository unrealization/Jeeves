package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.UserUpdateEvent;
import sx.blah.discord.handle.obj.IGuild;

public interface UserUpdateHandler
{
	/**
	 * React to a user update event
	 * @param server Since this event is not tied to a specific server the server the module needs to work on needs to be provided
	 * @param event The user update event that needs handling
	 */
	public void userUpdateHandler(IGuild serverId, UserUpdateEvent event);
}
