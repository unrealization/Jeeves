package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent;

public interface MessageUpdateHandler
{
	/**
	 * React to a message update event
	 * @param event The message update event that needs handling
	 */
	public void messageUpdateHandler(MessageUpdateEvent event);
}
