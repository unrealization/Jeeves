package interfaces;

import sx.blah.discord.handle.obj.IMessage;

public interface BotCommand
{
	public String help();
	public String usage();
	public String[] permissions();
	public boolean owner();
	public void execute(IMessage message, String[] arguments);
}
