package me.unrealization.jeeves.interfaces;

import discord4j.core.event.domain.guild.MemberJoinEvent;

public interface UserJoinedHandler
{
	/**
	 * React to a new user joining a Discord server
	 * @param event The user join event that needs handling
	 */
	public void userJoinedHandler(MemberJoinEvent event);
}
