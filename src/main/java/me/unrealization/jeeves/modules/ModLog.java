package me.unrealization.jeeves.modules;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.MessageQueue;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.MessageDeleteHandler;
import me.unrealization.jeeves.interfaces.MessageUpdateHandler;

public class ModLog extends BotModule implements MessageUpdateHandler, MessageDeleteHandler
{
	public ModLog()
	{
		this.version = "1.0";

		this.commandList = new String[2];
		this.commandList[0] = "GetModLogChannel";
		this.commandList[1] = "SetModLogChannel";

		this.defaultConfig.put("modLogChannel", "");
	}

	@Override
	public void messageUpdateHandler(MessageUpdateEvent event)
	{
		String channelIdString = (String)Jeeves.serverConfig.getValue(event.getNewMessage().getGuild().getLongID(), "modLogChannel");

		if (channelIdString.isEmpty() == true)
		{
			return;
		}

		long channelId = Long.parseLong(channelIdString);
		IChannel channel = event.getNewMessage().getGuild().getChannelByID(channelId);

		if (channel == null)
		{
			System.out.println("Invalid mod log channel.");
			ModLog modLog = new ModLog();
			Jeeves.serverConfig.setValue(event.getNewMessage().getGuild().getLongID(), "modLogChannel", modLog.getDefaultConfig().get("modLogChannel"));

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

		String oldAuthor = event.getOldMessage().getAuthor().getName();
		String newAuthor = event.getNewMessage().getAuthor().getName();
		String output = Jeeves.getUtcTime() + ": " + newAuthor + " has edited one of ";

		if (oldAuthor.equals(newAuthor) == true)
		{
			output += "his";
		}
		else
		{
			output += oldAuthor + "'s";
		}

		output += " messages in " + event.getNewMessage().getChannel().mention() + "\n";
		output += "=============================================\n";
		output += "**Old Message**\n";
		output += "=============================================\n";
		output += event.getOldMessage().getContent() + "\n";
		output += "=============================================\n";
		output += "**New Message**\n";
		output += "=============================================\n";
		output += event.getNewMessage().getContent() + "\n";
		output += "=============================================\n";
		MessageQueue.sendMessage(channel, output);
	}

	@Override
	public void messageDeleteHandler(MessageDeleteEvent event)
	{
		String channelIdString = (String)Jeeves.serverConfig.getValue(event.getMessage().getGuild().getLongID(), "modLogChannel");

		if (channelIdString.isEmpty() == true)
		{
			return;
		}

		long channelId = Long.parseLong(channelIdString);
		IChannel channel = event.getMessage().getGuild().getChannelByID(channelId);

		if (channel == null)
		{
			System.out.println("Invalid mod log channel.");
			ModLog modLog = new ModLog();
			Jeeves.serverConfig.setValue(event.getMessage().getGuild().getLongID(), "modLogChannel", modLog.getDefaultConfig().get("modLogChannel"));

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

		String output = Jeeves.getUtcTime() + ": One of " + event.getMessage().getAuthor().getName() + "'s messages has been deleted in " + event.getMessage().getChannel().mention() + "\n";
		output += "=============================================\n";
		output += event.getMessage().getContent() + "\n";
		output += "=============================================\n";
		MessageQueue.sendMessage(channel, output);
	}

	public static class GetModLogChannel extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Get the mod log channel.";
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
			String channelIdString = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "modLogChannel");

			if (channelIdString.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "No mod log channel has been set.");
				return;
			}

			long channelId = Long.parseLong(channelIdString);
			IChannel channel = message.getGuild().getChannelByID(channelId);

			if (channel == null)
			{
				MessageQueue.sendMessage(message.getChannel(), "A mod log channel has been set, but it does not exist.");

				ModLog modLog = new ModLog();
				Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "modLogChannel", modLog.getDefaultConfig().get("modLogChannel"));

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

			MessageQueue.sendMessage(message.getChannel(), "The mod log channel is: " + channel.getName());
		}
	}

	public static class SetModLogChannel extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Set or clear the mod log channel.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<channel>";
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
				Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "modLogChannel", "");
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
				Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "modLogChannel", channelIdString);
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
				MessageQueue.sendMessage(message.getChannel(), "The mod log channel has been cleared.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel(), "The mod log channel has been set to: " + channel.getName());
			}
		}
	}
}
