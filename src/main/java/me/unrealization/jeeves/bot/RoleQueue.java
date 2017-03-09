package me.unrealization.jeeves.bot;

import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class RoleQueue
{
	private static RoleQueue instance = null;
	private List<RoleQueue.RoleQueueItem> roleList = new ArrayList<RoleQueue.RoleQueueItem>();
	private QueueWorker worker = null;

	private static class RoleQueueItem
	{
		public String action = null;
		public IUser user = null;
		public IRole role = null;
		public IChannel notificationChannel = null;
	}

	private static class QueueWorker implements Runnable
	{
		private boolean stopped = false;

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			
		}

		public void stop()
		{
			this.stopped = true;
		}
	}

	private RoleQueue()
	{
		this.worker = new QueueWorker();
		Thread workerThread = new Thread(this.worker);
		workerThread.start();
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
}
