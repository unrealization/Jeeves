package me.unrealization.jeeves.interfaces;

import discord4j.core.event.domain.message.MessageUpdateEvent;

public interface MessageUpdateHandler
{
	/**
	 * React to a message update event
	 * @param event The message update event that needs handling
	 */
	public void messageUpdateHandler(MessageUpdateEvent event);
}
