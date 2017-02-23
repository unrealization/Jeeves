package bot;

import interfaces.BotCommand;
import interfaces.BotModule;
import interfaces.NewUserHandler;
import interfaces.PresenceUpdateHandler;
import interfaces.UserUpdateHandler;

import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MentionEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.events.UserUpdateEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

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
			dispatcher.registerListener(new NewUserListener());
			dispatcher.registerListener(new UserUpdateListener());
			dispatcher.registerListener(new UserPresenceListener());

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
			String messageContent = message.getContent();

			if (messageContent.startsWith(Jeeves.serverConfig.getValue(message.getGuild().getID(), "commandPrefix")))
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
			String messageContent = message.getContent();
			IUser botUser = event.getClient().getOurUser();

			if ((messageContent.startsWith(botUser.mention(true))) || (messageContent.startsWith(botUser.mention(false))))
			{
				DiscordEventHandlers.handleMessage(message);
			}
		}
	}

	public static class NewUserListener implements IListener<UserJoinEvent>
	{
		@Override
		public void handle(UserJoinEvent event)
		{
			Set<String> moduleSet = Jeeves.modules.keySet();
			String[] moduleList = moduleSet.toArray(new String[moduleSet.size()]);

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				NewUserHandler module;

				try
				{
					module = (NewUserHandler)Jeeves.modules.get(moduleList[moduleIndex]);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					continue;
				}

				module.newUserHandler(event);
			}
		}
	}

	public static class UserUpdateListener implements IListener<UserUpdateEvent>
	{
		@Override
		public void handle(UserUpdateEvent event)
		{
			Set<String> moduleSet = Jeeves.modules.keySet();
			String[] moduleList = moduleSet.toArray(new String[moduleSet.size()]);

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				UserUpdateHandler module;

				try
				{
					module = (UserUpdateHandler)Jeeves.modules.get(moduleList[moduleIndex]);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					continue;
				}

				module.userUpdateHandler(event);
			}
		}
	}

	public static class UserPresenceListener implements IListener<PresenceUpdateEvent>
	{
		@Override
		public void handle(PresenceUpdateEvent event)
		{
			Set<String> moduleSet = Jeeves.modules.keySet();
			String[] moduleList = moduleSet.toArray(new String[moduleSet.size()]);

			for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
			{
				PresenceUpdateHandler module;

				try
				{
					module = (PresenceUpdateHandler)Jeeves.modules.get(moduleList[moduleIndex]);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					continue;
				}

				module.presenceUpdateHandler(event);
			}
		}
	}

	private static void handleMessage(IMessage message)
	{
		String messageContent = message.getContent();
		IUser botUser = message.getClient().getOurUser();
		int cutLength = 0;

		if (messageContent.startsWith(Jeeves.serverConfig.getValue(message.getGuild().getID(), "commandPrefix")))
		{
			cutLength = Jeeves.serverConfig.getValue(message.getGuild().getID(), "commandPrefix").length();
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

		Set<String> moduleSet = Jeeves.modules.keySet();
		String[] moduleList = moduleSet.toArray(new String[moduleSet.size()]);

		for (int moduleIndex = 0; moduleIndex < moduleList.length; moduleIndex++)
		{
			BotModule module = Jeeves.modules.get(moduleList[moduleIndex]);
			String[] commandList = module.getCommands();

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
					e.printStackTrace();
					return;
				}

				System.out.println("Executing " + commandName + " for " + message.getAuthor().getName() + " (" + message.getGuild().getName() + ")");
				Class<?> commandClass;

				try
				{
					commandClass = Class.forName(module.getClass().getName() + "$" + commandList[commandIndex]);
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
					return;
				}

				BotCommand command;

				try
				{
					command = (BotCommand)commandClass.newInstance();
				}
				catch (InstantiationException | IllegalAccessException e)
				{
					e.printStackTrace();
					return;
				}

				if (command == null)
				{
					System.out.println("Error");
				}

				command.execute(message, arguments);
				//TaskHandler executor = new TaskHandler(message, command, arguments);
				//executor.start();
			}
		}
	}
}
