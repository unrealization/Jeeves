package me.unrealization.jeeves.interfaces;

import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.object.entity.Guild;

public interface UserUpdateHandler
{
	/**
	 * React to a user update event
	 * @param server Since this event is not tied to a specific server the server the module needs to work on needs to be provided
	 * @param event The user update event that needs handling
	 */
	public void userUpdateHandler(Guild server, MemberUpdateEvent event);
}
