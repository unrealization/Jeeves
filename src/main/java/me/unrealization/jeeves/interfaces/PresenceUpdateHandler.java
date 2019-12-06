package me.unrealization.jeeves.interfaces;

import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.object.entity.Guild;

public interface PresenceUpdateHandler
{
	/**
	 * React to a presence update event
	 * @param server Since this event is not tied to a specific server the server the module needs to work on needs to be provided
	 * @param event The presence update event that needs handling
	 */
	public void presenceUpdateHandler(Guild server, PresenceUpdateEvent event);
}
