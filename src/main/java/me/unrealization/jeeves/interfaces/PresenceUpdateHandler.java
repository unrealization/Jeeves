package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;

public interface PresenceUpdateHandler
{
	/**
	 * React to a presence update event
	 * @param serverId Since this event is not tied to a specific server the module needs to know what server to work on
	 * @param event The presence update event that needs handling
	 */
	public void presenceUpdateHandler(String serverId, PresenceUpdateEvent event);
}
