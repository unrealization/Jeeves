package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;

public interface MessageDeleteHandler
{
	/**
	 * React to a message delete event
	 * @param event The message delete event that needs handling
	 */
	public void messageDeleteHandler(MessageDeleteEvent event);
}
