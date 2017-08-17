package me.unrealization.jeeves.modules;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.quartz.SchedulerException;
import org.xml.sax.SAXException;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Permissions;
import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.MessageQueue;
import me.unrealization.jeeves.dataLists.CronJobList;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;

public class Cron extends BotModule
{
	private static CronJobList cronJobList = null;

	public Cron() throws ParserConfigurationException, SAXException
	{
		this.version = "1.0";

		this.commandList = new String[3];
		this.commandList[0] = "GetCronJobs";
		this.commandList[1] = "AddCron";
		this.commandList[2] = "RemoveCron";

		Cron.cronJobList = new CronJobList();
	}

	public static class GetCronJobs extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Get the list of scheduled cronjobs.";
			return output;
		}

		@Override
		public String getParameters()
		{
			return null;
		}

		@Override
		public Permissions[] permissions()
		{
			Permissions[] permissionList = new Permissions[1];
			permissionList[0] = Permissions.MANAGE_SERVER;
			return permissionList;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			Object cronJobs = Cron.cronJobList.getValue(message.getGuild().getLongID(), "cronJobs");

			if (cronJobs.getClass() == String.class)
			{
				MessageQueue.sendMessage(message.getChannel(), "There are no cronjobs for this Discord.");
				return;
			}

			List<CronJobList.CronJob> cronJobList = CronJobList.listToCronJobList((List<?>)cronJobs);

			if (cronJobList.size() == 0)
			{
				MessageQueue.sendMessage(message.getChannel(), "There are no cronjobs for this Discord.");
				return;
			}

			String output = "The following cronjobs are scheduled on this Discord:\n\n";

			for (int cronJobIndex = 0; cronJobIndex < cronJobList.size(); cronJobIndex++)
			{
				CronJobList.CronJob cronJob = cronJobList.get(cronJobIndex);
				output += "[" + Integer.toString(cronJobIndex) + "] " + cronJob.getSchedule() + " " + cronJob.getCommand() + " (" + cronJob.getChannelId() + ")\n";
			}

			MessageQueue.sendMessage(message.getChannel(), output);
		}
	}

	public static class AddCron extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Add a new cronjob.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<minute> <hour> <day of month> <month> <command>";
			return output;
		}

		@Override
		public Permissions[] permissions()
		{
			Permissions[] permissionList = new Permissions[1];
			permissionList[0] = Permissions.MANAGE_SERVER;
			return permissionList;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String minute;

			try
			{
				minute = arguments[0];
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				Jeeves.debugException(e);
				//TODO
				MessageQueue.sendMessage(message.getChannel(), "");
				return;
			}

			String hour;

			try
			{
				hour = arguments[1];
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				Jeeves.debugException(e);
				//TODO
				MessageQueue.sendMessage(message.getChannel(), "");
				return;
			}

			String day;

			try
			{
				day = arguments[2];
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				Jeeves.debugException(e);
				//TODO
				MessageQueue.sendMessage(message.getChannel(), "");
				return;
			}

			String month;

			try
			{
				month = arguments[3];
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				Jeeves.debugException(e);
				//TODO
				MessageQueue.sendMessage(message.getChannel(), "");
				return;
			}

			String schedule = minute + " " + hour + " " + day + " " + month;
			String[] commandParts = new String[arguments.length - 4];

			for (int index = 4; index < arguments.length; index++)
			{
				commandParts[index - 4] = arguments[index];
			}

			String command = String.join(" ", commandParts);

			if (command.isEmpty() == true)
			{
				//TODO
				MessageQueue.sendMessage(message.getChannel(), "");
				return;
			}

			CronJobList.CronJob cronJob = new CronJobList.CronJob();
			cronJob.setChannelId(message.getChannel().getLongID());
			cronJob.setCommand(command);
			cronJob.setSchedule(schedule);

			try
			{
				cronJob.start();
			}
			catch (SchedulerException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "The cronjob cannot be started.");
				return;
			}

			Object cronJobs = Cron.cronJobList.getValue(message.getGuild().getLongID(), "cronJobs");
			List<CronJobList.CronJob> cronJobList;

			if (cronJobs.getClass() == String.class)
			{
				cronJobList = new ArrayList<CronJobList.CronJob>();
			}
			else
			{
				cronJobList = CronJobList.listToCronJobList((List<?>)cronJobs);
			}

			cronJobList.add(cronJob);
			Cron.cronJobList.setValue(message.getGuild().getLongID(), "cronJobs", cronJobList);

			try
			{
				Cron.cronJobList.saveConfig();
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "Cannot store the setting.");
				return;
			}

			MessageQueue.sendMessage(message.getChannel(), "The cronjob has been added.");
		}
	}

	public static class RemoveCron extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Remove a cronjob.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<index>";
			return output;
		}

		@Override
		public Permissions[] permissions()
		{
			Permissions[] permissionList = new Permissions[1];
			permissionList[0] = Permissions.MANAGE_SERVER;
			return permissionList;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String cronIndex = String.join(" ", arguments).trim();
			int index;

			try
			{
				index = Integer.parseInt(cronIndex);
			}
			catch (NumberFormatException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "The index must be numeric.");
				return;
			}

			Object cronJobs = Cron.cronJobList.getValue(message.getGuild().getLongID(), "cronJobs");

			if (cronJobs.getClass() == String.class)
			{
				MessageQueue.sendMessage(message.getChannel(), "There are no cronjobs for this Discord.");
				return;
			}

			List<CronJobList.CronJob> cronJobList = CronJobList.listToCronJobList((List<?>)cronJobs);

			if (cronJobList.size() == 0)
			{
				MessageQueue.sendMessage(message.getChannel(), "There are no cronjobs for this Discord.");
				return;
			}

			CronJobList.CronJob removedCron = cronJobList.remove(index);

			if (removedCron == null)
			{
				MessageQueue.sendMessage(message.getChannel(), "The index " + cronIndex + " does not exist.");
				return;
			}

			removedCron.stop();
			Cron.cronJobList.setValue(message.getGuild().getLongID(), "cronJobs", cronJobList);

			try
			{
				Cron.cronJobList.saveConfig();
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "Cannot store the setting.");
				return;
			}

			MessageQueue.sendMessage(message.getChannel(), "The cronjob at index " + cronIndex + " has been removed.");
		}
	}
}
