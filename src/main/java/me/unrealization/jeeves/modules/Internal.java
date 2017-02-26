package me.unrealization.jeeves.modules;

import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import me.unrealization.jeeves.bot.Jeeves;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;

public class Internal implements BotModule
{
	private String version = Jeeves.version;
	private String[] commandList;
	private HashMap<String, Object> defaultConfig = new HashMap<String, Object>();

	public Internal()
	{
		this.commandList = new String[14];
		this.commandList[0] = "Version";
		this.commandList[1] = "Ping";
		this.commandList[2] = "Shutdown";
		this.commandList[3] = "GetDebugging";
		this.commandList[4] = "SetDebugging";
		this.commandList[5] = "GetCommandPrefix";
		this.commandList[6] = "SetCommandPrefix";
		this.commandList[7] = "GetRespondOnPrefix";
		this.commandList[8] = "SetRespondOnPrefix";
		this.commandList[9] = "GetRespondOnMention";
		this.commandList[10] = "SetRespondOnMention";
		this.commandList[11] = "GetIgnoredChannels";
		this.commandList[12] = "AddIgnoredChannel";
		this.commandList[13] = "RemoveIgnoredChannel";
		this.defaultConfig.put("commandPrefix", "!");
		this.defaultConfig.put("respondOnPrefix", "0");
		this.defaultConfig.put("respondOnMention", "1");
		this.defaultConfig.put("ignoredChannels", new String[0]);
		this.defaultConfig.put("ignoredUsers", new String[0]);
		this.defaultConfig.put("disabledModules", new String[0]);
	}

	@Override
	public HashMap<String, Object> getDefaultConfig()
	{
		return this.defaultConfig;
	}

	@Override
	public String getHelp()
	{
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
		return false;
	}

	public static class Ping implements BotCommand
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
			return null;
		}

		@Override
		public boolean owner()
		{
			return false;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			Jeeves.sendMessage(message.getChannel(), "Pong!");
		}
	}

	public static class Version implements BotCommand
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
			return null;
		}

		@Override
		public boolean owner()
		{
			return false;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			Jeeves.sendMessage(message.getChannel(), Jeeves.version);
		}
	}

	public static class Shutdown implements BotCommand
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
			return null;
		}

		@Override
		public boolean owner()
		{
			return true;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			Jeeves.sendMessage(message.getChannel(), "Good bye, cruel world.");

			try
			{
				message.getClient().logout();
			}
			catch (DiscordException e)
			{
				Jeeves.debugException(e);
			}
		}
	}

	public static class GetDebugging implements BotCommand
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
			return null;
		}

		@Override
		public boolean owner()
		{
			return true;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String debugging = (String)Jeeves.clientConfig.getValue("debugging");

			if (debugging.equals("0") == true)
			{
				Jeeves.sendMessage(message.getChannel(), "Debugging is disabled.");
			}
			else
			{
				Jeeves.sendMessage(message.getChannel(), "Debugging is enabled.");
			}
		}
	}

	public static class SetDebugging implements BotCommand
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
			return null;
		}

		@Override
		public boolean owner()
		{
			return true;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String debugging = String.join(" ", arguments).trim();

			if ((debugging.equals("0") == false) && (debugging.equals("1") == false))
			{
				Jeeves.sendMessage(message.getChannel(), "Invalid value");
			}

			Jeeves.clientConfig.setValue("debugging", debugging);

			try
			{
				Jeeves.clientConfig.saveConfig();
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				Jeeves.debugException(e);
				Jeeves.sendMessage(message.getChannel(), "Cannot store the setting.");
				return;
			}

			if (debugging.equals("0") == true)
			{
				Jeeves.sendMessage(message.getChannel(), "Debugging has been disabled.");
			}
			else
			{
				Jeeves.sendMessage(message.getChannel(), "Debugging has been enabled.");
			}
		}
	}

	public static class GetCommandPrefix implements BotCommand
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
			String commandPrefix = (String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "commandPrefix");
			Jeeves.sendMessage(message.getChannel(), "The command prefix is: " + commandPrefix);
		}
	}

	public static class SetCommandPrefix implements BotCommand
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
			// TODO Auto-generated method stub
			String commandPrefix = String.join(" ", arguments).trim();

			if (commandPrefix.isEmpty() == true)
			{
				Jeeves.sendMessage(message.getChannel(), "The command prefix cannot be empty.");
				return;
			}

			Jeeves.serverConfig.setValue(message.getGuild().getID(), "commandPrefix", commandPrefix);

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

			Jeeves.sendMessage(message.getChannel(), "The command prefix has been set to: " + commandPrefix);
		}
	}

	public static class GetRespondOnPrefix implements BotCommand
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
			String respondOnPrefix = (String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "respondOnPrefix");

			if (respondOnPrefix.equals("0") == true)
			{
				Jeeves.sendMessage(message.getChannel(), "The bot will not respond to the command prefix.");
			}
			else
			{
				Jeeves.sendMessage(message.getChannel(), "The bot will respond to the command prefix.");
			}
		}
	}

	public static class SetRespondOnPrefix implements BotCommand
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
			String respondOnPrefix = String.join(" ", arguments).trim();

			if ((respondOnPrefix.equals("0") == false) && (respondOnPrefix.equals("1") == false))
			{
				Jeeves.sendMessage(message.getChannel(), "Invalid value");
				return;
			}

			System.out.println(respondOnPrefix);
			Jeeves.serverConfig.setValue(message.getGuild().getID(), "respondOnPrefix", respondOnPrefix);

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

			if (respondOnPrefix.equals("0") == true)
			{
				Jeeves.sendMessage(message.getChannel(), "The bot will no longer respond to the command prefix.");
			}
			else
			{
				Jeeves.sendMessage(message.getChannel(), "The bot now responds to the command prefix.");
			}
		}
	}

	public static class GetRespondOnMention implements BotCommand
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
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String respondOnMention = (String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "respondOnMention");

			if (respondOnMention.equals("0") == true)
			{
				Jeeves.sendMessage(message.getChannel(), "The bot will not respond to mentions.");
			}
			else
			{
				Jeeves.sendMessage(message.getChannel(), "The bot will respond to mentions.");
			}
		}
	}

	public static class SetRespondOnMention implements BotCommand
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
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String respondOnMention = String.join(" ", arguments).trim();

			if ((respondOnMention.equals("0") == false) && (respondOnMention.equals("1") == false))
			{
				Jeeves.sendMessage(message.getChannel(), "Invalid value");
				return;
			}

			Jeeves.serverConfig.setValue(message.getGuild().getID(), "respondOnMention", respondOnMention);

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

			if (respondOnMention.equals("0") == true)
			{
				Jeeves.sendMessage(message.getChannel(), "The bot will no longer respond to mentions.");
			}
			else
			{
				Jeeves.sendMessage(message.getChannel(), "The bot now responds to mentions.");
			}
		}
	}

	public static class GetIgnoredChannels implements BotCommand
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
			Object ignoredChannels = Jeeves.serverConfig.getValue(message.getGuild().getID(), "ignoredChannels");

			if (ignoredChannels.getClass() == String.class)
			{
				Jeeves.sendMessage(message.getChannel(), "No channels are being ignored.");
				return;
			}

			String[] ignoredChannelList = (String[])ignoredChannels;

			if (ignoredChannelList.length == 0)
			{
				Jeeves.sendMessage(message.getChannel(), "No channels are being ignored.");
				return;
			}

			String output = "The following channels are being ignored:\n\n";

			for (int channelIndex = 0; channelIndex < ignoredChannelList.length; channelIndex++)
			{
				IChannel channel = message.getGuild().getChannelByID(ignoredChannelList[channelIndex]);
				output += channel.getName() + "\n";
			}

			Jeeves.sendMessage(message.getChannel(), output);
		}
	}

	public static class AddIgnoredChannel implements BotCommand
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

			if (channelName.isEmpty() == true)
			{
				Jeeves.sendMessage(message.getChannel(), "You need to provide a channel name.");
				return;
			}

			IChannel channel = Jeeves.findChannel(message.getGuild(), channelName);

			if (channel == null)
			{
				Jeeves.sendMessage(message.getChannel(), "Cannot find the channel " + channelName);
				return;
			}

			Object ignoredChannels = Jeeves.serverConfig.getValue(message.getGuild().getID(), "ignoredChannels");
			String[] ignoredChannelList;

			if (ignoredChannels.getClass() == String.class)
			{
				ignoredChannelList = new String[0];
			}
			else
			{
				ignoredChannelList = (String[])ignoredChannels;
			}

			String[] tmpIgnoredChannelList = new String[ignoredChannelList.length + 1];

			for (int channelIndex = 0; channelIndex < ignoredChannelList.length; channelIndex++)
			{
				tmpIgnoredChannelList[channelIndex] = ignoredChannelList[channelIndex];
			}

			tmpIgnoredChannelList[ignoredChannelList.length] = channel.getID();
			ignoredChannelList = tmpIgnoredChannelList;
			Jeeves.serverConfig.setValue(message.getGuild().getID(), "ignoredChannels", ignoredChannelList);

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

			Jeeves.sendMessage(message.getChannel(), "The following channel is now being ignored: " + channel.getName());
		}
	}

	public static class RemoveIgnoredChannel implements BotCommand
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

			if (channelName.isEmpty() == true)
			{
				Jeeves.sendMessage(message.getChannel(), "You need to provide a channel name.");
				return;
			}

			IChannel channel = Jeeves.findChannel(message.getGuild(), channelName);

			if (channel == null)
			{
				Jeeves.sendMessage(message.getChannel(), "Cannot find the channel " + channelName);
				return;
			}

			Object ignoredChannels = Jeeves.serverConfig.getValue(message.getGuild().getID(), "ignoredChannels");

			if (ignoredChannels.getClass() == String.class)
			{
				Jeeves.sendMessage(message.getChannel(), "No channels are being ignored.");
			}


			String[] ignoredChannelList = (String[])ignoredChannels;
			String[] tmpIgnoredChannelList = new String[ignoredChannelList.length - 1];
			int tmpIndex = 0;
			boolean removed = false;

			for (int channelIndex = 0; channelIndex < ignoredChannelList.length; channelIndex++)
			{
				if (channel.getID().equals(ignoredChannelList[channelIndex]) == true)
				{
					removed = true;
					continue;
				}

				if (tmpIndex == tmpIgnoredChannelList.length)
				{
					break;
				}

				tmpIgnoredChannelList[tmpIndex] = ignoredChannelList[channelIndex];
				tmpIndex++;
			}

			if (removed == false)
			{
				Jeeves.sendMessage(message.getChannel(), "The channel " + channel.getName() + " is not being ignored.");
				return;
			}

			ignoredChannelList = tmpIgnoredChannelList;
			Jeeves.serverConfig.setValue(message.getGuild().getID(), "ignoredChannels", ignoredChannelList);

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

			Jeeves.sendMessage(message.getChannel(), "The following channel is no longer being ignored: " + channel.getName());
		}
	}
}
