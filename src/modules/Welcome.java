package modules;

import java.util.HashMap;

import sx.blah.discord.handle.impl.events.UserJoinEvent;
import interfaces.BotModule;
import interfaces.NewUserHandler;

public class Welcome implements BotModule, NewUserHandler
{
	@Override
	public HashMap<String, String> getDefaultConfig()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHelp()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getCommands()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void newUserHandler(UserJoinEvent event)
	{
		System.out.println("User " + event.getUser().getName() + " has joined " + event.getGuild().getName());
		//return false;
	}
}
