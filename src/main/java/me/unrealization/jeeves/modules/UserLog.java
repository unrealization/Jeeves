package me.unrealization.jeeves.modules;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;
import sx.blah.discord.handle.impl.events.user.UserUpdateEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IPresence;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.handle.obj.StatusType;
import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.MessageQueue;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.PresenceUpdateHandler;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;
import me.unrealization.jeeves.interfaces.UserLeftHandler;
import me.unrealization.jeeves.interfaces.UserUpdateHandler;

public class UserLog extends BotModule implements UserJoinedHandler, UserLeftHandler, PresenceUpdateHandler, UserUpdateHandler
{
	public UserLog()
	{
		this.version = "0.9.7";

		this.commandList = new String[2];
		this.commandList[0] = "GetUserLogChannel";
		this.commandList[1] = "SetUserLogChannel";

		this.defaultConfig.put("userLogChannel", "");
	}

	@Override
	public void userJoinedHandler(UserJoinEvent event)
	{
		String channelIdString = (String)Jeeves.serverConfig.getValue(event.getGuild().getLongID(), "userLogChannel");

		if (channelIdString.isEmpty() == true)
		{
			return;
		}

		long channelId = Long.parseLong(channelIdString);
		IChannel channel = event.getGuild().getChannelByID(channelId);

		if (channel == null)
		{
			System.out.println("Invalid user log channel.");
			UserLog userLog = new UserLog();
			Jeeves.serverConfig.setValue(event.getGuild().getLongID(), "userLogChannel", userLog.getDefaultConfig().get("userLogChannel"));

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

		MessageQueue.sendMessage(channel, Jeeves.getUtcTime() + ": " + event.getUser().getName() + " has joined the server.");
	}

	@Override
	public void userLeftHandler(UserLeaveEvent event)
	{
		String channelIdString = (String)Jeeves.serverConfig.getValue(event.getGuild().getLongID(), "userLogChannel");

		if (channelIdString.isEmpty() == true)
		{
			return;
		}

		long channelId = Long.parseLong(channelIdString);
		IChannel channel = event.getGuild().getChannelByID(channelId);

		if (channel == null)
		{
			System.out.println("Invalid user log channel.");
			Jeeves.serverConfig.setValue(event.getGuild().getLongID(), "userLogChannel", "");

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

		MessageQueue.sendMessage(channel, Jeeves.getUtcTime() + ": " + event.getUser().getName() + " has left the server.");
	}

	@Override
	public void presenceUpdateHandler(IGuild server, PresenceUpdateEvent event)
	{
		String channelIdString = (String)Jeeves.serverConfig.getValue(server.getLongID(), "userLogChannel");

		if (channelIdString.isEmpty() == true)
		{
			return;
		}

		long channelId = Long.parseLong(channelIdString);
		IChannel channel = server.getChannelByID(channelId);

		if (channel == null)
		{
			System.out.println("Invalid user log channel.");
			Jeeves.serverConfig.setValue(server.getLongID(), "userLogChannel", "");

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

		IPresence oldPresence = event.getOldPresence();
		IPresence newPresence = event.getNewPresence();

		if (newPresence.equals(oldPresence) == true)
		{
			return;
		}

		if ((oldPresence.getStatus().equals(StatusType.ONLINE) == true) && ((newPresence.getStatus().equals(StatusType.IDLE) == true) || (newPresence.getStatus().equals(StatusType.DND) == true)))
		{
			return;
		}

		if ((newPresence.getStatus().equals(StatusType.ONLINE) == true) && ((oldPresence.getStatus().equals(StatusType.IDLE) == true) || (oldPresence.getStatus().equals(StatusType.DND) == true)))
		{
			return;
		}

		MessageQueue.sendMessage(channel, Jeeves.getUtcTime() + ": " + event.getUser().getName() + "'s status has changed from " + oldPresence.getStatus().name() + " to " + newPresence.getStatus().name());
	}

	@Override
	public void userUpdateHandler(IGuild server, UserUpdateEvent event)
	{
		// TODO Auto-generated method stub
		return;
	}

	public static class GetUserLogChannel extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Get the user log channel.";
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
		public void execute(IMessage message, String argumentString)
		{
			String channelIdString = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "userLogChannel");

			if (channelIdString.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "No user log channel has been set.");
				return;
			}

			long channelId = Long.parseLong(channelIdString);
			IChannel channel = message.getGuild().getChannelByID(channelId);

			if (channel == null)
			{
				MessageQueue.sendMessage(message.getChannel(), "A user log channel has been set, but it does not exist.");

				UserLog userLog = new UserLog();
				Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "userLogChannel", userLog.getDefaultConfig().get("userLogChannel"));

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

			MessageQueue.sendMessage(message.getChannel(), "The user log channel is: " + channel.getName());
		}
	}

	public static class SetUserLogChannel extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Set or clear the user log channel.";
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
		public void execute(IMessage message, String channelName)
		{
			IChannel channel = null;

			if (channelName.isEmpty() == true)
			{
				Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "userLogChannel", "");
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
				Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "userLogChannel", channelIdString);
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
				MessageQueue.sendMessage(message.getChannel(), "The user log channel has been cleared.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel(), "The user log channel has been set to: " + channel.getName());
			}
		}
	}
}
