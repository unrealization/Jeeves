package me.unrealization.jeeves.interfaces;

import discord4j.core.event.domain.message.MessageDeleteEvent;

public interface MessageDeleteHandler
{
	/**
	 * React to a message delete event
	 * @param event The message delete event that needs handling
	 */
	public void messageDeleteHandler(MessageDeleteEvent event);
}
