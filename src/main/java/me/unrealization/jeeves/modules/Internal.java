package me.unrealization.jeeves.modules;

import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import me.unrealization.jeeves.bot.Jeeves;
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
		this.commandList = new String[11];
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
		public String help()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage()
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
		public String help()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage()
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
		public String help()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage()
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
				e.printStackTrace();
			}
		}
	}

	public static class GetDebugging implements BotCommand
	{
		@Override
		public String help()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage()
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
			// TODO Auto-generated method stub
			
		}
	}

	public static class SetDebugging implements BotCommand
	{
		@Override
		public String help()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage()
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
			// TODO Auto-generated method stub
			
		}
		
	}

	public static class GetCommandPrefix implements BotCommand
	{
		@Override
		public String help()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage()
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
		public String help()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage()
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
				e.printStackTrace();
				Jeeves.sendMessage(message.getChannel(), "Cannot store the setting.");
				return;
			}

			Jeeves.sendMessage(message.getChannel(), "The command prefix has been set to: " + commandPrefix);
		}
	}

	public static class GetRespondOnPrefix implements BotCommand
	{
		@Override
		public String help()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage()
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
		public String help()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage()
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
			String respondOnPrefix = String.join(" ", arguments);

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
				e.printStackTrace();
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
		public String help()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage()
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
		public String help()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage()
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
			String respondOnMention = String.join(" ", arguments);

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
				e.printStackTrace();
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
}
