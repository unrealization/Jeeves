package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.MessageDeleteEvent;

public interface MessageDeleteHandler
{
	public void messageDeleteHandler(MessageDeleteEvent event);
}
