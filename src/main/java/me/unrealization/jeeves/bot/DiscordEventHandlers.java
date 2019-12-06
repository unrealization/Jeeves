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

import discord4j.core.DiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import me.unrealization.jeeves.modules.Internal;

public class DiscordEventHandlers
{
	interface EventListener<EventType extends Event>
	{
		public void execute(EventType event);
	}

	public static class ReadyEventListener implements EventListener<ReadyEvent>
	{
		@Override
		public void execute(ReadyEvent event)
		{
			DiscordClient bot = event.getClient();
			EventDispatcher dispatcher = bot.getEventDispatcher();

			dispatcher.on(MessageCreateEvent.class).subscribe(runtimeEvent -> new MessageReceivedListener().execute(runtimeEvent));
			dispatcher.on(MemberJoinEvent.class).subscribe(runtimeEvent -> new UserJoinedListener().execute(runtimeEvent));
			dispatcher.on(MemberLeaveEvent.class).subscribe(runtimeEvent -> new UserLeftListener().execute(runtimeEvent));
			dispatcher.on(MemberUpdateEvent.class).subscribe(runtimeEvent -> new UserUpdateListener().execute(runtimeEvent));
			dispatcher.on(PresenceUpdateEvent.class).subscribe(runtimeEvent -> new UserPresenceListener().execute(runtimeEvent));
			dispatcher.on(GuildCreateEvent.class).subscribe(runtimeEvent -> new GuildCreateListener().execute(runtimeEvent));
			dispatcher.on(MessageUpdateEvent.class).subscribe(runtimeEvent -> new MessageUpdateListener().execute(runtimeEvent));
			dispatcher.on(MessageDeleteEvent.class).subscribe(runtimeEvent -> new MessageDeleteListener().execute(runtimeEvent));

			User botUser = bot.getSelf().block();
			System.out.println("Logged in as " + botUser.getUsername() + " (" + Jeeves.version + ")");

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

	private static class MessageReceivedListener implements EventListener<MessageCreateEvent>
	{
		@Override
		public void execute(MessageCreateEvent event)
		{
			Message message = event.getMessage();
			String[] moduleList = Jeeves.getModuleList();

			for (int index = 0; index < moduleList.length; index++)
			{
				BotModule module = Jeeves.getModule(moduleList[index]);

				if (Jeeves.isDisabled(event.getGuildId().get().asLong(), module) == true)
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

			String respondOnPrefix = (String)Jeeves.serverConfig.getValue(event.getGuildId().get().asLong(), "respondOnPrefix");

			if (respondOnPrefix.equals("0") == true)
			{
				return;
			}

			String messageContent = message.getContent().get();

			if (messageContent.startsWith((String)Jeeves.serverConfig.getValue(event.getGuildId().get().asLong(), "commandPrefix")) == true)
			{
				DiscordEventHandlers.handleMessage(message);
			}
		}
	}

	private static class UserJoinedListener implements EventListener<MemberJoinEvent>
	{
		@Override
		public void execute(MemberJoinEvent event)
		{
			System.out.println("User " + event.getMember().getDisplayName() + " has joined " + event.getGuild().block().getName());
			String[] moduleList = Jeeves.getModuleList();

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

				if (Jeeves.isDisabled(event.getGuildId().asLong(), module) == true)
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

	private static class UserLeftListener implements EventListener<MemberLeaveEvent>
	{
		@Override
		public void execute(MemberLeaveEvent event)
		{
			String[] moduleList = Jeeves.getModuleList();

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

				if (Jeeves.isDisabled(event.getGuildId().asLong(), module) == true)
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

	private static class UserUpdateListener implements EventListener<MemberUpdateEvent>
	{
		@Override
		public void execute(MemberUpdateEvent event)
		{
			List<Guild> serverList = (List<Guild>)event.getClient().getGuilds().toIterable();
			String[] moduleList = Jeeves.getModuleList();

			for (int serverIndex = 0; serverIndex < serverList.size(); serverIndex++)
			{
				Guild server = serverList.get(serverIndex);
				User user = server.getMemberById(event.getMemberId()).block();

				if (user == null)
				{
					continue;
				}

				for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
				{
					BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

					if (Jeeves.isDisabled(server.getId().asLong(), module) == true)
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

	private static class UserPresenceListener implements EventListener<PresenceUpdateEvent>
	{
		@Override
		public void execute(PresenceUpdateEvent event)
		{
			List<Guild> serverList = (List<Guild>)event.getClient().getGuilds().toIterable();
			String[] moduleList = Jeeves.getModuleList();

			for (int serverIndex = 0; serverIndex < serverList.size(); serverIndex++)
			{
				Guild server = serverList.get(serverIndex);
				User user = server.getMemberById(event.getUserId()).block();

				if (user == null)
				{
					continue;
				}

				for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
				{
					BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

					if (Jeeves.isDisabled(server.getId().asLong(), module) == true)
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

	private static class GuildCreateListener implements EventListener<GuildCreateEvent>
	{
		@Override
		public void execute(GuildCreateEvent event)
		{
			Internal internal = new Internal();

			try
			{
				Jeeves.checkConfig(event.getGuild().getId().asLong(), internal.getDefaultConfig());
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

	private static class MessageUpdateListener implements EventListener<MessageUpdateEvent>
	{
		@Override
		public void execute(MessageUpdateEvent event)
		{
			String[] moduleList = Jeeves.getModuleList();

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

				if (Jeeves.isDisabled(event.getGuildId().get().asLong(), module) == true)
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

	private static class MessageDeleteListener implements EventListener<MessageDeleteEvent>
	{
		@Override
		public void execute(MessageDeleteEvent event)
		{
			String[] moduleList = Jeeves.getModuleList();

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

				if (Jeeves.isDisabled(event.getMessage().get().getGuild().block().getId().asLong(), module) == true)
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

	public static void handleMessage(Message message)
	{
		DiscordEventHandlers.handleMessage(message, false);
	}

	public static void handleMessage(Message message, boolean cronJob)
	{
		//if ((message.mentionsEveryone() == true) || (message.mentionsHere() == true))
		if (message.mentionsEveryone() == true)
		{
			return;
		}

		if (Jeeves.isIgnored(message.getGuild().block().getId().asLong(), message.getChannel().block()) == true)
		{
			return;
		}

		if (Jeeves.isIgnored(message.getGuild().block().getId().asLong(), message.getAuthor().get()) == true)
		{
			return;
		}

		List<Role> roleList = (List<Role>)message.getAuthorAsMember().block().getRoles().toIterable();

		for (int index = 0; index < roleList.size(); index++)
		{
			if (Jeeves.isIgnored(message.getGuild().block().getId().asLong(), roleList.get(index)) == true)
			{
				return;
			}
		}

		String messageContent = message.getContent().get().trim();
		User botUser = message.getClient().getSelf().block();
		int cutLength = 0;
		String commandPrefix = (String)Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "commandPrefix");

		if (messageContent.startsWith(commandPrefix))
		{
			cutLength = commandPrefix.length();
		}
		else if (messageContent.startsWith(botUser.getMention()))
		{
			cutLength = botUser.getMention().length();
		}
		/*else if (messageContent.startsWith(botUser.mention(true)))
		{
			cutLength = botUser.mention(true).length();
		}
		else if (messageContent.startsWith(botUser.mention(false)))
		{
			cutLength = botUser.mention(false).length();
		}*/
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

			if (Jeeves.isDisabled(message.getGuild().block().getId().asLong(), module) == true)
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
					Jeeves.checkConfig(message.getGuild().block().getId().asLong(), module.getDefaultConfig());
				}
				catch (ParserConfigurationException | TransformerException e)
				{
					Jeeves.debugException(e);
					return;
				}

				Long discordId = module.getDiscordId();

				if ((discordId != null) && (discordId.equals(message.getGuild().block().getId().asLong()) == false))
				{
					MessageQueue.sendMessage(message.getChannel().block(), "This command is not available on this Discord.");
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
					Long ownerId = message.getClient().getApplicationInfo().block().getOwnerId().asLong();

					if ((ownerId != null) && (ownerId.equals(message.getAuthor().get().getId().asLong()) == false))
					{
						MessageQueue.sendMessage(message.getChannel().block(), "You are not permitted to execute this command.");
						return;
					}
				}

				Permission[] permissionList = command.permissions();

				if ((permissionList != null) && (cronJob == false))
				{
					EnumSet<Permission> userPermissions = message.getAuthorAsMember().block().getBasePermissions().block().asEnumSet();

					for (int permissionIndex = 0; permissionIndex < permissionList.length; permissionIndex++)
					{
						if (userPermissions.contains(permissionList[permissionIndex]) == false)
						{
							MessageQueue.sendMessage(message.getChannel().block(), "You are not permitted to execute this command.");
							return;
						}
					}
				}

				System.out.println("Executing " + command.getClass().getSimpleName() + " for " + message.getAuthor().get().getUsername() + " (" + message.getGuild().block().getName() + ": " + message.getChannel().block().getMention() + ")");
				String argumentString = String.join(" ", arguments);
				CommandQueue.runCommand(command, message, argumentString);
			}
		}
	}
}
