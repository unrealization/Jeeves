package me.unrealization.jeeves.modules;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.MessageQueue;
import me.unrealization.jeeves.dataLists.EdsmUserList;
import me.unrealization.jeeves.jsonModels.EdsmModels;
import me.unrealization.jeeves.apis.EdsmApi;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;

public class Edsm extends BotModule
{
	private static EdsmUserList edsmUserList = null;

	public Edsm() throws ParserConfigurationException, SAXException
	{
		this.version = "0.5.1";

		this.commandList = new String[9];
		this.commandList[0] = "GetUseEdsmBetaServer";
		this.commandList[1] = "SetUseEdsmBetaServer";
		this.commandList[2] = "Register";
		this.commandList[3] = "Unregister";
		this.commandList[4] = "GetEdsmUser";
		this.commandList[5] = "GetEDStatus";
		this.commandList[6] = "Locate";
		this.commandList[7] = "SysCoords";
		this.commandList[8] = "CmdrCoords";

		this.defaultConfig.put("edsmUseBetaServer", "1");

		Edsm.edsmUserList = new EdsmUserList();
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

	private static EdsmApi getApiObject(long serverId)
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
		public void execute(IMessage message, String argumentString)
		{
			String useBetaServer = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "edsmUseBetaServer");

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
		public void execute(IMessage message, String useBetaServer)
		{
			if ((useBetaServer.equals("0") == false) && (useBetaServer.equals("1") == false))
			{
				MessageQueue.sendMessage(message.getChannel(), "Invalid value");
				return;
			}

			Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "edsmUseBetaServer", useBetaServer);

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

	public static class Register extends BotCommand
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
			String output = "<edsmUserName>";
			return output;
		}

		@Override
		public void execute(IMessage message, String edsmUserName)
		{
			if (edsmUserName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "You need to provide an EDSM username");
				return;
			}

			String userIdString = Long.toString(message.getAuthor().getLongID());
			Edsm.edsmUserList.setValue(userIdString, edsmUserName);

			try
			{
				Edsm.edsmUserList.saveConfig();
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "Cannot store the setting.");
				return;
			}

			MessageQueue.sendMessage(message.getChannel(), "Your EDSM username has been set to: " + edsmUserName);
		}
	}

	public static class Unregister extends BotCommand
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
		public void execute(IMessage message, String argumentString)
		{
			String userIdString = Long.toString(message.getAuthor().getLongID());

			if (Edsm.edsmUserList.hasKey(userIdString) == false)
			{
				MessageQueue.sendMessage(message.getChannel(), "You have not registered an EDSM username.");
				return;
			}

			Edsm.edsmUserList.removeValue(userIdString);
			MessageQueue.sendMessage(message.getChannel(), "Your EDSM username has been removed.");
		}
	}

	public static class GetEdsmUser extends BotCommand
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
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void execute(IMessage message, String userName)
		{
			IUser user;

			if (userName.isEmpty() == true)
			{
				user = message.getAuthor();
			}
			else
			{
				user = Jeeves.findUser(message.getGuild(), userName);

				if (user == null)
				{
					MessageQueue.sendMessage(message.getChannel(), "Cannot find the user " + userName);
					return;
				}
			}

			String userIdString = Long.toString(user.getLongID());

			if (Edsm.edsmUserList.hasKey(userIdString) == false)
			{
				if (userName.isEmpty() == true)
				{
					MessageQueue.sendMessage(message.getChannel(), "You have not registered an EDSM username.");
				}
				else
				{
					MessageQueue.sendMessage(message.getChannel(), user.getName() + " has not registered an EDSM username.");
				}

				return;
			}

			String edsmUserName = (String)Edsm.edsmUserList.getValue(userIdString);

			if (userName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "Your EDSM username is: " + edsmUserName);
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel(), "The EDSM username for " + user.getName() + " is: " + edsmUserName);
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
		public void execute(IMessage message, String argumentString)
		{
			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getLongID());
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
			String output = "[commander]";
			return output;
		}

		@Override
		public void execute(IMessage message, String commanderName)
		{
			if (commanderName.isEmpty() == true)
			{
				String userIdString = Long.toString(message.getAuthor().getLongID());

				if (Edsm.edsmUserList.hasKey(userIdString) == false)
				{
					MessageQueue.sendMessage(message.getChannel(), "You need to provide a commander name or register your EDSM username.");
					return;
				}

				commanderName = (String)Edsm.edsmUserList.getValue(userIdString);
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getLongID());
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
		public void execute(IMessage message, String systemName)
		{
			if (systemName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "You need to provide a system name.");
				return;
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getLongID());
			//EdsmModels.SystemCoordinates data;
			EdsmModels.SystemInfo data;

			try
			{
				data = edsmApi.getSystemInfo(Edsm.sanitizeString(systemName));
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

	public static class CmdrCoords extends BotCommand
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
			String output = "[commander]";
			return output;
		}

		@Override
		public void execute(IMessage message, String commanderName)
		{
			if (commanderName.isEmpty() == true)
			{
				String userIdString = Long.toString(message.getAuthor().getLongID());

				if (Edsm.edsmUserList.hasKey(userIdString) == false)
				{
					MessageQueue.sendMessage(message.getChannel(), "You need to provide a commander name or register your ESDM username.");
					return;
				}

				commanderName = (String)Edsm.edsmUserList.getValue(userIdString);
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getLongID());
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
				output = "System: " + data.system + " [ " + data.coordinates.x + " : " + data.coordinates.y + " : " + data.coordinates.z + " ]";
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
}
