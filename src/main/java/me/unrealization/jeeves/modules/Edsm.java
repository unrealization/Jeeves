package me.unrealization.jeeves.modules;

import java.io.IOException;

import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.jsonModels.EdsmModels;
import me.unrealization.jeeves.apis.EdsmApi;
import sx.blah.discord.handle.obj.IMessage;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;

public class Edsm extends BotModule
{
	public Edsm()
	{
		this.version = "0.2";

		this.commandList = new String[3];
		this.commandList[0] = "GetEDStatus";
		this.commandList[1] = "Locate";
		this.commandList[2] = "SysCoords";

		this.defaultConfig.put("edsmUseBetaServer", "1");
	}

	private String sanitizeString(String input)
	{
		String output = "";
		return output;
	}

	private String desanitizeString(String input)
	{
		String output = "";
		return output;
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
			EdsmApi edsmApi = new EdsmApi();
			edsmApi.setUseBetaServer(false);
			EdsmModels.EDStatus data;

			try
			{
				data = edsmApi.getEDStatus();
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				Jeeves.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			Jeeves.sendMessage(message.getChannel(), "Elite: Dangerous Server Status: " + data.message + "\nLast Update: " + data.lastUpdate);
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
				Jeeves.sendMessage(message.getChannel(), "You need to provide a commander name.");
				return;
			}

			EdsmApi edsmApi = new EdsmApi();
			edsmApi.setUseBetaServer(false);
			EdsmModels.CommanderLocation data;

			try
			{
				data = edsmApi.getCommanderLocation(commanderName);
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				Jeeves.sendMessage(message.getChannel(), "EDSM communication error.");
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

			Jeeves.sendMessage(message.getChannel(), output);
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
			// TODO Auto-generated method stub
			String systemName = String.join(" ", arguments).trim();

			if (systemName.isEmpty() == true)
			{
				Jeeves.sendMessage(message.getChannel(), "You need to provide a system name.");
				return;
			}

			EdsmApi edsmApi = new EdsmApi();
			edsmApi.setUseBetaServer(false);
			EdsmModels.SystemCoordinates data;

			try
			{
				data = edsmApi.getSystemCoordinates(systemName);
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				Jeeves.sendMessage(message.getChannel(), "EDSM communication error.");
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

			Jeeves.sendMessage(message.getChannel(), output);
		}
	}
}
