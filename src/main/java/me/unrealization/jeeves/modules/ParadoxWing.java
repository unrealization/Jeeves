package me.unrealization.jeeves.modules;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.MessageQueue;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

public class ParadoxWing extends BotModule implements UserJoinedHandler
{
	public ParadoxWing()
	{
		this.version = "1.0.0";

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
	public void userJoinedHandler(UserJoinEvent event)
	{
		String paradoxWelcomeMessageEnabled = (String)Jeeves.serverConfig.getValue(event.getGuild().getLongID(), "paradoxWelcomeMessageEnabled");

		if (paradoxWelcomeMessageEnabled.equals("1") == true)
		{
			String message = "Welcome to the home of Paradox Wing CMDR!\n\n";
			message += "Thank you so much for joining us here! We like to say hi to everyone personally; you've made the effort to come and visit us, so it's the least we can do.\n";
			message += "With that in mind, one of the Admin team will be along shortly to assign you the member role, but in the meantime, pop into the **#guest-chat**, make a cup of tea and say Hi!\n\n";
			message += "We're looking forward to seeing you out in the black! o7";

			MessageQueue.sendMessage(event.getUser(), message);
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
		public Permissions[] permissions()
		{
			Permissions[] permissionList = new Permissions[1];
			permissionList[0] = Permissions.MANAGE_SERVER;
			return permissionList;
		}

		@Override
		public void execute(IMessage message, String argumentString)
		{
			String paradoxWelcomeMessageEnabled = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "paradoxWelcomeMessageEnabled");

			if (paradoxWelcomeMessageEnabled.equals("0") == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "The Paradox Wing welcome message is disabled.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel(), "The Paradox Wing welcome message is enabled.");
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
		public Permissions[] permissions()
		{
			Permissions[] permissionList = new Permissions[1];
			permissionList[0] = Permissions.MANAGE_SERVER;
			return permissionList;
		}

		@Override
		public void execute(IMessage message, String paradoxWelcomeMessageEnabled)
		{
			if ((paradoxWelcomeMessageEnabled.equals("0") == false) && (paradoxWelcomeMessageEnabled.equals("1") == false))
			{
				MessageQueue.sendMessage(message.getChannel(), "Invalid value");
				return;
			}

			Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "paradoxWelcomeMessageEnabled", paradoxWelcomeMessageEnabled);

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

			if (paradoxWelcomeMessageEnabled.equals("0") == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "The Paradox Wing welcome message has been disabled.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel(), "The Paradox Wing welcome message has ben enabled.");
			}
		}
	}
}
