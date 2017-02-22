package interfaces;

import sx.blah.discord.handle.impl.events.UserUpdateEvent;

public interface UserUpdateHandler
{
	public void userUpdateHandler(UserUpdateEvent event);
}
