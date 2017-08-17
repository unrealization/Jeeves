package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent;

public interface MessageUpdateHandler
{
	public void messageUpdateHandler(MessageUpdateEvent event);
}
