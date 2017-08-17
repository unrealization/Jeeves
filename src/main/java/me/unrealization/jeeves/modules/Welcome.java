package me.unrealization.jeeves.modules;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.MessageQueue;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;

public class Welcome extends BotModule implements UserJoinedHandler
{
	public Welcome()
	{
		this.version = "1.0.1";

		this.commandList = new String[2];
		this.commandList[0] = "GetWelcomeChannel";
		this.commandList[1] = "SetWelcomeChannel";

		this.defaultConfig.put("welcomeChannel", "");
	}

	@Override
	public void userJoinedHandler(UserJoinEvent event)
	{
		String channelIdString = (String)Jeeves.serverConfig.getValue(event.getGuild().getLongID(), "welcomeChannel");

		if (channelIdString.isEmpty() == true)
		{
			return;
		}

		long channelId = Long.parseLong(channelIdString);
		IChannel channel = event.getGuild().getChannelByID(channelId);

		if (channel == null)
		{
			System.out.println("Invalid welcome channel.");
			Welcome welcome = new Welcome();
			Jeeves.serverConfig.setValue(event.getGuild().getLongID(), "welcomeChannel", welcome.getDefaultConfig().get("welcomeChannel"));

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

		MessageQueue.sendMessage(channel, "Welcome to " + event.getGuild().getName() + ", " + event.getUser().mention() + "!");
	}

	public static class GetWelcomeChannel extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Get the welcome channel.";
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
			String channelIdString = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "welcomeChannel");

			if (channelIdString.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "No welcome channel has been set.");
				return;
			}

			long channelId = Long.parseLong(channelIdString);
			IChannel channel = message.getGuild().getChannelByID(channelId);

			if (channel == null)
			{
				MessageQueue.sendMessage(message.getChannel(), "A welcome channel has been set, but it does not exist.");

				Welcome welcome = new Welcome();
				Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "welcomeChannel", welcome.getDefaultConfig().get("welcomeChannel"));

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

			MessageQueue.sendMessage(message.getChannel(), "The welcome channel is: " + channel.getName());
		}
	}

	public static class SetWelcomeChannel extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Set or clear the welcome channel.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "[channel]";
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
			String channelName = String.join(" ", arguments).trim();
			IChannel channel = null;

			if (channelName.isEmpty() == true)
			{
				Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "welcomeChannel", "");
			}
			else
			{
				channel = Jeeves.findChannel(message.getGuild(), channelName);

				if (channel == null)
				{
					MessageQueue.sendMessage(message.getChannel(), "Cannot find the channel " + channelName);
					return;
				}

				String channelIdString = Long.toString(channel.getLongID());
				Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "welcomeChannel", channelIdString);
			}

			try
			{
				Jeeves.serverConfig.saveConfig();
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "Cannot store the setting.");
				return;
			}

			if (channel == null)
			{
				MessageQueue.sendMessage(message.getChannel(), "The welcome channel has been cleared.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel(), "The welcome channel has been set to: " + channel.getName());
			}
		}
	}
}
