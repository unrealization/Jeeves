package me.unrealization.jeeves.modules;

import java.io.IOException;
import java.text.DecimalFormat;

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
		this.version = "0.9.0";

		this.commandList = new String[17];
		this.commandList[0] = "GetUseEdsmBetaServer";
		this.commandList[1] = "SetUseEdsmBetaServer";
		this.commandList[2] = "Register";
		this.commandList[3] = "Unregister";
		this.commandList[4] = "EdsmUser";
		this.commandList[5] = "EDStatus";
		this.commandList[6] = "Locate";
		this.commandList[7] = "SysCoords";
		this.commandList[8] = "CmdrCoords";
		this.commandList[9] = "Distance";
		this.commandList[10] = "SystemSphere";
		this.commandList[11] = "SystemCube";
		this.commandList[12] = "Route";
		this.commandList[13] = "SystemInfo";
		this.commandList[14] = "BodyInfo";
		this.commandList[15] = "StationInfo";
		this.commandList[16] = "FactionInfo";

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

	private static String calculateDistance(EdsmModels.SystemInfo.Coordinates leftCoords, EdsmModels.SystemInfo.Coordinates rightCoords)
	{
		double xDistSqr = Math.pow(Double.parseDouble(leftCoords.x) - Double.parseDouble(rightCoords.x), 2);
		double yDistSqr = Math.pow(Double.parseDouble(leftCoords.y) - Double.parseDouble(rightCoords.y), 2);
		double zDistSqr = Math.pow(Double.parseDouble(leftCoords.z) - Double.parseDouble(rightCoords.z), 2);
		double distance = Math.sqrt(xDistSqr + yDistSqr + zDistSqr);
		String distanceString = new DecimalFormat("#.##").format(distance);
		return distanceString;
	}

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

	public static class EdsmUser extends BotCommand
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
			String output = "[username]";
			return output;
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

	public static class EDStatus extends BotCommand
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

			if (data == null)
			{
				MessageQueue.sendMessage(message.getChannel(), systemName + " cannot be found on EDSM.");
				return;
			}

			String output = "System: " + data.name + " [ " + data.coords.x + " : " + data.coords.y + " : " + data.coords.z + " ]";
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

	public static class Distance extends BotCommand
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
			String output = "[commander|system] : [commander|system]";
			return output;
		}

		@Override
		public void execute(IMessage message, String argumentString)
		{
			String[] arguments = Jeeves.splitArguments(argumentString);
			String[] searchNames = new String[2];

			for (int index = 0; index < searchNames.length; index++)
			{
				try
				{
					searchNames[index] = arguments[index];
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					//Jeeves.debugException(e);
					searchNames[index] = "";
				}

				if (searchNames[index].isEmpty() == true)
				{
					String userIdString = Long.toString(message.getAuthor().getLongID());

					if (Edsm.edsmUserList.hasKey(userIdString) == false)
					{
						MessageQueue.sendMessage(message.getChannel(), "You need to provide a commander or system name or register your EDSM username.");
						return;
					}

					searchNames[index] = (String)Edsm.edsmUserList.getValue(userIdString);
				}
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getLongID());
			EdsmModels.SystemInfo systemInfo[] = new EdsmModels.SystemInfo[2];

			for (int index = 0; index < searchNames.length; index++)
			{
				EdsmModels.SystemInfo systemData;

				try
				{
					systemData = edsmApi.getSystemInfo(searchNames[index]);
				}
				catch (IOException e)
				{
					Jeeves.debugException(e);
					MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
					return;
				}

				if (systemData != null)
				{
					systemInfo[index] = systemData;
					continue;
				}

				EdsmModels.CommanderLocation cmdrData;

				try
				{
					cmdrData = edsmApi.getCommanderLocation(searchNames[index]);
				}
				catch (IOException e)
				{
					Jeeves.debugException(e);
					MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
					return;
				}

				if (cmdrData == null)
				{
					MessageQueue.sendMessage(message.getChannel(), searchNames[index] + " cannot be found on EDSM.");
					return;
				}

				if (cmdrData.system == null)
				{
					switch (cmdrData.msgnum)
					{
					case "100":
						MessageQueue.sendMessage(message.getChannel(), searchNames[index] + " cannot be located.");
						return;
					case "203":
						MessageQueue.sendMessage(message.getChannel(), searchNames[index] + " does not seem to be using EDSM.");
						return;
					default:
						MessageQueue.sendMessage(message.getChannel(), cmdrData.msg);
						return;
					}
				}

				systemInfo[index] = new EdsmModels.SystemInfo();
				systemInfo[index].name = cmdrData.system;
				systemInfo[index].coords = cmdrData.coordinates;
			}

			String distance = Edsm.calculateDistance(systemInfo[0].coords, systemInfo[1].coords);
			String output = "The distance between " + systemInfo[0].name + " and " + systemInfo[0].name + " is " + distance + " ly.";
			MessageQueue.sendMessage(message.getChannel(), output);
		}
	}

	public static class SystemSphere extends BotCommand
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
			String output = "<system> [ : <radius> ]";
			return output;
		}

		@Override
		public void execute(IMessage message, String argumentString)
		{
			String[] arguments = Jeeves.splitArguments(argumentString);

			if ((arguments.length == 0) || (arguments[0].isEmpty() == true))
			{
				MessageQueue.sendMessage(message.getChannel(), "You need to provide a system name.");
				return;
			}

			String systemName = arguments[0];
			String radius = null;

			if (arguments.length == 2)
			{
				radius = arguments[1];

				try
				{
					Float.parseFloat(radius);
				}
				catch (NumberFormatException e)
				{
					Jeeves.debugException(e);
					MessageQueue.sendMessage(message.getChannel(), "Invalid value for radius.");
					return;
				}
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getLongID());
			EdsmModels.SystemInfo centerData;

			try
			{
				centerData = edsmApi.getSystemInfo(Edsm.sanitizeString(systemName));
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			if (centerData == null)
			{
				MessageQueue.sendMessage(message.getChannel(), systemName + " cannot be found on EDSM.");
			}

			EdsmModels.SystemInfo[] data;

			try
			{
				data = edsmApi.getSystemSphere(Edsm.sanitizeString(systemName), radius);
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			if (data.length == 1)
			{
				MessageQueue.sendMessage(message.getChannel(), "No systems found near " + systemName);
				return;
			}

			String output = "";

			for (int index = 0; index < data.length; index++)
			{
				if (data[index].name.toLowerCase().equals(systemName.toLowerCase()))
				{
					continue;
				}

				output += data[index].name + " (Distance: " + Edsm.calculateDistance(centerData.coords, data[index].coords) + " ly)\n";
			}

			MessageQueue.sendMessage(message.getChannel(), output);
		}
	}

	public static class SystemCube extends BotCommand
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
			String output = "<system> [ : <size> ]";
			return output;
		}

		@Override
		public void execute(IMessage message, String argumentString)
		{
			String[] arguments = Jeeves.splitArguments(argumentString);

			if ((arguments.length == 0) || (arguments[0].isEmpty() == true))
			{
				MessageQueue.sendMessage(message.getChannel(), "You need to provide a system name.");
				return;
			}

			String systemName = arguments[0];
			String size = null;

			if (arguments.length == 2)
			{
				size = arguments[1];

				try
				{
					Float.parseFloat(size);
				}
				catch (NumberFormatException e)
				{
					Jeeves.debugException(e);
					MessageQueue.sendMessage(message.getChannel(), "Invalid value for size.");
					return;
				}
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getLongID());
			EdsmModels.SystemInfo centerData;

			try
			{
				centerData = edsmApi.getSystemInfo(Edsm.sanitizeString(systemName));
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			if (centerData == null)
			{
				MessageQueue.sendMessage(message.getChannel(), systemName + " cannot be found on EDSM.");
			}

			EdsmModels.SystemInfo[] data;

			try
			{
				data = edsmApi.getSystemCube(Edsm.sanitizeString(systemName), size);
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			if (data.length == 1)
			{
				MessageQueue.sendMessage(message.getChannel(), "No systems found near " + systemName);
				return;
			}

			String output = "";

			for (int index = 0; index < data.length; index++)
			{
				if (data[index].name.toLowerCase().equals(systemName.toLowerCase()))
				{
					continue;
				}

				output += data[index].name + " (Distance: " + Edsm.calculateDistance(centerData.coords, data[index].coords) + " ly)\n";
			}

			MessageQueue.sendMessage(message.getChannel(), output);
		}
	}

	public static class Route extends BotCommand
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
			String output = "<origin> : <destination> : <jumprange>";
			return output;
		}

		@Override
		public void execute(IMessage message, String argumentString)
		{
			String[] arguments = Jeeves.splitArguments(argumentString);

			if (arguments.length < 3)
			{
				MessageQueue.sendMessage(message.getChannel(), "Insufficient amount of parameters.\n" + this.getParameters());
				return;
			}

			String origin = arguments[0];
			String destination = arguments[1];
			String jumpRange = arguments[2];

			try
			{
				Float.parseFloat(jumpRange);
			}
			catch (NumberFormatException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "Invalid value for jump range.");
				return;
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getLongID());
			EdsmModels.SystemInfo originInfo;

			try
			{
				originInfo = edsmApi.getSystemInfo(Edsm.sanitizeString(origin));
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			if (originInfo == null)
			{
				MessageQueue.sendMessage(message.getChannel(), origin + " cannot be found on EDSM.");
				return;
			}

			EdsmModels.SystemInfo destinationInfo;

			try
			{
				destinationInfo = edsmApi.getSystemInfo(Edsm.sanitizeString(destination));
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			if (destinationInfo == null)
			{
				MessageQueue.sendMessage(message.getChannel(), destination + " cannot be found on EDSM.");
			}

			EdsmModels.SystemInfo currentOriginInfo = originInfo;
			String currentDistanceString = Edsm.calculateDistance(originInfo.coords, destinationInfo.coords);
			float currentDistance = Float.parseFloat(currentDistanceString);
			int jumpNo = 0;
			String output = Integer.toString(jumpNo) + ": " + originInfo.name + " (Jump Distance: 0 ly) (Distance to " + destinationInfo.name + ": " + currentDistanceString + " ly)\n";
			boolean abortRouting = false;

			while ((currentOriginInfo.name.equals(destinationInfo.name) == false) && (abortRouting == false))
			{
				EdsmModels.SystemInfo[] systemBubble;

				try
				{
					systemBubble = edsmApi.getSystemSphere(Edsm.sanitizeString(currentOriginInfo.name), jumpRange);
				}
				catch (IOException e)
				{
					Jeeves.debugException(e);
					MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
					return;
				}

				EdsmModels.SystemInfo nextJump = null;
				float nextJumpDistance = Float.POSITIVE_INFINITY;

				for (int index = 0; index < systemBubble.length; index++)
				{
					String distanceString = Edsm.calculateDistance(systemBubble[index].coords, destinationInfo.coords);
					float distance = Float.parseFloat(distanceString);

					if (distance > currentDistance)
					{
						continue;
					}

					if ((nextJump != null) && (distance > nextJumpDistance))
					{
						continue;
					}

					nextJump = systemBubble[index];
					nextJumpDistance = distance;
				}

				if (nextJump == null)
				{
					output += "Unable to find the next jump.\n";
					abortRouting = true;
					continue;
				}

				String jumpDistance = Edsm.calculateDistance(currentOriginInfo.coords, nextJump.coords);
				currentOriginInfo = nextJump;
				currentDistance = nextJumpDistance;
				jumpNo++;
				output += Integer.toString(jumpNo) + ": " + currentOriginInfo.name + " (Jump Distance: " + jumpDistance + " ly) (Distance to " + destinationInfo.name + ": " + Float.toString(currentDistance) + " ly)\n";
			}

			if (abortRouting == false)
			{
				output = "Total number of jumps: " + jumpNo + "\n" + output;
			}

			MessageQueue.sendMessage(message.getChannel(), output);
		}
	}

	public static class SystemInfo extends BotCommand
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
				MessageQueue.sendMessage(message.getChannel(), "You need to supply as system name.");
				return;
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getLongID());
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

			if (data == null)
			{
				MessageQueue.sendMessage(message.getChannel(), systemName + " cannot be found on EDSM.");
			}

			String output = "System: " + data.name + "\n";
			output += "Galactic Coordinates: X: " + data.coords.x + " Y: " + data.coords.y + " Z: " + data.coords.z + "\n";

			if (data.requirePermit.equals("true") == true)
			{
				output += "Required Permit: " + data.permitName + "\n";
			}

			if (data.primaryStar != null)
			{
				output += "\n";
				output += "Primary Star\n";
				output += "Name: " + data.primaryStar.name + "\n";
				output += "Type: " + data.primaryStar.type + "\n";
				output += "Is Scoopable: ";

				if (data.primaryStar.isScoopable.equals("true") == true)
				{
					output += "yes";
				}
				else
				{
					output += "no";
				}
			}

			MessageQueue.sendMessage(message.getChannel(), output);
		}
	}

	public static class BodyInfo extends BotCommand
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
			String output = "<system> [ : <body> ]";
			return output;
		}

		@Override
		public void execute(IMessage message, String argumentString)
		{
			String[] arguments = Jeeves.splitArguments(argumentString);

			if ((arguments.length == 0) || (arguments[0].isEmpty() == true))
			{
				MessageQueue.sendMessage(message.getChannel(), "You need to provide a system name.");
				return;
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getLongID());
			String systemName = arguments[0];
			String bodyName = null;

			if (arguments.length == 2)
			{
				bodyName = arguments[1];
			}

			EdsmModels.SystemBodies data;

			try
			{
				data = edsmApi.getSystemBodies(Edsm.sanitizeString(systemName));
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			if (data == null)
			{
				MessageQueue.sendMessage(message.getChannel(), systemName + " cannot be found on EDSM.");
				return;
			}

			String output = "System: " + data.name + "\n";
			boolean bodyNotFound = true;

			for (int index = 0; index < data.bodies.length; index++)
			{
				if (bodyName != null)
				{
					if ((data.bodies[index].name.toLowerCase().equals(bodyName.toLowerCase()) == false) && (data.bodies[index].name.toLowerCase().equals(systemName.toLowerCase() + " " + bodyName.toLowerCase()) == false))
					{
						continue;
					}
					else
					{
						bodyNotFound = false;
					}
				}

				output += "\n";
				output += "Body: " + data.bodies[index].name + "\n";

				if (data.bodies[index].type.equals("Star"))
				{
					output += "Type: " + data.bodies[index].subType + "\n";
				}
				else
				{
					output += "Type: " + data.bodies[index].type + " (" + data.bodies[index].subType + ")\n";
				}

				output += "Distance from Arrival: " + data.bodies[index].distanceToArrival + " ls\n";

				if (bodyName != null)
				{
					//add all the details!
				}
			}

			if ((bodyName != null) && (bodyNotFound == true))
			{
				MessageQueue.sendMessage(message.getChannel(), "Cannot find the body " + bodyName);
				return;
			}

			MessageQueue.sendMessage(message.getChannel(), output);
		}
	}

	public static class StationInfo extends BotCommand
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
			String output = "<system> [ : <station> ]";
			return output;
		}

		@Override
		public void execute(IMessage message, String argumentString)
		{
			String[] arguments = Jeeves.splitArguments(argumentString);

			if ((arguments.length == 0) || (arguments[0].isEmpty() == true))
			{
				MessageQueue.sendMessage(message.getChannel(), "You need to provide a system name.");
				return;
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getLongID());
			String systemName = arguments[0];
			String stationName = null;

			if (arguments.length == 2)
			{
				stationName = arguments[1];
			}

			EdsmModels.SystemStations data;

			try
			{
				data = edsmApi.getSystemStations(Edsm.sanitizeString(systemName));
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			if (data == null)
			{
				MessageQueue.sendMessage(message.getChannel(), systemName + " cannot be found on EDSM.");
				return;
			}

			String output = "System: " + data.name + "\n";
			boolean stationNotFound = true;

			for (int index = 0; index < data.stations.length; index++)
			{
				if (stationName != null)
				{
					if (data.stations[index].name.toLowerCase().equals(stationName.toLowerCase()) == false)
					{
						continue;
					}
					else
					{
						stationNotFound = false;
					}
				}

				output += "\n";
				output += "Station: " + data.stations[index].name + "\n";
				output += "Type: " + data.stations[index].type + "\n";

				if (stationName != null)
				{
					//add all the details!
				}
			}

			if ((stationName != null) && (stationNotFound == true))
			{
				MessageQueue.sendMessage(message.getChannel(), "Cannot find the station " + stationName);
				return;
			}

			MessageQueue.sendMessage(message.getChannel(), output);
		}
	}

	public static class FactionInfo extends BotCommand
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
			String output = "<system> [ : <faction> ]";
			return output;
		}

		@Override
		public void execute(IMessage message, String argumentString)
		{
			String[] arguments = Jeeves.splitArguments(argumentString);

			if ((arguments.length == 0) || (arguments[0].isEmpty() == true))
			{
				MessageQueue.sendMessage(message.getChannel(), "You need to provide a system name.");
				return;
			}

			EdsmApi edsmApi = Edsm.getApiObject(message.getGuild().getLongID());
			String systemName = arguments[0];
			String factionName = null;

			if (arguments.length == 2)
			{
				factionName = arguments[1];
			}

			EdsmModels.SystemFactions data;

			try
			{
				data = edsmApi.getSystemFactions(Edsm.sanitizeString(systemName));
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			if (data == null)
			{
				MessageQueue.sendMessage(message.getChannel(), systemName + " cannot be found on EDSM.");
				return;
			}

			String output = "System: " + data.name + "\n";
			boolean factionNotFound = true;

			for (int index = 0; index < data.factions.length; index++)
			{
				if (factionName != null)
				{
					if (data.factions[index].name.toLowerCase().equals(factionName.toLowerCase()) == false)
					{
						continue;
					}
					else
					{
						factionNotFound = false;
					}
				}

				output += "\n";
				output += "Faction: " + data.factions[index].name + "\n";

				try
				{
					float influence = Float.parseFloat(data.factions[index].influence) * 100;
					String influenceString = new DecimalFormat("#.##").format(influence);
					output += "Influence: " + influenceString + " %\n";
				}
				catch (NumberFormatException e)
				{
					Jeeves.debugException(e);
				}

				output += "State: " + data.factions[index].state + "\n";

				if (factionName != null)
				{
					//add all the details!
				}
			}

			if ((factionName != null) && (factionNotFound == true))
			{
				MessageQueue.sendMessage(message.getChannel(), "Cannot find the faction " + factionName);
				return;
			}

			MessageQueue.sendMessage(message.getChannel(), output);
		}
	}
}
