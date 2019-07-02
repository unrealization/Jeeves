package me.unrealization.jeeves.bot;

import me.unrealization.jeeves.interfaces.BotCommand;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.List;

public class CommandQueue
{
	private static CommandQueue instance = null;
	private List<CommandQueue.QueueItem> commandList = new ArrayList<CommandQueue.QueueItem>();
	private List<Thread> workers = new ArrayList<Thread>();

	private static class QueueItem
	{
		public BotCommand command = null;
		public IMessage message = null;
		public String arguments = null;
	}

	private static class QueueWorker implements Runnable
	{
		@Override
		public void run()
		{
			CommandQueue commandQueue = CommandQueue.getInstance();
			QueueItem queueItem;

			while ((queueItem = commandQueue.getQueueItem()) != null)
			{
				commandQueue.removeQueueItem();
				queueItem.command.execute(queueItem.message, queueItem.arguments);
			}
		}
	}

	private CommandQueue()
	{
		//Private constructor to prevent multiple instances
	}

	public static CommandQueue getInstance()
	{
		if (CommandQueue.instance == null)
		{
			CommandQueue.instance = new CommandQueue();
		}

		return CommandQueue.instance;
	}

	public static void runCommand(BotCommand command, IMessage message, String arguments)
	{
		CommandQueue commandQueue = CommandQueue.getInstance();
		QueueItem commandItem = new QueueItem();
		commandItem.command = command;
		commandItem.message = message;
		commandItem.arguments = arguments;
		commandQueue.addQueueItem(commandItem);
	}

	private void addQueueItem(QueueItem queueItem)
	{
		this.commandList.add(queueItem);
		boolean startWorker = false;

		if (this.workers.size() == 0)
		{
			startWorker = true;
		}
		else
		{
			for (int workerIndex = 0; workerIndex < this.workers.size(); workerIndex++)
			{
				if (this.workers.get(workerIndex).isAlive() == false)
				{
					this.workers.remove(workerIndex);
					workerIndex--;
				}
			}

			if (this.workers.size() < 5)
			{
				startWorker = true;
			}
		}

		if (startWorker == true)
		{
			Thread worker = new Thread(new QueueWorker());
			worker.start();
			this.workers.add(worker);
		}
	}

	private QueueItem getQueueItem()
	{
		if (this.commandList.size() == 0)
		{
			return null;
		}

		return this.commandList.get(0);
	}

	private void removeQueueItem()
	{
		if (this.commandList.size() == 0)
		{
			return;
		}

		this.commandList.remove(0);
	}

	public boolean isWorking()
	{
		if (this.workers == null)
		{
			return false;
		}

		for (int workerIndex = 0; workerIndex < this.workers.size(); workerIndex++)
		{
			if (this.workers.get(workerIndex).isAlive() == true)
			{
				return true;
			}
		}

		return false;
	}
}
