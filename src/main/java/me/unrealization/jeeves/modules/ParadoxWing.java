package me.unrealization.jeeves.modules;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Permission;
import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.MessageQueue;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;

public class ParadoxWing extends BotModule implements UserJoinedHandler
{
	public ParadoxWing()
	{
		this.version = "2.0.0";

		this.commandList = new String[2];
		this.commandList[0] = "GetParadoxWelcomeMessageEnabled";
		this.commandList[1] = "SetParadoxWelcomeMessageEnabled";

		this.defaultConfig.put("paradoxWelcomeMessageEnabled", "0");
	}

	@Override
	public Long getDiscordId()
	{
		return Long.parseLong("195562598266044417");
	}

	@Override
	public void userJoinedHandler(MemberJoinEvent event)
	{
		String paradoxWelcomeMessageEnabled = (String)Jeeves.serverConfig.getValue(event.getGuildId().asLong(), "paradoxWelcomeMessageEnabled");

		if (paradoxWelcomeMessageEnabled.equals("1") == true)
		{
			String message = "Welcome to the home of Paradox Wing CMDR!\n\n";
			message += "Thank you so much for joining us here! We like to say hi to everyone personally; you've made the effort to come and visit us, so it's the least we can do.\n";
			message += "With that in mind, one of the Admin team will be along shortly to assign you the member role, but in the meantime, pop into the **#guest-chat**, make a cup of tea and say Hi!\n\n";
			message += "We're looking forward to seeing you out in the black! o7";

			MessageQueue.sendMessage(event.getMember(), message);
		}
	}

	public static class GetParadoxWelcomeMessageEnabled extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Check if the Paradox Wing welcome message is enabled.";
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
			String paradoxWelcomeMessageEnabled = (String)Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "paradoxWelcomeMessageEnabled");

			if (paradoxWelcomeMessageEnabled.equals("0") == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The Paradox Wing welcome message is disabled.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The Paradox Wing welcome message is enabled.");
			}
		}
	}

	public static class SetParadoxWelcomeMessageEnabled extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Enable/disable the Paradox Wing welcome message.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<1|0>";
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
		public void execute(Message message, String paradoxWelcomeMessageEnabled)
		{
			if ((paradoxWelcomeMessageEnabled.equals("0") == false) && (paradoxWelcomeMessageEnabled.equals("1") == false))
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Invalid value");
				return;
			}

			Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "paradoxWelcomeMessageEnabled", paradoxWelcomeMessageEnabled);

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

			if (paradoxWelcomeMessageEnabled.equals("0") == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The Paradox Wing welcome message has been disabled.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The Paradox Wing welcome message has been enabled.");
			}
		}
	}
}
