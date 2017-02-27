package me.unrealization.jeeves.bot;

import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;
import me.unrealization.jeeves.interfaces.PresenceUpdateHandler;
import me.unrealization.jeeves.interfaces.UserLeftHandler;
import me.unrealization.jeeves.interfaces.UserUpdateHandler;

import java.util.EnumSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import me.unrealization.jeeves.modules.Internal;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.MentionEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.events.UserLeaveEvent;
import sx.blah.discord.handle.impl.events.UserUpdateEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
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

			IUser botUser = bot.getOurUser();
			System.out.println("Logged in as " + botUser.getName() + " (" + Jeeves.version + ")");
		}
	}

	public static class MessageReceivedListener implements IListener<MessageReceivedEvent>
	{
		@Override
		public void handle(MessageReceivedEvent event)
		{
			IMessage message = event.getMessage();
			String respondOnPrefix = (String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "respondOnPrefix");

			if (respondOnPrefix.equals("0") == true)
			{
				return;
			}

			String messageContent = message.getContent();

			if (messageContent.startsWith((String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "commandPrefix")) == true)
			{
				DiscordEventHandlers.handleMessage(message);
			}
		}
	}

	public static class MentionListener implements IListener<MentionEvent>
	{
		@Override
		public void handle(MentionEvent event)
		{
			IMessage message = event.getMessage();
			String respondOnMention = (String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "respondOnMention");

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

	public static class UserJoinedListener implements IListener<UserJoinEvent>
	{
		@Override
		public void handle(UserJoinEvent event)
		{
			System.out.println("User " + event.getUser().getName() + " has joined " + event.getGuild().getName());
			String[] moduleList = Jeeves.getModuleList();

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

				if (Jeeves.isDisabled(event.getGuild().getID(), module) == true)
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
					Jeeves.debugException(e);
					continue;
				}

				handler.userJoinedHandler(event);
			}
		}
	}

	public static class UserLeftListener implements IListener<UserLeaveEvent>
	{
		@Override
		public void handle(UserLeaveEvent event)
		{
			String[] moduleList = Jeeves.getModuleList();

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

				if (Jeeves.isDisabled(event.getGuild().getID(), module) == true)
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
					Jeeves.debugException(e);
					continue;
				}

				handler.userLeftHandler(event);
			}
		}
	}

	public static class UserUpdateListener implements IListener<UserUpdateEvent>
	{
		@Override
		public void handle(UserUpdateEvent event)
		{
			List<IGuild> serverList = event.getClient().getGuilds();
			String[] moduleList = Jeeves.getModuleList();

			for (int serverIndex = 0; serverIndex < serverList.size(); serverIndex++)
			{
				IGuild server = serverList.get(serverIndex);
				IUser user = server.getUserByID(event.getNewUser().getID());

				if (user == null)
				{
					continue;
				}

				for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
				{
					BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

					if (Jeeves.isDisabled(server.getID(), module) == true)
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
						Jeeves.debugException(e);
						continue;
					}

					handler.userUpdateHandler(server, event);
				}
			}
		}
	}

	public static class UserPresenceListener implements IListener<PresenceUpdateEvent>
	{
		@Override
		public void handle(PresenceUpdateEvent event)
		{
			List<IGuild> serverList = event.getClient().getGuilds();
			String[] moduleList = Jeeves.getModuleList();

			for (int serverIndex = 0; serverIndex < serverList.size(); serverIndex++)
			{
				IGuild server = serverList.get(serverIndex);
				IUser user = server.getUserByID(event.getUser().getID());

				if (user == null)
				{
					continue;
				}

				for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
				{
					BotModule module = Jeeves.getModule(moduleList[moduleIndex]);

					if (Jeeves.isDisabled(server.getID(), module) == true)
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
						Jeeves.debugException(e);
						continue;
					}

					handler.presenceUpdateHandler(server, event);
				}
			}
		}
	}

	public static class GuildCreateListener implements IListener<GuildCreateEvent>
	{
		@Override
		public void handle(GuildCreateEvent event)
		{
			System.out.println("Creating default config for " + event.getGuild().getName());
			Internal internal = new Internal();

			try
			{
				Jeeves.checkConfig(event.getGuild().getID(), internal.getDefaultConfig());
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

	private static void handleMessage(IMessage message)
	{
		if (Jeeves.isIgnored(message.getChannel()) == true)
		{
			return;
		}

		if (Jeeves.isIgnored(message.getGuild().getID(), message.getAuthor()) == true)
		{
			return;
		}

		String messageContent = message.getContent();
		IUser botUser = message.getClient().getOurUser();
		int cutLength = 0;
		String commandPrefix = (String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "commandPrefix");

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

		while ((messageParts.length > 0) && (messageParts[0].length() == 0))
		{
			String[] tmpParts = new String[messageParts.length - 1];

			for (int x = 1; x < messageParts.length; x++)
			{
				tmpParts[x - 1] = messageParts[x];
			}
			
			messageParts = tmpParts;
		}

		if (messageParts.length == 0)
		{
			System.out.println("Empty");
			return;
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

			if (Jeeves.isDisabled(message.getGuild().getID(), module) == true)
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
					Jeeves.checkConfig(message.getGuild().getID(), module.getDefaultConfig());
				}
				catch (ParserConfigurationException | TransformerException e)
				{
					Jeeves.debugException(e);
					return;
				}

				String discordId = module.getDiscordId();

				if ((discordId != null) && (discordId != message.getGuild().getID()))
				{
					Jeeves.sendMessage(message.getChannel(), "This command is not available on this Discord.");
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
					String ownerId = "";

					try
					{
						ownerId = message.getClient().getApplicationOwner().getID();
					}
					catch (DiscordException e)
					{
						Jeeves.debugException(e);
					}

					if (message.getAuthor().getID().equals(ownerId) == false)
					{
						Jeeves.sendMessage(message.getChannel(), "You are not permitted to execute this command.");
						return;
					}
				}

				Permissions[] permissionList = command.permissions();

				if (permissionList != null)
				{
					EnumSet<Permissions> userPermissions = message.getAuthor().getPermissionsForGuild(message.getGuild());

					for (int permissionIndex = 0; permissionIndex < permissionList.length; permissionIndex++)
					{
						if (userPermissions.contains(permissionList[permissionIndex]) == false)
						{
							Jeeves.sendMessage(message.getChannel(), "You are not permitted to execute this command.");
							return;
						}
					}
				}

				System.out.println("Executing " + command.getClass().getSimpleName() + " for " + message.getAuthor().getName() + " (" + message.getGuild().getName() + ": " + message.getChannel().getName() + ")");
				command.execute(message, arguments);
				//TaskHandler executor = new TaskHandler(message, command, arguments);
				//executor.start();
			}
		}
	}
}
