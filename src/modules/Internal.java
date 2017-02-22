package modules;

import java.util.HashMap;

import bot.Jeeves;
import sx.blah.discord.handle.obj.IMessage;
import interfaces.BotCommand;
import interfaces.BotModule;

public class Internal implements BotModule
{
	private String version = Jeeves.version;
	private String[] commandList;

	public Internal()
	{
		this.commandList = new String[2];
		this.commandList[0] = "Version";
		this.commandList[1] = "Ping";
	}

	@Override
	public HashMap<String, String> getDefaultConfig()
	{
		// TODO Auto-generated method stub
		return null;
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
		public String[] permissions()
		{
			// TODO Auto-generated method stub
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
		public String help() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String usage() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] permissions() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean owner() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void execute(IMessage message, String[] arguments) {
			/*try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}*/

			Jeeves.sendMessage(message.getChannel(), Jeeves.version);
		}
	}
}
