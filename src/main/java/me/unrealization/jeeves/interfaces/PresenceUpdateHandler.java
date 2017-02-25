package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;

public interface PresenceUpdateHandler
{
	/**
	 * React to a presence update event
	 * @param event The presence update event that needs handling
	 */
	public void presenceUpdateHandler(PresenceUpdateEvent event);
}
