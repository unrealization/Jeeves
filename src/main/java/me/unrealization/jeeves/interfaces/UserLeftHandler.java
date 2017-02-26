package me.unrealization.jeeves.interfaces;

import sx.blah.discord.handle.impl.events.UserLeaveEvent;

public interface UserLeftHandler
{
	/**
	 * React to a user leaving a Discord server
	 * @param event The user leave event that needs handling
	 */
	public void userLeftHandler(UserLeaveEvent event);
}
