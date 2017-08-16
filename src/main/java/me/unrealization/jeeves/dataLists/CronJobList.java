package me.unrealization.jeeves.dataLists;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sx.blah.discord.handle.impl.obj.Message;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import me.unrealization.jeeves.bot.DiscordEventHandlers;
import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.interfaces.BotConfig;

public class CronJobList implements BotConfig
{
	public static class CronJob
	{
		private String schedule = "";
		private String command = "";
		private String channelId = "";
		private JobKey jobKey = null;

		public static class CronTask implements Job
		{
			@Override
			public void execute(JobExecutionContext context) throws JobExecutionException
			{
				JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
				String command = jobDataMap.getString("command");
				String channelId = jobDataMap.getString("channelId");

				System.out.println("Running cron command " + command);
				IChannel channel = Jeeves.bot.getChannelByID(channelId);
				String commandPrefix = (String)Jeeves.serverConfig.getValue(channel.getGuild().getID(), "commandPrefix");
				Message commandMessage = new Message(Jeeves.bot, 0, commandPrefix + command, Jeeves.bot.getOurUser(), channel, null, null, false, null, null, null, false, null, null, 0);
				DiscordEventHandlers.handleMessage(commandMessage, true);
			}
		}

		public void start() throws SchedulerException
		{
			CronScheduleBuilder schedule = CronScheduleBuilder.cronSchedule("0 " + this.schedule + " ?");
			CronTrigger trigger = TriggerBuilder.newTrigger().withSchedule(schedule).build();
			this.jobKey = new JobKey(this.channelId + " " + this.schedule + " " + this.command);
			JobDetail job = JobBuilder.newJob(CronTask.class).withIdentity(this.jobKey).usingJobData("channelId", this.channelId).usingJobData("command", this.command).build();
			Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.scheduleJob(job, trigger);
		}

		public void stop()
		{
			if (this.jobKey == null)
			{
				return;
			}

			try
			{
				Scheduler scheduler = new StdSchedulerFactory().getScheduler();
				scheduler.deleteJob(this.jobKey);
			}
			catch (SchedulerException e)
			{
				Jeeves.debugException(e);
			}

			this.jobKey = null;
		}

		public String getSchedule()
		{
			return this.schedule;
		}

		public void setSchedule(String schedule)
		{
			this.schedule = schedule;
		}

		public String getCommand()
		{
			return this.command;
		}

		public void setCommand(String command)
		{
			this.command = command;
		}

		public String getChannelId()
		{
			return this.channelId;
		}

		public void setChannelId(String channelId)
		{
			this.channelId = channelId;
		}
	}

	private HashMap<String, HashMap<String, Object>> config = new HashMap<String, HashMap<String, Object>>();

	public CronJobList() throws ParserConfigurationException, SAXException
	{
		this.loadConfig();
	}

	@Override
	public void loadConfig(String fileName) throws ParserConfigurationException, SAXException
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setIgnoringComments(true);
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		File xmlFile = new File(fileName);
		Document doc;

		try
		{
			doc = docBuilder.parse(xmlFile);
		}
		catch (IOException e)
		{
			Jeeves.debugException(e);
			this.config = new HashMap<String, HashMap<String, Object>>();
			return;
		}

		NodeList servers = doc.getElementsByTagName("server");

		for (int serverIndex = 0; serverIndex < servers.getLength(); serverIndex++)
		{
			Element server = (Element)servers.item(serverIndex);
			NodeList serverConfigList = server.getChildNodes();
			String serverId = server.getAttribute("serverId");
			HashMap<String, Object> config = new HashMap<String, Object>();

			for (int configIndex = 0; configIndex < serverConfigList.getLength(); configIndex++)
			{
				Node configNode = serverConfigList.item(configIndex);

				if (configNode.getNodeType() != Node.ELEMENT_NODE)
				{
					continue;
				}

				String configName = configNode.getNodeName();
				Object configValue;
				NodeList itemChildNodes = configNode.getChildNodes();

				if (itemChildNodes.getLength() == 0)
				{
					configValue = "";
				}
				else if (itemChildNodes.getLength() == 1)
				{
					System.out.println("CronJobList wants to load a string.");
					configValue = configNode.getTextContent().trim();
				}
				else
				{
					configValue = new CronJob[0];
					List<CronJob> tmpList = new ArrayList<CronJob>();

					for (int itemIndex = 0; itemIndex < itemChildNodes.getLength(); itemIndex++)
					{
						Node itemNode = itemChildNodes.item(itemIndex);

						if (itemNode.getNodeType() != Node.ELEMENT_NODE)
						{
							continue;
						}

						CronJob cronJob = new CronJob();
						NodeList properties = itemNode.getChildNodes();

						for (int propertyIndex = 0; propertyIndex < properties.getLength(); propertyIndex++)
						{
							Node property = properties.item(propertyIndex);

							if (property.getNodeType() != Node.ELEMENT_NODE)
							{
								continue;
							}

							switch (property.getNodeName())
							{
							case "channelId":
								cronJob.setChannelId(property.getTextContent().trim());
								break;
							case "command":
								cronJob.setCommand(property.getTextContent().trim());
								break;
							case "schedule":
								cronJob.setSchedule(property.getTextContent().trim());
								break;
							default:
								break;
							}
						}

						try
						{
							cronJob.start();
						}
						catch (SchedulerException e)
						{
							Jeeves.debugException(e);
							continue;
						}

						tmpList.add(cronJob);
					}

					configValue = tmpList;
				}

				config.put(configName, configValue);
			}

			this.config.put(serverId, config);
		}
	}

	public void loadConfig() throws ParserConfigurationException, SAXException
	{
		this.loadConfig("cronJobList.xml");
	}

	@Override
	public void saveConfig(String fileName) throws ParserConfigurationException, TransformerException
	{
		Set<String> serverIdSet = this.config.keySet();
		String[] serverIdList = serverIdSet.toArray(new String[serverIdSet.size()]);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		Element docRoot = doc.createElement("config");
		doc.appendChild(docRoot);

		for (int serverIndex = 0; serverIndex < serverIdList.length; serverIndex++)
		{
			HashMap<String, Object> serverConfig = this.config.get(serverIdList[serverIndex]);

			Element server = doc.createElement("server");
			Attr serverId = doc.createAttribute("serverId");
			serverId.setValue(serverIdList[serverIndex]);
			server.setAttributeNode(serverId);
			docRoot.appendChild(server);

			if (Jeeves.bot != null)
			{
				IGuild guild = Jeeves.bot.getGuildByID(serverIdList[serverIndex]);
				Comment serverName = doc.createComment(guild.getName());
				server.appendChild(serverName);
			}

			Set<String> configKeySet = serverConfig.keySet();
			String[] configKeyList = configKeySet.toArray(new String[configKeySet.size()]);

			for (int keyIndex = 0; keyIndex < configKeyList.length; keyIndex++)
			{
				Element setting = doc.createElement(configKeyList[keyIndex]);
				Object configItem = serverConfig.get(configKeyList[keyIndex]);

				if (configItem.getClass() == String.class)
				{
					System.out.println("CronJobList wants to store a string.");
					//cronJobs.setTextContent((String)configItem);
				}
				else
				{
					List<CronJob> configItemList = CronJobList.listToCronJobList((List<?>)configItem);

					for (int itemIndex = 0; itemIndex < configItemList.size(); itemIndex++)
					{
						Element item = doc.createElement("cronJob");
						CronJob cronJob = configItemList.get(itemIndex);

						Element channel = doc.createElement("channelId");
						channel.setTextContent(cronJob.getChannelId());
						item.appendChild(channel);

						Element command = doc.createElement("command");
						command.setTextContent(cronJob.getCommand());
						item.appendChild(command);

						Element schedule = doc.createElement("schedule");
						schedule.setTextContent(cronJob.getSchedule());
						item.appendChild(schedule);

						setting.appendChild(item);
					}
				}

				server.appendChild(setting);
			}
		}

		DOMSource domSource = new DOMSource(doc);
		File xmlFile = new File(fileName);
		StreamResult result = new StreamResult(xmlFile);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(domSource, result);
	}

	public void saveConfig() throws ParserConfigurationException, TransformerException
	{
		this.saveConfig("cronJobList.xml");
	}

	@Override
	public Object getValue(String serverId, String key)
	{
		if (this.config.containsKey(serverId) == false)
		{
			return "";
		}

		HashMap<String, Object> serverConfig = this.config.get(serverId);

		if (serverConfig.containsKey(key) == false)
		{
			return "";
		}

		Object value = serverConfig.get(key);
		return value;
	}

	@Override
	public void setValue(String serverId, String key, Object value)
	{
		HashMap<String, Object> serverConfig;

		if (this.config.containsKey(serverId) == false)
		{
			serverConfig = new HashMap<String, Object>();
		}
		else
		{
			serverConfig = this.config.get(serverId);
		}

		serverConfig.put(key, value);
		this.config.put(serverId, serverConfig);
	}

	@Override
	public void removeValue(String serverId, String key)
	{
		if (this.config.containsKey(serverId) == false)
		{
			return;
		}

		this.config.get(serverId).remove(key);

		if (this.config.get(serverId).size() == 0)
		{
			this.config.remove(serverId);
		}
	}

	@Override
	public String[] getKeyList(String serverId)
	{
		HashMap<String, Object> serverConfig = this.config.get(serverId);

		if (serverConfig == null)
		{
			return null;
		}

		Set<String> keySet = this.config.get(serverId).keySet();
		String[] keyList = keySet.toArray(new String[keySet.size()]);
		return keyList;
	}

	@Override
	public boolean hasKey(String serverId, String key)
	{
		String[] keyList = this.getKeyList(serverId);

		if (keyList == null)
		{
			return false;
		}

		for (int keyIndex = 0; keyIndex < keyList.length; keyIndex++)
		{
			if (keyList[keyIndex].equals(key))
			{
				return true;
			}
		}

		return false;
	}

	public static List<CronJob> listToCronJobList(List<?> list)
	{
		List<CronJob> cronJobList = new ArrayList<CronJob>();

		for (int listIndex = 0; listIndex < list.size(); listIndex++)
		{
			Object item = list.get(listIndex);

			if (item.getClass() != CronJob.class)
			{
				continue;
			}

			CronJob value = (CronJob)item;
			cronJobList.add(value);
		}

		return cronJobList;
	}
}
