package me.unrealization.jeeves.bot;

import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IPrivateChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class MessageQueue
{
	private static MessageQueue instance = null;
	private List<MessageQueue.MessageQueueItem> messageList = new ArrayList<MessageQueue.MessageQueueItem>();
	private QueueWorker worker = null;

	private static class MessageQueueItem
	{
		public IChannel channel = null;
		public IUser user = null;
		public String message = null;
	}

	private static class QueueWorker implements Runnable
	{
		private boolean stopped = false;

		@Override
		public void run()
		{
			MessageQueue messageQueue = MessageQueue.getInstance();

			while (this.stopped == false)
			{
				MessageQueueItem queueItem = messageQueue.getQueueItem();

				if (queueItem == null)
				{
					continue;
				}

				if (queueItem.channel == null)
				{
					IPrivateChannel channel;

					try
					{
						channel = queueItem.user.getOrCreatePMChannel();
					}
					catch (RateLimitException e)
					{
						Jeeves.debugException(e);
						//TODO: sleep
						continue;
					}
					catch (DiscordException e)
					{
						Jeeves.debugException(e);
						continue;
					}

					MessageBuilder messageBuilder = new MessageBuilder(Jeeves.bot);

					try
					{
						messageBuilder.withContent(queueItem.message).withChannel(channel).build();
					}
					catch (DiscordException | MissingPermissionsException e)
					{
						Jeeves.debugException(e);
						continue;
					}
					catch (RateLimitException e)
					{
						Jeeves.debugException(e);

						try
						{
							Thread.sleep(5000);
						}
						catch (InterruptedException e1)
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						continue;
					}

					messageQueue.removeQueueItem();
				}
				else
				{
					MessageBuilder messageBuilder = new MessageBuilder(Jeeves.bot);

					try
					{
						messageBuilder.withContent(queueItem.message).withChannel(queueItem.channel).build();
					}
					catch (DiscordException | MissingPermissionsException e)
					{
						Jeeves.debugException(e);
						continue;
					}
					catch (RateLimitException e)
					{
						Jeeves.debugException(e);

						try
						{
							Thread.sleep(5000);
						}
						catch (InterruptedException e1)
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						continue;
					}

					messageQueue.removeQueueItem();
				}
			}
		}

		public void stop()
		{
			this.stopped = true;
		}
	}

	private MessageQueue()
	{
		this.worker = new QueueWorker();
		Thread workerThread = new Thread(this.worker);
		workerThread.start();
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
			MessageQueueItem messageItem = new MessageQueueItem();
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
			MessageQueueItem messageItem = new MessageQueueItem();
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
			String messagePart = message.substring(0, 2000);
			messageList.add(messagePart);
			message = message.substring(2000);
		}

		messageList.add(message);
		return messageList;
	}

	private void addQueueItem(MessageQueueItem queueItem)
	{
		this.messageList.add(queueItem);
	}

	private MessageQueueItem getQueueItem()
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
}
