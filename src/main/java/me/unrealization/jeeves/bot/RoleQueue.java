package me.unrealization.jeeves.bot;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;

public class RoleQueue
{
	private static RoleQueue instance = null;
	private List<RoleQueue.QueueItem> roleList = new ArrayList<RoleQueue.QueueItem>();
	private Thread worker = null;

	private static class QueueItem
	{
		public String action = null;
		public Member user = null;
		public Role role = null;
		public MessageChannel notificationChannel = null;
	}

	private static class QueueWorker implements Runnable
	{
		@Override
		public void run()
		{
			RoleQueue roleQueue = RoleQueue.getInstance();
			QueueItem queueItem;

			while ((queueItem = roleQueue.getQueueItem()) != null)
			{
				switch (queueItem.action)
				{
				case "add":
					queueItem.user.addRole(queueItem.role.getId()).doOnSuccess(something -> roleQueue.removeQueueItem()).doOnError(e -> System.out.println(e.getMessage())).block();

					if (queueItem.notificationChannel != null)
					{
						MessageQueue.sendMessage(queueItem.notificationChannel, "The role " + queueItem.role.getName() + " has been added to " + queueItem.user.getDisplayName());
					}
					break;
				case "remove":
					queueItem.user.removeRole(queueItem.role.getId()).doOnSuccess(something -> roleQueue.removeQueueItem()).doOnError(e-> System.out.println(e.getMessage())).block();

					if (queueItem.notificationChannel != null)
					{
						MessageQueue.sendMessage(queueItem.notificationChannel, "The role " + queueItem.role.getName() + " has been removed from " + queueItem.user.getDisplayName());
					}
					break;
				default:
					System.out.println("Unknown role queue action " + queueItem.action);
					break;
				}
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

	public static void addRoleToUser(Role role, Member user, MessageChannel notificationChannel)
	{
		RoleQueue roleQueue = RoleQueue.getInstance();
		QueueItem roleItem = new QueueItem();
		roleItem.action = "add";
		roleItem.role = role;
		roleItem.user = user;
		roleItem.notificationChannel = notificationChannel;
		roleQueue.addQueueItem(roleItem);
	}

	public static void addRoleToUser(Role role, Member user)
	{
		RoleQueue.addRoleToUser(role, user, null);
	}

	public static void removeRoleFromUser(Role role, Member user, MessageChannel notificationChannel)
	{
		RoleQueue roleQueue = RoleQueue.getInstance();
		QueueItem roleItem = new QueueItem();
		roleItem.action = "remove";
		roleItem.role = role;
		roleItem.user = user;
		roleItem.notificationChannel = notificationChannel;
		roleQueue.addQueueItem(roleItem);
	}

	public static void removeRoleFromUser(Role role, Member user)
	{
		RoleQueue.removeRoleFromUser(role, user, null);
	}

	private void addQueueItem(QueueItem queueItem)
	{
		this.roleList.add(queueItem);

		if ((this.worker == null) || (this.worker.isAlive() == false))
		{
			this.worker = new Thread(new QueueWorker());
			this.worker.start();
		}
	}

	private QueueItem getQueueItem()
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
