package me.unrealization.jeeves.modules;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.MessageQueue;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;

public class Welcome extends BotModule implements UserJoinedHandler
{
	public Welcome()
	{
		this.version = "2.0.1";

		this.commandList = new String[2];
		this.commandList[0] = "GetWelcomeChannel";
		this.commandList[1] = "SetWelcomeChannel";

		this.defaultConfig.put("welcomeChannel", "");
	}

	@Override
	public void userJoinedHandler(MemberJoinEvent event)
	{
		String channelIdString = (String)Jeeves.serverConfig.getValue(event.getGuildId().asLong(), "welcomeChannel");

		if (channelIdString.isEmpty() == true)
		{
			return;
		}

		long channelId = Long.parseLong(channelIdString);
		GuildChannel channel = event.getGuild().block().getChannelById(Snowflake.of(channelId)).block();

		if (channel == null)
		{
			System.out.println("Invalid welcome channel.");
			Welcome welcome = new Welcome();
			Jeeves.serverConfig.setValue(event.getGuildId().asLong(), "welcomeChannel", welcome.getDefaultConfig().get("welcomeChannel"));

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

		MessageQueue.sendMessage(channel, "Welcome to " + event.getGuild().block().getName() + ", " + event.getMember().getMention() + "!");
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
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_GUILD;
			return permissionList;
		}

		@Override
		public void execute(Message message, String argumentString)
		{
			String channelIdString = (String)Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "welcomeChannel");

			if (channelIdString.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No welcome channel has been set.");
				return;
			}

			long channelId = Long.parseLong(channelIdString);
			GuildChannel channel = message.getGuild().block().getChannelById(Snowflake.of(channelId)).block();

			if (channel == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "A welcome channel has been set, but it does not exist.");

				Welcome welcome = new Welcome();
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "welcomeChannel", welcome.getDefaultConfig().get("welcomeChannel"));

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

			MessageQueue.sendMessage(message.getChannel().block(), "The welcome channel is: " + channel.getMention());
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
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "welcomeChannel", "");
			}
			else
			{
				channel = Jeeves.findChannel(message.getGuild().block(), channelName);

				if (channel == null)
				{
					MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the channel " + channelName);
					return;
				}

				String channelIdString = Long.toString(channel.getId().asLong());
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "welcomeChannel", channelIdString);
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
				MessageQueue.sendMessage(message.getChannel().block(), "The welcome channel has been cleared.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The welcome channel has been set to: " + channel.getMention());
			}
		}
	}
}
