package me.unrealization.jeeves.modules;

import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.NewUserHandler;
import java.util.HashMap;

import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class Ccn implements BotModule, NewUserHandler
{
	private String version = "0.1";
	private String[] commandList;
	private HashMap<String, String> defaultConfig = new HashMap<String, String>();

	public Ccn()
	{
		this.commandList = new String[2];
		this.commandList[0] = "GetWelcomeChannel";
		this.commandList[1] = "SetWelcomeChannel";
		this.defaultConfig.put("ccnProximityRole", "");
		//this.defaultConfig.put("ccnEdsmUseBetaServer", "0");
		this.defaultConfig.put("ccnEdsmId", "");
		this.defaultConfig.put("ccnEdsmApiKey", "");
	}

	@Override
	public HashMap<String, String> getDefaultConfig()
	{
		return this.defaultConfig;
	}

	@Override
	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion()
	{
		return this.version;
	}

	@Override
	public String[] getCommands()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDiscordId()
	{
		return "209372315673165825";
	}

	@Override
	public boolean canDisable()
	{
		return true;
	}

	@Override
	public void newUserHandler(UserJoinEvent event)
	{
		String message = "Welcome to the Colonia Citizens Network, " + event.getUser().getName() + "\n\n";
		message += "In order to make the most out of your experience here we have set up a number of roles which you can assign to yourself, using our bot Jeeves in our **#bots** channel. These roles allow access to special channels dedicated to different topics, where you can meet players who share your interests.\n\n";
		message += "The bot commands ``roles``, ``join`` and ``leave`` will help you to find out which roles are currently available for you to use, and allow you to give yourself a role, or take it away again.\n\n";
		message += "Please note that all bot commands have to be prefixed by pinging the bot using ``@Jeeves``\n\n";
		message += "To query what roles are available, type:\n\t``@Jeeves roles``\n\n";
		message += "To assign the role **Exploration Wing Member**:\n\t``@Jeeves join Exploration Wing Member``\n\n";
		message += "To remove the role **Exploration Wing Member**:\n\t``@Jeeves leave Exploration Wing Member``\n\n";
		message += "Our bot can also do quite a few other things to help you. Feel free to ask him for help using ``@Jeeves help``\n\n";
		message += "Have a pleasant stay on the Colonia Citizens Network Discord!\n";
		message += "The CCN Team";

		IPrivateChannel pmChannel;

		try
		{
			pmChannel = event.getUser().getOrCreatePMChannel();
		}
		catch (RateLimitException | DiscordException e)
		{
			e.printStackTrace();
			return;
		}

		Jeeves.sendMessage(pmChannel, message);
	}
}
