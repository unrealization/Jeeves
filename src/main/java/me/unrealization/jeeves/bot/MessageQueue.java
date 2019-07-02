package me.unrealization.jeeves.bot;

import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class MessageQueue
{
	private static MessageQueue instance = null;
	private List<MessageQueue.QueueItem> messageList = new ArrayList<MessageQueue.QueueItem>();
	private Thread worker = null;

	private static class QueueItem
	{
		public IChannel channel = null;
		public IUser user = null;
		public String message = null;
	}

	private static class QueueWorker implements Runnable
	{
		@Override
		public void run()
		{
			MessageQueue messageQueue = MessageQueue.getInstance();
			QueueItem queueItem;

			while ((queueItem = messageQueue.getQueueItem()) != null)
			{
				IChannel channel;

				if (queueItem.channel == null)
				{
					try
					{
						channel = queueItem.user.getOrCreatePMChannel();
					}
					catch (RateLimitException e)
					{
						Jeeves.debugException(e);

						try
						{
							Thread.sleep(e.getRetryDelay());
						}
						catch (InterruptedException e1)
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						continue;
					}
					catch (DiscordException e)
					{
						Jeeves.debugException(e);
						continue;
					}

				}
				else
				{
					channel = queueItem.channel;
				}

				MessageBuilder messageBuilder = new MessageBuilder(Jeeves.bot);

				try
				{
					messageBuilder.withContent(queueItem.message).withChannel(channel).build();
				}
				catch (RateLimitException e)
				{
					Jeeves.debugException(e);

					try
					{
						Thread.sleep(e.getRetryDelay());
					}
					catch (InterruptedException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					continue;
				}
				catch (MissingPermissionsException e)
				{
					Jeeves.debugException(e);
					System.out.println("Missing permissions to send message. Discarding.");
				}
				catch (DiscordException e)
				{
					Jeeves.debugException(e);
					continue;
				}

				messageQueue.removeQueueItem();
			}
		}
	}

	private MessageQueue()
	{
		//private constructor to prevent multiple instances
	}

	public static MessageQueue getInstance()
	{
		if (MessageQueue.instance == null)
		{
			MessageQueue.instance = new MessageQueue();
		}

		return MessageQueue.instance;
	}

	public static void sendMessage(IChannel channel, String message)
	{
		List<String> messageList = MessageQueue.splitMessage(message);
		MessageQueue messageQueue = MessageQueue.getInstance();

		for (int messageIndex = 0; messageIndex < messageList.size(); messageIndex++)
		{
			QueueItem messageItem = new QueueItem();
			messageItem.channel = channel;
			messageItem.message = messageList.get(messageIndex);
			messageQueue.addQueueItem(messageItem);
		}
	}

	public static void sendMessage(IUser user, String message)
	{
		List<String> messageList = MessageQueue.splitMessage(message);
		MessageQueue messageQueue = MessageQueue.getInstance();

		for (int messageIndex = 0; messageIndex < messageList.size(); messageIndex++)
		{
			QueueItem messageItem = new QueueItem();
			messageItem.user = user;
			messageItem.message = messageList.get(messageIndex);
			messageQueue.addQueueItem(messageItem);
		}
	}

	private static List<String> splitMessage(String message)
	{
		List<String> messageList = new ArrayList<String>();

		while (message.length() > 2000)
		{
			String[] messageParts = message.split("\n");
			String tmpMessage = "";

			while ((messageParts.length > 0) && (tmpMessage.length() + messageParts[0].length() < 1950))
			{
				tmpMessage += messageParts[0] + "\n";
				String[] tmpParts = new String[messageParts.length - 1];

				for (int partIndex = 1; partIndex < messageParts.length; partIndex++)
				{
					tmpParts[partIndex - 1] = messageParts[partIndex];
				}

				messageParts = tmpParts;
			}

			messageList.add(tmpMessage);
			message = String.join("\n", messageParts);
		}

		messageList.add(message);
		return messageList;
	}

	private void addQueueItem(QueueItem queueItem)
	{
		this.messageList.add(queueItem);

		if ((this.worker == null) || (this.worker.isAlive() == false))
		{
			this.worker = new Thread(new QueueWorker());
			this.worker.start();
		}
	}

	private QueueItem getQueueItem()
	{
		if (this.messageList.size() == 0)
		{
			return null;
		}

		return this.messageList.get(0);
	}

	private void removeQueueItem()
	{
		if (this.messageList.size() == 0)
		{
			return;
		}

		this.messageList.remove(0);
	}

	public boolean isWorking()
	{
		if (this.worker == null)
		{
			return false;
		}

		return this.worker.isAlive();
	}
}
