package interfaces;

import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;

public interface PresenceUpdateHandler
{
	public void presenceUpdateHandler(PresenceUpdateEvent event);
}
