package me.unrealization.jeeves.bot;

import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class RoleQueue
{
	private static RoleQueue instance = null;
	private List<RoleQueue.RoleQueueItem> roleList = new ArrayList<RoleQueue.RoleQueueItem>();
	private Thread worker = null;

	private static class RoleQueueItem
	{
		public String action = null;
		public IUser user = null;
		public IRole role = null;
		public IChannel notificationChannel = null;
	}

	private static class QueueWorker implements Runnable
	{
		@Override
		public void run()
		{
			RoleQueue roleQueue = RoleQueue.getInstance();
			RoleQueueItem queueItem;

			while ((queueItem = roleQueue.getQueueItem()) != null)
			{
				try
				{
					switch (queueItem.action)
					{
					case "add":
						queueItem.user.addRole(queueItem.role);

						if (queueItem.notificationChannel != null)
						{
							MessageQueue.sendMessage(queueItem.notificationChannel, "The role " + queueItem.role.getName() + " has been added to " + queueItem.user.getName());
						}
						break;
					case "remove":
						queueItem.user.removeRole(queueItem.role);

						if (queueItem.notificationChannel != null)
						{
							MessageQueue.sendMessage(queueItem.notificationChannel, "The role " + queueItem.role.getName() + " has been removed from " + queueItem.user.getName());
						}
						break;
					default:
						System.out.println("Unknown role queue action " + queueItem.action);
						break;
					}
				}
				catch (DiscordException e)
				{
					Jeeves.debugException(e);
					continue;
				}
				catch (MissingPermissionsException e)
				{
					Jeeves.debugException(e);
					System.out.println("Missing permissions to apply role change.");
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

				roleQueue.removeQueueItem();
			}
		}
	}

	private RoleQueue()
	{
		//private constructor to prevent multiple instances
	}

	public static RoleQueue getInstance()
	{
		if (RoleQueue.instance == null)
		{
			RoleQueue.instance = new RoleQueue();
		}

		return RoleQueue.instance;
	}

	public static void addRoleToUser(IRole role, IUser user, IChannel notificationChannel)
	{
		RoleQueue roleQueue = RoleQueue.getInstance();
		RoleQueueItem roleItem = new RoleQueueItem();
		roleItem.action = "add";
		roleItem.role = role;
		roleItem.user = user;
		roleItem.notificationChannel = notificationChannel;
		roleQueue.addQueueItem(roleItem);
	}

	public static void addRoleToUser(IRole role, IUser user)
	{
		RoleQueue.addRoleToUser(role, user, null);
	}

	public static void removeRoleFromUser(IRole role, IUser user, IChannel notificationChannel)
	{
		RoleQueue roleQueue = RoleQueue.getInstance();
		RoleQueueItem roleItem = new RoleQueueItem();
		roleItem.action = "remove";
		roleItem.role = role;
		roleItem.user = user;
		roleItem.notificationChannel = notificationChannel;
		roleQueue.addQueueItem(roleItem);
	}

	public static void removeRoleFromUser(IRole role, IUser user)
	{
		RoleQueue.removeRoleFromUser(role, user, null);
	}

	private void addQueueItem(RoleQueueItem queueItem)
	{
		this.roleList.add(queueItem);

		if ((this.worker == null) || (this.worker.isAlive() == false))
		{
			this.worker = new Thread(new QueueWorker());
			this.worker.start();
		}
	}

	private RoleQueueItem getQueueItem()
	{
		if (this.roleList.size() == 0)
		{
			return null;
		}

		return this.roleList.get(0);
	}

	private void removeQueueItem()
	{
		if (this.roleList.size() == 0)
		{
			return;
		}

		this.roleList.remove(0);
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
