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
		this.version = "0.1";

		this.commandList = new String[2];
		this.commandList[0] = "GetEDStatus";
		this.commandList[1] = "Locate";

		this.defaultConfig.put("edsmUseBetaServer", "1");
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
			// TODO Auto-generated method stub
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
				Jeeves.sendMessage(message.getChannel(), "Cannot retrieve the Elite: Dangerous server status.");
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
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String commanderName = String.join(" ", arguments).trim();

			if (commanderName.isEmpty() == true)
			{
				return;
			}

			EdsmApi edsmApi = new EdsmApi();
			edsmApi.setUseBetaServer(false);
			EdsmModels.CommanderLocation data;

			try
			{
				data = edsmApi.locateCommander(commanderName);
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				Jeeves.sendMessage(message.getChannel(), "Cannot retrieve the location of " + commanderName);
				return;
			}

			//TODO: More!
			Jeeves.sendMessage(message.getChannel(), commanderName + " was last seen in " + data.system);
		}
		
	}
}
