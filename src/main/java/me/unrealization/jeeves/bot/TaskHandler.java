package me.unrealization.jeeves.bot;

import sx.blah.discord.handle.obj.IMessage;
import me.unrealization.jeeves.interfaces.BotCommand;

public class TaskHandler implements Runnable
{
	private IMessage message;
	private BotCommand command;
	private String[] arguments;

	public TaskHandler(IMessage message, BotCommand command, String[] arguments)
	{
		this.message = message;
		this.command = command;
		this.arguments = arguments;
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		this.command.execute(message, arguments);
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.start();
	}
}
