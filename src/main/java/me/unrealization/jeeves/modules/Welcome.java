package me.unrealization.jeeves.modules;

import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import me.unrealization.jeeves.bot.Jeeves;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;

public class Welcome implements BotModule, UserJoinedHandler
{
	private String version = "1.0";
	private String[] commandList;
	private HashMap<String, Object> defaultConfig = new HashMap<String, Object>();

	public Welcome()
	{
		this.commandList = new String[2];
		this.commandList[0] = "GetWelcomeChannel";
		this.commandList[1] = "SetWelcomeChannel";
		this.defaultConfig.put("welcomeChannel", "");
	}

	@Override
	public HashMap<String, Object> getDefaultConfig()
	{
		return this.defaultConfig;
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
		return this.version;
	}

	@Override
	public String[] getCommands()
	{
		return this.commandList;
	}

	@Override
	public String getDiscordId()
	{
		return null;
	}

	@Override
	public boolean canDisable()
	{
		return true;
	}

	@Override
	public void userJoinedHandler(UserJoinEvent event)
	{
		System.out.println("User " + event.getUser().getName() + " has joined " + event.getGuild().getName());
		String channelId = (String)Jeeves.serverConfig.getValue(event.getGuild().getID(), "welcomeChannel");

		if (channelId.isEmpty() == true)
		{
			return;
		}

		IChannel channel = event.getGuild().getChannelByID(channelId);

		if (channel == null)
		{
			System.out.println("Invalid welcome channel.");
			Jeeves.serverConfig.setValue(event.getGuild().getID(), "welcomeChannel", "");

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

		Jeeves.sendMessage(channel, "Welcome to " + event.getGuild().getName() + ", " + event.getUser().mention() + "!");
	}

	public static class GetWelcomeChannel implements BotCommand
	{
		@Override
		public String getHelp()
		{
			// TODO Auto-generated method stub
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
		public boolean owner()
		{
			return false;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String welcomeChannelId = (String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "welcomeChannel");

			if (welcomeChannelId.isEmpty() == true)
			{
				Jeeves.sendMessage(message.getChannel(), "No welcome channel has been set.");
				return;
			}

			IChannel welcomeChannel = message.getGuild().getChannelByID(welcomeChannelId);

			if (welcomeChannel == null)
			{
				Jeeves.sendMessage(message.getChannel(), "A welcome channel has been set, but it does not exist.");

				Welcome welcome = new Welcome();
				Jeeves.serverConfig.setValue(message.getGuild().getID(), "welcomeChannel", welcome.getDefaultConfig().get("welcomeChannel"));

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

			Jeeves.sendMessage(message.getChannel(), "The welcome channel is: " + welcomeChannel.getName());
		}
	}

	public static class SetWelcomeChannel implements BotCommand
	{
		@Override
		public String getHelp()
		{
			// TODO Auto-generated method stub
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
		public boolean owner()
		{
			return false;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String channelName = String.join(" ", arguments).trim();
			IChannel welcomeChannel = null;

			if (channelName.isEmpty() == true)
			{
				Jeeves.serverConfig.setValue(message.getGuild().getID(), "welcomeChannel", "");
			}
			else
			{
				welcomeChannel = Jeeves.findChannel(message.getGuild(), channelName);

				if (welcomeChannel == null)
				{
					Jeeves.sendMessage(message.getChannel(), "Cannot find the channel " + channelName);
					return;
				}

				Jeeves.serverConfig.setValue(message.getGuild().getID(), "welcomeChannel", welcomeChannel.getID());
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

			if (welcomeChannel == null)
			{
				Jeeves.sendMessage(message.getChannel(), "The welcome channel has been cleared.");
			}
			else
			{
				Jeeves.sendMessage(message.getChannel(), "The welcome channel has been set to: " + welcomeChannel.getName());
			}
		}
	}
}
