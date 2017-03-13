package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.MessageUpdateEvent;

public interface MessageUpdateHandler
{
	public void messageUpdateHandler(MessageUpdateEvent event);
}
