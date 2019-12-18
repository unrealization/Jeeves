package me.unrealization.jeeves.modules;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.presence.Status;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
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
		this.version = "1.99.1";

		this.commandList = new String[2];
		this.commandList[0] = "GetUserLogChannel";
		this.commandList[1] = "SetUserLogChannel";

		this.defaultConfig.put("userLogChannel", "");
	}

	@Override
	public void userJoinedHandler(MemberJoinEvent event)
	{
		String channelIdString = (String)Jeeves.serverConfig.getValue(event.getGuildId().asLong(), "userLogChannel");

		if (channelIdString.isEmpty() == true)
		{
			return;
		}

		long channelId = Long.parseLong(channelIdString);
		GuildChannel channel = event.getGuild().block().getChannelById(Snowflake.of(channelId)).block();

		if (channel == null)
		{
			System.out.println("Invalid user log channel.");
			UserLog userLog = new UserLog();
			Jeeves.serverConfig.setValue(event.getGuildId().asLong(), "userLogChannel", userLog.getDefaultConfig().get("userLogChannel"));

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

		MessageQueue.sendMessage(channel, Jeeves.getUtcTime() + ": " + event.getMember().getDisplayName() + " has joined the server.");
	}

	@Override
	public void userLeftHandler(MemberLeaveEvent event)
	{
		String channelIdString = (String)Jeeves.serverConfig.getValue(event.getGuildId().asLong(), "userLogChannel");

		if (channelIdString.isEmpty() == true)
		{
			return;
		}

		long channelId = Long.parseLong(channelIdString);
		GuildChannel channel = event.getGuild().block().getChannelById(Snowflake.of(channelId)).block();

		if (channel == null)
		{
			System.out.println("Invalid user log channel.");
			Jeeves.serverConfig.setValue(event.getGuildId().asLong(), "userLogChannel", "");

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

		MessageQueue.sendMessage(channel, Jeeves.getUtcTime() + ": " + event.getMember().get().getDisplayName() + " has left the server.");
	}

	@Override
	public void presenceUpdateHandler(Guild server, PresenceUpdateEvent event)
	{
		String channelIdString = (String)Jeeves.serverConfig.getValue(server.getId().asLong(), "userLogChannel");

		if (channelIdString.isEmpty() == true)
		{
			return;
		}

		long channelId = Long.parseLong(channelIdString);
		GuildChannel channel = server.getChannelById(Snowflake.of(channelId)).block();

		if (channel == null)
		{
			System.out.println("Invalid user log channel.");
			Jeeves.serverConfig.setValue(server.getId().asLong(), "userLogChannel", "");

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

		Presence oldPresence = event.getOld().get();
		Presence newPresence = event.getCurrent();

		if (newPresence.equals(oldPresence) == true)
		{
			return;
		}

		if ((oldPresence.getStatus().equals(Status.ONLINE) == true) && ((newPresence.getStatus().equals(Status.IDLE) == true) || (newPresence.getStatus().equals(Status.DO_NOT_DISTURB) == true)))
		{
			return;
		}

		if ((newPresence.getStatus().equals(Status.ONLINE) == true) && ((oldPresence.getStatus().equals(Status.IDLE) == true) || (oldPresence.getStatus().equals(Status.DO_NOT_DISTURB) == true)))
		{
			return;
		}

		MessageQueue.sendMessage(channel, Jeeves.getUtcTime() + ": " + event.getMember().block().getDisplayName() + "'s status has changed from " + oldPresence.getStatus().name() + " to " + newPresence.getStatus().name());
	}

	@Override
	public void userUpdateHandler(Guild server, MemberUpdateEvent event)
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
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_GUILD;
			return permissionList;
		}

		@Override
		public void execute(Message message, String argumentString)
		{
			String channelIdString = (String)Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "userLogChannel");

			if (channelIdString.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No user log channel has been set.");
				return;
			}

			long channelId = Long.parseLong(channelIdString);
			GuildChannel channel = message.getGuild().block().getChannelById(Snowflake.of(channelId)).block();

			if (channel == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "A user log channel has been set, but it does not exist.");

				UserLog userLog = new UserLog();
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "userLogChannel", userLog.getDefaultConfig().get("userLogChannel"));

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

			MessageQueue.sendMessage(message.getChannel().block(), "The user log channel is: " + channel.getMention());
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
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_GUILD;
			return permissionList;
		}

		@Override
		public void execute(Message message, String channelName)
		{
			GuildChannel channel = null;

			if (channelName.isEmpty() == true)
			{
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "userLogChannel", "");
			}
			else
			{
				channel = Jeeves.findChannel(message.getGuild().block(), channelName);

				if (channel == null)
				{
					MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the channel " + channelName);
					return;
				}

				String channelIdString = channel.getId().asString();
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "userLogChannel", channelIdString);
			}

			try
			{
				Jeeves.serverConfig.saveConfig();
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot store the setting.");
				return;
			}

			if (channel == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The user log channel has been cleared.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The user log channel has been set to: " + channel.getMention());
			}
		}
	}
}
