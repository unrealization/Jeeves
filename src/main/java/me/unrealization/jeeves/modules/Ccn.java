package me.unrealization.jeeves.modules;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class Ccn extends BotModule implements UserJoinedHandler
{
	public Ccn()
	{
		this.version = "0.3";

		this.commandList = new String[2];
		this.commandList[0] = "GetCcnProximityRole";
		this.commandList[1] = "SetCcnProximityRole";

		this.defaultConfig.put("ccnProximityRole", "");
		//this.defaultConfig.put("ccnEdsmUseBetaServer", "0");
		this.defaultConfig.put("ccnEdsmId", "");
		this.defaultConfig.put("ccnEdsmApiKey", "");
	}

	@Override
	public String getDiscordId()
	{
		return "209372315673165825";
	}

	@Override
	public void userJoinedHandler(UserJoinEvent event)
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
			Jeeves.debugException(e);
			return;
		}

		Jeeves.sendMessage(pmChannel, message);
	}

	public static class GetCcnProximityRole extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Get the current CCN proximity role.";
			return output;
		}

		@Override
		public String getParameters()
		{
			return null;
		}

		@Override
		public Permissions[] permissions()
		{
			Permissions[] permissionList = new Permissions[1];
			permissionList[0] = Permissions.MANAGE_SERVER;
			return permissionList;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String roleId = (String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "ccnProximityRole");

			if (roleId.isEmpty() == true)
			{
				Jeeves.sendMessage(message.getChannel(), "No CCN proximity role has been set.");
				return;
			}

			IRole role = message.getGuild().getRoleByID(roleId);

			if (role == null)
			{
				Jeeves.sendMessage(message.getChannel(), "A CCN proximity role has been set, but it does not exist.");

				Ccn ccn = new Ccn();
				Jeeves.serverConfig.setValue(message.getGuild().getID(), "ccnProximityRole", ccn.getDefaultConfig().get("ccnProximityRole"));

				try
				{
					Jeeves.serverConfig.saveConfig();
				}
				catch (ParserConfigurationException | TransformerException e)
				{
					Jeeves.debugException(e);
				}

				return;
			}

			Jeeves.sendMessage(message.getChannel(), "The CCN proximity role is: " + role.getName());
		}
	}

	public static class SetCcnProximityRole extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Set or clear the CCN proximity role.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "[role]";
			return output;
		}

		@Override
		public Permissions[] permissions()
		{
			Permissions[] permissionList = new Permissions[1];
			permissionList[0] = Permissions.MANAGE_SERVER;
			return permissionList;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String roleName = String.join(" ", arguments).trim();
			IRole role = null;

			if (roleName.isEmpty() == true)
			{
				Jeeves.serverConfig.setValue(message.getGuild().getID(), "ccnProximityRole", "");
			}
			else
			{
				role = Jeeves.findRole(message.getGuild(), roleName);

				if (role == null)
				{
					Jeeves.sendMessage(message.getChannel(), "Cannot find the role " + roleName);
					return;
				}

				Jeeves.serverConfig.setValue(message.getGuild().getID(), "ccnProximityRole", role.getID());
			}

			try
			{
				Jeeves.serverConfig.saveConfig();
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				Jeeves.debugException(e);
				Jeeves.sendMessage(message.getChannel(), "Cannot store the setting.");
				return;
			}

			if (role == null)
			{
				Jeeves.sendMessage(message.getChannel(), "The CCN proximity role has been cleared.");
			}
			else
			{
				Jeeves.sendMessage(message.getChannel(), "The CCN proximity role has been set to: " + role.getName());
			}
		}
	}
}
