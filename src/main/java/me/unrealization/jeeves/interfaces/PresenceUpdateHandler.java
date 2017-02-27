package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.obj.IGuild;

public interface PresenceUpdateHandler
{
	/**
	 * React to a presence update event
	 * @param server Since this event is not tied to a specific server the server the module needs to work on needs to be provided
	 * @param event The presence update event that needs handling
	 */
	public void presenceUpdateHandler(IGuild serverId, PresenceUpdateEvent event);
}
