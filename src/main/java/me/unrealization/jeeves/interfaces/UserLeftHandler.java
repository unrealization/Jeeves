package me.unrealization.jeeves.interfaces;

import discord4j.core.event.domain.guild.MemberLeaveEvent;

public interface UserLeftHandler
{
	/**
	 * React to a user leaving a Discord server
	 * @param event The user leave event that needs handling
	 */
	public void userLeftHandler(MemberLeaveEvent event);
}
