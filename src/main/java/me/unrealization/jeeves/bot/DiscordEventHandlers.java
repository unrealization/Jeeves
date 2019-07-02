package me.unrealization.jeeves.bot;

import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.MessageDeleteHandler;
import me.unrealization.jeeves.interfaces.MessageReceivedHandler;
import me.unrealization.jeeves.interfaces.MessageUpdateHandler;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;
import me.unrealization.jeeves.interfaces.PresenceUpdateHandler;
import me.unrealization.jeeves.interfaces.UserLeftHandler;
import me.unrealization.jeeves.interfaces.UserUpdateHandler;

import java.util.EnumSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import me.unrealization.jeeves.modules.Internal;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MentionEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageDeleteEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent;
import sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;
import sx.blah.discord.handle.impl.events.user.UserUpdateEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;

public class DiscordEventHandlers
{
	public static class ReadyEventListener implements IListener<ReadyEvent>
	{
		@Override
		public void handle(ReadyEvent event)
		{
			IDiscordClient bot = event.getClient();

			EventDispatcher dispatcher = bot.getDispatcher();
			dispatcher.registerListener(new MessageReceivedListener());
			dispatcher.registerListener(new MentionListener());
			dispatcher.registerListener(new UserJoinedListener());
			dispatcher.registerListener(new UserLeftListener());
			dispatcher.registerListener(new UserUpdateListener());
			dispatcher.registerListener(new UserPresenceListener());
			dispatcher.registerListener(new GuildCreateListener());
			dispatcher.registerListener(new MessageUpdateListener());
			dispatcher.registerListener(new MessageDeleteListener());

			IUser botUser = bot.getOurUser();
			System.out.println("Logged in as " + botUser.getName() + " (" + Jeeves.version + ")");

			try
			{
				Scheduler scheduler = new StdSchedulerFactory().getScheduler();

				if (scheduler.isStarted() == false)
				{
					scheduler.start();
				}
			}
			catch (SchedulerException e)
			{
				Jeeves.debugException(e);
			}

		}
	}

	private static class MessageReceivedListener implements IListener<MessageReceivedEvent>
	{
		@Override
		public void handle(MessageReceivedEvent event)
		{
			IMessage message = event.getMessage();
			String[] moduleList = Jeeves.getModuleList();

			for (int index = 0; index < moduleList.length; index++)
			{
				BotModule module = Jeeves.getModule(moduleList[index]);

				if (Jeeves.isDisabled(event.getGuild().getLongID(), module) == true)
				{
					continue;
				}

				MessageReceivedHandler handler;

				try
				{
					handler = (MessageReceivedHandler)module;
				}
				catch (Exception e)
				{
					//Jeeves.debugException(e);
					continue;
				}

				boolean handled = handler.messageReceivedHandler(message);

				if (handled == true)
				{
					return;
				}
			}

			String respondOnPrefix = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "respondOnPrefix");

			if (respondOnPrefix.equals("0") == true)
			{
				return;
			}

			String messageContent = message.getContent();

			if (messageContent.startsWith((String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "commandPrefix")) == true)
			{
				DiscordEventHandlers.handleMessage(message);
			}
		}
	}

	private static class MentionListener implements IListener<MentionEvent>
	{
		@Override
		public void handle(MentionEvent event)
		{
			IMessage message = event.getMessage();
			String respondOnMention = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "respondOnMention");

			if (respondOnMention.equals("0"))
			{
				return;
			}

			String messageContent = message.getContent();
			IUser botUser = event.getClient().getOurUser();

			if ((messageContent.startsWith(botUser.mention(true)) == true) || (messageContent.startsWith(botUser.mention(false)) == true))
			{
				DiscordEventHandlers.handleMessage(message);
			}
		}
	}

	private static class UserJoinedListener implements IListener<UserJoinEvent>
	{
		@Override
		public void handle(UserJoinEvent event)
		{
			System.out.println("User " + event.getUser().getName() + " has joined " + event.getGuild().getName());
			String[] moduleList = Jeeves.getModuleList();

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

				if (Jeeves.isDisabled(event.getGuild().getLongID(), module) == true)
				{
					continue;
				}

				UserJoinedHandler handler;

				try
				{
					handler = (UserJoinedHandler)module;
				}
				catch (Exception e)
				{
					//Jeeves.debugException(e);
					continue;
				}

				handler.userJoinedHandler(event);
			}
		}
	}

	private static class UserLeftListener implements IListener<UserLeaveEvent>
	{
		@Override
		public void handle(UserLeaveEvent event)
		{
			String[] moduleList = Jeeves.getModuleList();

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

				if (Jeeves.isDisabled(event.getGuild().getLongID(), module) == true)
				{
					continue;
				}

				UserLeftHandler handler;

				try
				{
					handler = (UserLeftHandler)module;
				}
				catch (Exception e)
				{
					//Jeeves.debugException(e);
					continue;
				}

				handler.userLeftHandler(event);
			}
		}
	}

	private static class UserUpdateListener implements IListener<UserUpdateEvent>
	{
		@Override
		public void handle(UserUpdateEvent event)
		{
			List<IGuild> serverList = event.getClient().getGuilds();
			String[] moduleList = Jeeves.getModuleList();

			for (int serverIndex = 0; serverIndex < serverList.size(); serverIndex++)
			{
				IGuild server = serverList.get(serverIndex);
				IUser user = server.getUserByID(event.getNewUser().getLongID());

				if (user == null)
				{
					continue;
				}

				for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
				{
					BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

					if (Jeeves.isDisabled(server.getLongID(), module) == true)
					{
						continue;
					}

					UserUpdateHandler handler;

					try
					{
						handler = (UserUpdateHandler)module;
					}
					catch (Exception e)
					{
						//Jeeves.debugException(e);
						continue;
					}

					handler.userUpdateHandler(server, event);
				}
			}
		}
	}

	private static class UserPresenceListener implements IListener<PresenceUpdateEvent>
	{
		@Override
		public void handle(PresenceUpdateEvent event)
		{
			List<IGuild> serverList = event.getClient().getGuilds();
			String[] moduleList = Jeeves.getModuleList();

			for (int serverIndex = 0; serverIndex < serverList.size(); serverIndex++)
			{
				IGuild server = serverList.get(serverIndex);
				IUser user = server.getUserByID(event.getUser().getLongID());

				if (user == null)
				{
					continue;
				}

				for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
				{
					BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

					if (Jeeves.isDisabled(server.getLongID(), module) == true)
					{
						continue;
					}

					PresenceUpdateHandler handler;

					try
					{
						handler = (PresenceUpdateHandler)module;
					}
					catch (Exception e)
					{
						//Jeeves.debugException(e);
						continue;
					}

					handler.presenceUpdateHandler(server, event);
				}
			}
		}
	}

	private static class GuildCreateListener implements IListener<GuildCreateEvent>
	{
		@Override
		public void handle(GuildCreateEvent event)
		{
			Internal internal = new Internal();

			try
			{
				Jeeves.checkConfig(event.getGuild().getLongID(), internal.getDefaultConfig());
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				boolean debugging = Jeeves.debugException(e);

				if (debugging == true)
				{
					System.out.println("Cannot create default config for " + event.getGuild().getName());
				}
			}
		}
	}

	private static class MessageUpdateListener implements IListener<MessageUpdateEvent>
	{
		@Override
		public void handle(MessageUpdateEvent event)
		{
			String[] moduleList = Jeeves.getModuleList();

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

				if (Jeeves.isDisabled(event.getNewMessage().getGuild().getLongID(), module) == true)
				{
					continue;
				}

				MessageUpdateHandler handler;

				try
				{
					handler = (MessageUpdateHandler)module;
				}
				catch (Exception e)
				{
					//Jeeves.debugException(e);
					continue;
				}

				handler.messageUpdateHandler(event);
			}
		}
	}

	private static class MessageDeleteListener implements IListener<MessageDeleteEvent>
	{
		@Override
		public void handle(MessageDeleteEvent event)
		{
			String[] moduleList = Jeeves.getModuleList();

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

				if (Jeeves.isDisabled(event.getMessage().getGuild().getLongID(), module) == true)
				{
					continue;
				}

				MessageDeleteHandler handler;

				try
				{
					handler = (MessageDeleteHandler)module;
				}
				catch (Exception e)
				{
					//Jeeves.debugException(e);
					continue;
				}

				handler.messageDeleteHandler(event);
			}
		}
	}

	public static void handleMessage(IMessage message)
	{
		DiscordEventHandlers.handleMessage(message, false);
	}

	public static void handleMessage(IMessage message, boolean cronJob)
	{
		if ((message.mentionsEveryone() == true) || (message.mentionsHere() == true))
		{
			return;
		}

		if (Jeeves.isIgnored(message.getChannel()) == true)
		{
			return;
		}

		if (Jeeves.isIgnored(message.getGuild().getLongID(), message.getAuthor()) == true)
		{
			return;
		}

		List<IRole> roleList = message.getAuthor().getRolesForGuild(message.getGuild());

		for (int index = 0; index < roleList.size(); index++)
		{
			if (Jeeves.isIgnored(roleList.get(index)) == true)
			{
				return;
			}
		}

		String messageContent = message.getContent().trim();
		IUser botUser = message.getClient().getOurUser();
		int cutLength = 0;
		String commandPrefix = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "commandPrefix");

		if (messageContent.startsWith(commandPrefix))
		{
			cutLength = commandPrefix.length();
		}
		else if (messageContent.startsWith(botUser.mention(true)))
		{
			cutLength = botUser.mention(true).length();
		}
		else if (messageContent.startsWith(botUser.mention(false)))
		{
			cutLength = botUser.mention(false).length();
		}
		else
		{
			System.out.println("What is this message doing here?");
			System.out.println("Message: " + message.getContent());
		}
		
		if (cutLength > 0)
		{
			messageContent = messageContent.substring(cutLength);
		}

		String[] messageParts = messageContent.split(" ");
		int nonEmptyParts = 0;

		for (int index = 0; index < messageParts.length; index++)
		{
			if (messageParts[index].isEmpty() == false)
			{
				nonEmptyParts++;
			}
		}

		if (nonEmptyParts == 0)
		{
			System.out.println("Empty");
			return;
		}

		if (nonEmptyParts != messageParts.length)
		{
			String tmpParts[] = new String[nonEmptyParts];
			int tmpIndex = 0;

			for (int index = 0; index < messageParts.length; index++)
			{
				if (messageParts[index].isEmpty() == true)
				{
					continue;
				}

				tmpParts[tmpIndex] = messageParts[index];
				tmpIndex++;
			}

			messageParts = tmpParts;
		}

		String commandName = messageParts[0].toLowerCase();
		String[] arguments = new String[messageParts.length - 1];

		for (int x = 1; x < messageParts.length; x++)
		{
			arguments[x - 1] = messageParts[x];
		}

		String[] moduleList = Jeeves.getModuleList();

		for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
		{
			BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

			if (Jeeves.isDisabled(message.getGuild().getLongID(), module) == true)
			{
				continue;
			}

			String[] commandList = module.getCommands();

			if (commandList == null)
			{
				continue;
			}

			for (int commandIndex = 0; commandIndex < commandList.length; commandIndex++)
			{
				if (commandList[commandIndex].toLowerCase().equals(commandName) == false)
				{
					continue;
				}

				try
				{
					Jeeves.checkConfig(message.getGuild().getLongID(), module.getDefaultConfig());
				}
				catch (ParserConfigurationException | TransformerException e)
				{
					Jeeves.debugException(e);
					return;
				}

				Long discordId = module.getDiscordId();

				if ((discordId != null) && (discordId.equals(message.getGuild().getLongID()) == false))
				{
					MessageQueue.sendMessage(message.getChannel(), "This command is not available on this Discord.");
					return;
				}

				Class<?> commandClass;

				try
				{
					commandClass = Class.forName(module.getClass().getName() + "$" + commandList[commandIndex]);
				}
				catch (ClassNotFoundException e)
				{
					Jeeves.debugException(e);
					return;
				}

				BotCommand command;

				try
				{
					command = (BotCommand)commandClass.newInstance();
				}
				catch (InstantiationException | IllegalAccessException e)
				{
					Jeeves.debugException(e);
					return;
				}

				if (command == null)
				{
					System.out.println("Error");
				}

				if (command.owner() == true)
				{
					Long ownerId = null;

					try
					{
						ownerId = message.getClient().getApplicationOwner().getLongID();
					}
					catch (DiscordException e)
					{
						Jeeves.debugException(e);
					}

					if ((ownerId != null) && (ownerId.equals(message.getAuthor().getLongID()) == false))
					{
						MessageQueue.sendMessage(message.getChannel(), "You are not permitted to execute this command.");
						return;
					}
				}

				Permissions[] permissionList = command.permissions();

				if ((permissionList != null) && (cronJob == false))
				{
					EnumSet<Permissions> userPermissions = message.getAuthor().getPermissionsForGuild(message.getGuild());

					for (int permissionIndex = 0; permissionIndex < permissionList.length; permissionIndex++)
					{
						if (userPermissions.contains(permissionList[permissionIndex]) == false)
						{
							MessageQueue.sendMessage(message.getChannel(), "You are not permitted to execute this command.");
							return;
						}
					}
				}

				System.out.println("Executing " + command.getClass().getSimpleName() + " for " + message.getAuthor().getName() + " (" + message.getGuild().getName() + ": " + message.getChannel().getName() + ")");
				String argumentString = String.join(" ", arguments);
				CommandQueue.runCommand(command, message, argumentString);
			}
		}
	}
}
