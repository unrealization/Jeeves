package me.unrealization.jeeves.modules;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.MessageQueue;
import me.unrealization.jeeves.jsonModels.EdsmModels;
import me.unrealization.jeeves.apis.EdsmApi;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;

public class Edsm extends BotModule
{
	public Edsm()
	{
		this.version = "0.2";

		this.commandList = new String[5];
		this.commandList[0] = "GetUseEdsmBetaServer";
		this.commandList[1] = "SetUseEdsmBetaServer";
		this.commandList[2] = "GetEDStatus";
		this.commandList[3] = "Locate";
		this.commandList[4] = "SysCoords";

		this.defaultConfig.put("edsmUseBetaServer", "1");
	}

	private static String sanitizeString(String input)
	{
		String output = input.replace(" ", "%20").replace("+", "%2B").replace("'", "%27");
		return output;
	}

	/*private static String desanitizeString(String input)
	{
		String output = input.replace("%20", " ").replace("%2B", "+").replace("%27", "'");
		return output;
	}*/

	private static EdsmApi getApiObject(String serverId)
	{
		String useBetaServer = (String)Jeeves.serverConfig.getValue(serverId, "edsmUseBetaServer");
		boolean useBeta = useBetaServer.equals("1");
		EdsmApi edsmApi = new EdsmApi();
		edsmApi.setUseBetaServer(useBeta);
		return edsmApi;
	}

	public static class GetUseEdsmBetaServer extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Check if the bot uses the EDSM beta server.";
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
		public void execute(IMessage message, String[] arguments)
		{
			String useBetaServer = (String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "edsmUseBetaServer");

			if (useBetaServer.equals("0") == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "The bot uses the EDSM live server.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel(), "The bot uses the EDSM beta server.");
			}
		}
	}

	public static class SetUseEdsmBetaServer extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Set whether or not the bot should use the EDSM beta server.";
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
		public void execute(IMessage message, String[] arguments)
		{
			String useBetaServer = String.join(" ", arguments).trim();

			if ((useBetaServer.equals("0") == false) && (useBetaServer.equals("1") == false))
			{
				MessageQueue.sendMessage(message.getChannel(), "Invalid value");
				return;
			}

			Jeeves.serverConfig.setValue(message.getGuild().getID(), "edsmUseBetaServer", useBetaServer);

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

			if (useBetaServer.equals("0") == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "The bot will now use the EDSM live server.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel(), "The bot will now use the EDSM beta server.");
			}
		}
	}

	public static class GetEDStatus extends BotCommand
	{
		@Override
		public String getHelp()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getParameters()
		{
			return null;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getID());
			EdsmModels.EDStatus data;

			try
			{
				data = edsmApi.getEDStatus();
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			MessageQueue.sendMessage(message.getChannel(), "Elite: Dangerous Server Status: " + data.message + "\nLast Update: " + data.lastUpdate);
		}
	}

	public static class Locate extends BotCommand
	{
		@Override
		public String getHelp()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getParameters()
		{
			String output = "<commander>";
			return output;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String commanderName = String.join(" ", arguments).trim();

			if (commanderName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "You need to provide a commander name.");
				return;
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getID());
			EdsmModels.CommanderLocation data;

			try
			{
				data = edsmApi.getCommanderLocation(Edsm.sanitizeString(commanderName));
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			String output;

			if (data.system != null)
			{
				output = commanderName + " was last seen in " + data.system;

				if (data.date != null)
				{
					output += " at " + data.date;
				}
			}
			else
			{
				switch (data.msgnum)
				{
				case "100":
					output = commanderName + " cannot be located.";
					break;
				case "203":
					output = commanderName + " does not seem to be using EDSM.";
					break;
				default:
					output = data.msg;
					break;
				}
			}

			MessageQueue.sendMessage(message.getChannel(), output);
		}
	}

	public static class SysCoords extends BotCommand
	{
		@Override
		public String getHelp()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getParameters()
		{
			String output = "<system>";
			return output;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String systemName = String.join(" ", arguments).trim();

			if (systemName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "You need to provide a system name.");
				return;
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getID());
			EdsmModels.SystemCoordinates data;

			try
			{
				data = edsmApi.getSystemCoordinates(Edsm.sanitizeString(systemName));
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			String output;

			if (data.name != null)
			{
				output = "System: " + data.name + " [ " + data.coords.x + " : " + data.coords.y + " : " + data.coords.z + " ]";
			}
			else
			{
				output = systemName + " cannot be found in EDSM.";
			}

			MessageQueue.sendMessage(message.getChannel(), output);
		}
	}
}
