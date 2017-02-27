package me.unrealization.jeeves.modules;

import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.events.UserLeaveEvent;
import sx.blah.discord.handle.impl.events.UserUpdateEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.handle.obj.Presences;
import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.PresenceUpdateHandler;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;
import me.unrealization.jeeves.interfaces.UserLeftHandler;
import me.unrealization.jeeves.interfaces.UserUpdateHandler;

public class UserLog implements BotModule, UserJoinedHandler, UserLeftHandler, PresenceUpdateHandler, UserUpdateHandler
{
	private String version = "0.8";
	private String[] commandList;
	private HashMap<String, Object> defaultConfig = new HashMap<String, Object>();

	public UserLog()
	{
		this.commandList = new String[2];
		this.commandList[0] = "GetUserLogChannel";
		this.commandList[1] = "SetUserLogChannel";
		this.defaultConfig.put("userLogChannel", "");
	}

	@Override
	public HashMap<String, Object> getDefaultConfig()
	{
		return this.defaultConfig;
	}

	@Override
	public String getHelp()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion()
	{
		return this.version;
	}

	@Override
	public String[] getCommands()
	{
		return this.commandList;
	}

	@Override
	public String getDiscordId()
	{
		return null;
	}

	@Override
	public boolean canDisable()
	{
		return true;
	}

	@Override
	public void userJoinedHandler(UserJoinEvent event)
	{
		String channelId = (String)Jeeves.serverConfig.getValue(event.getGuild().getID(), "userLogChannel");

		if (channelId.isEmpty() == true)
		{
			return;
		}

		IChannel channel = event.getGuild().getChannelByID(channelId);

		if (channel == null)
		{
			System.out.println("Invalid user log channel.");
			Jeeves.serverConfig.setValue(event.getGuild().getID(), "userLogChannel", "");

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

		Date now = new Date();
		Jeeves.sendMessage(channel, now.toString() + ": " + event.getUser().getName() + " has joined the server.");
	}

	@Override
	public void userLeftHandler(UserLeaveEvent event)
	{
		String channelId = (String)Jeeves.serverConfig.getValue(event.getGuild().getID(), "userLogChannel");

		if (channelId.isEmpty() == true)
		{
			return;
		}

		IChannel channel = event.getGuild().getChannelByID(channelId);

		if (channel == null)
		{
			System.out.println("Invalid user log channel.");
			Jeeves.serverConfig.setValue(event.getGuild().getID(), "userLogChannel", "");

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

		Date now = new Date();
		Jeeves.sendMessage(channel, now.toString() + ": " + event.getUser().getName() + " has left the server.");
	}

	@Override
	public void presenceUpdateHandler(IGuild server, PresenceUpdateEvent event)
	{
		String channelId = (String)Jeeves.serverConfig.getValue(server.getID(), "userLogChannel");

		if (channelId.isEmpty() == true)
		{
			return;
		}

		IChannel channel = server.getChannelByID(channelId);

		if (channel == null)
		{
			System.out.println("Invalid user log channel.");
			Jeeves.serverConfig.setValue(server.getID(), "userLogChannel", "");

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

		Presences oldPresence = event.getOldPresence();
		Presences newPresence = event.getNewPresence();

		if (newPresence.equals(oldPresence) == true)
		{
			return;
		}

		if ((oldPresence.equals(Presences.ONLINE) == true) && ((newPresence.equals(Presences.IDLE) == true) || (newPresence.equals(Presences.DND) == true)))
		{
			return;
		}

		if ((newPresence.equals(Presences.ONLINE) == true) && ((oldPresence.equals(Presences.IDLE) == true) || (oldPresence.equals(Presences.DND) == true)))
		{
			return;
		}

		Date now = new Date();
		Jeeves.sendMessage(channel, now.toString() + ": " + event.getUser().getName() + "'s status has changed from " + oldPresence.name() + " to " + newPresence.name());
	}


	@Override
	public void userUpdateHandler(IGuild server, UserUpdateEvent event)
	{
		// TODO Auto-generated method stub
		return;
	}

	public static class GetUserLogChannel implements BotCommand
	{
		@Override
		public String getHelp()
		{
			// TODO Auto-generated method stub
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
		public boolean owner()
		{
			return false;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String userLogChannelId = (String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "userLogChannel");

			if (userLogChannelId.isEmpty() == true)
			{
				Jeeves.sendMessage(message.getChannel(), "No user log channel has been set.");
				return;
			}

			IChannel userLogChannel = message.getGuild().getChannelByID(userLogChannelId);

			if (userLogChannel == null)
			{
				Jeeves.sendMessage(message.getChannel(), "A user log channel has been set, but it does not exist.");

				UserLog userLog = new UserLog();
				Jeeves.serverConfig.setValue(message.getGuild().getID(), "userLogChannel", userLog.getDefaultConfig().get("userLogChannel"));

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

			Jeeves.sendMessage(message.getChannel(), "The user log channel is: " + userLogChannel.getName());
		}
	}

	public static class SetUserLogChannel implements BotCommand
	{
		@Override
		public String getHelp()
		{
			// TODO Auto-generated method stub
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
		public boolean owner()
		{
			return false;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String channelName = String.join(" ", arguments).trim();
			IChannel userLogChannel = null;

			if (channelName.isEmpty() == true)
			{
				Jeeves.serverConfig.setValue(message.getGuild().getID(), "userLogChannel", "");
			}
			else
			{
				userLogChannel = Jeeves.findChannel(message.getGuild(), channelName);

				if (userLogChannel == null)
				{
					Jeeves.sendMessage(message.getChannel(), "Cannot find the channel " + channelName);
					return;
				}

				Jeeves.serverConfig.setValue(message.getGuild().getID(), "userLogChannel", userLogChannel.getID());
			}

			try
			{
				Jeeves.serverConfig.saveConfig();
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				Jeeves.debugException(e);
				Jeeves.sendMessage(message.getChannel(), "Cannot store the setting.");
				return;
			}

			if (userLogChannel == null)
			{
				Jeeves.sendMessage(message.getChannel(), "The user log channel has been cleared.");
			}
			else
			{
				Jeeves.sendMessage(message.getChannel(), "The user log channel has been set to: " + userLogChannel.getName());
			}
		}
	}
}
