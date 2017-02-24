package interfaces;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;

public interface BotCommand
{
	public String help();
	public String usage();
	public Permissions[] permissions();
	public boolean owner();
	public void execute(IMessage message, String[] arguments);
}
