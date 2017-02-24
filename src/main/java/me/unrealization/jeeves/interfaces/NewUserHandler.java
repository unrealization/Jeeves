package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.UserJoinEvent;

public interface NewUserHandler
{
	public void newUserHandler(UserJoinEvent event);
}
