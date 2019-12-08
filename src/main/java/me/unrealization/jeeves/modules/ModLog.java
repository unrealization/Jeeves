package me.unrealization.jeeves.modules;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
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
		this.version = "2.0.0";

		this.commandList = new String[2];
		this.commandList[0] = "GetModLogChannel";
		this.commandList[1] = "SetModLogChannel";

		this.defaultConfig.put("modLogChannel", "");
	}

	@Override
	public void messageUpdateHandler(MessageUpdateEvent event)
	{
		if (event.isContentChanged() == false)
		{
			return;
		}

		String channelIdString = (String)Jeeves.serverConfig.getValue(event.getGuildId().get().asLong(), "modLogChannel");

		if (channelIdString.isEmpty() == true)
		{
			return;
		}

		long channelId = Long.parseLong(channelIdString);
		Channel channel = event.getGuild().block().getChannelById(Snowflake.of(channelId)).block();

		if (channel == null)
		{
			System.out.println("Invalid mod log channel.");
			ModLog modLog = new ModLog();
			Jeeves.serverConfig.setValue(event.getGuildId().get().asLong(), "modLogChannel", modLog.getDefaultConfig().get("modLogChannel"));

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

		String oldAuthor = event.getOld().get().getAuthor().get().getUsername();
		String newAuthor = event.getMessage().block().getAuthor().get().getUsername();
		String output = Jeeves.getUtcTime() + ": " + newAuthor + " has edited one of ";

		if (oldAuthor.equals(newAuthor) == true)
		{
			output += "their";
		}
		else
		{
			output += oldAuthor + "'s";
		}

		
		output += " messages in " + event.getChannel().block().getMention() + "\n";
		output += "=============================================\n";
		output += "**Old Message**\n";
		output += "=============================================\n";
		output += event.getOld().get().getContent().get() + "\n";
		output += "=============================================\n";
		output += "**New Message**\n";
		output += "=============================================\n";
		output += event.getMessage().block().getContent().get() + "\n";
		output += "=============================================\n";
		MessageQueue.sendMessage(channel, output);
	}

	@Override
	public void messageDeleteHandler(MessageDeleteEvent event)
	{
		String channelIdString = (String)Jeeves.serverConfig.getValue(event.getMessage().get().getGuild().block().getId().asLong(), "modLogChannel");

		if (channelIdString.isEmpty() == true)
		{
			return;
		}

		long channelId = Long.parseLong(channelIdString);
		Channel channel = event.getMessage().get().getGuild().block().getChannelById(Snowflake.of(channelId)).block();

		if (channel == null)
		{
			System.out.println("Invalid mod log channel.");
			ModLog modLog = new ModLog();
			Jeeves.serverConfig.setValue(event.getMessage().get().getGuild().block().getId().asLong(), "modLogChannel", modLog.getDefaultConfig().get("modLogChannel"));

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

		String output = Jeeves.getUtcTime() + ": One of " + event.getMessage().get().getAuthor().get().getUsername() + "'s messages has been deleted in " + event.getChannel().block().getMention() + "\n";
		output += "=============================================\n";
		output += event.getMessage().get().getContent().get() + "\n";
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
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_GUILD;
			return permissionList;
		}

		@Override
		public void execute(Message message, String argumentString)
		{
			String channelIdString = (String)Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "modLogChannel");

			if (channelIdString.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No mod log channel has been set.");
				return;
			}

			long channelId = Long.parseLong(channelIdString);
			Channel channel = message.getGuild().block().getChannelById(Snowflake.of(channelId)).block();

			if (channel == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "A mod log channel has been set, but it does not exist.");

				ModLog modLog = new ModLog();
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "modLogChannel", modLog.getDefaultConfig().get("modLogChannel"));

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

			MessageQueue.sendMessage(message.getChannel().block(), "The mod log channel is: " + channel.getMention());
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
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_GUILD;
			return permissionList;
		}

		@Override
		public void execute(Message message, String channelName)
		{
			Channel channel = null;

			if (channelName.isEmpty() == true)
			{
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "modLogChannel", "");
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
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "modLogChannel", channelIdString);
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
				MessageQueue.sendMessage(message.getChannel().block(), "The mod log channel has been cleared.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The mod log channel has been set to: " + channel.getMention());
			}
		}
	}
}
