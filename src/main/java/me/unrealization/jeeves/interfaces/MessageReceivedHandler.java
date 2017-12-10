package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.obj.IMessage;

public interface MessageReceivedHandler
{
	/**
	 * Handle an incoming message before it gets passed to the regular message handling.
	 * @param message The received message
	 * @return Whether or not the message has been handled.
	 */
	public boolean messageReceivedHandler(IMessage message);
}
