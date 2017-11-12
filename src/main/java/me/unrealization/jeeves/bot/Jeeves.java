package me.unrealization.jeeves.bot;

import me.unrealization.jeeves.interfaces.BotModule;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import me.unrealization.jeeves.modules.Ccn;
import me.unrealization.jeeves.modules.Cron;
import me.unrealization.jeeves.modules.Edsm;
import me.unrealization.jeeves.modules.Internal;
import me.unrealization.jeeves.modules.ModLog;
import me.unrealization.jeeves.modules.Roles;
import me.unrealization.jeeves.modules.UserLog;
import me.unrealization.jeeves.modules.Welcome;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.xml.sax.SAXException;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

public class Jeeves
{
	public static String version = "0.9.1";
	public static IDiscordClient bot = null;
	public static ClientConfig clientConfig = null;
	public static ServerConfig serverConfig = null;
	private static HashMap<String, BotModule> modules = null;

	private static IDiscordClient createClient(String token)
	{
		return Jeeves.createClient(token, true);
	}

	private static IDiscordClient createClient(String token, boolean login)
	{
		ClientBuilder clientBuilder = new ClientBuilder();
		clientBuilder.withToken(token);
		IDiscordClient client = null;

		try
		{
			if (login == true)
			{
				client = clientBuilder.login();
			}
			else
			{
				client = clientBuilder.build();
			}
		}
		catch (DiscordException e)
		{
			Jeeves.debugException(e);
		}

		return client;
	}

	private static void loadModules()
	{
		Jeeves.modules = new HashMap< String, BotModule>();
		Jeeves.modules.put("ccn", new Ccn());

		try
		{
			Jeeves.modules.put("cron", new Cron());
		}
		catch (ParserConfigurationException | SAXException e)
		{
			Jeeves.debugException(e);
		}

		try
		{
			Jeeves.modules.put("edsm", new Edsm());
		}
		catch (ParserConfigurationException | SAXException e)
		{
			Jeeves.debugException(e);
		}

		Jeeves.modules.put("internal", new Internal());
		Jeeves.modules.put("modLog", new ModLog());
		Jeeves.modules.put("roles", new Roles());
		Jeeves.modules.put("userLog", new UserLog());
		Jeeves.modules.put("welcome", new Welcome());
	}

	public static BotModule getModule(String moduleName)
	{
		BotModule module = Jeeves.modules.get(moduleName);
		return module;
	}

	public static String[] getModuleList()
	{
		Set<String> moduleSet = Jeeves.modules.keySet();
		String[] moduleList = moduleSet.toArray(new String[moduleSet.size()]);
		return moduleList;
	}

	public static void checkConfig(long serverId, HashMap<String, Object> defaultConfig) throws ParserConfigurationException, TransformerException
	{
		if (defaultConfig == null)
		{
			return;
		}

		boolean updated = false;
		Set<String> keySet = defaultConfig.keySet();
		String[] keyList = keySet.toArray(new String[keySet.size()]);

		for (int keyIndex = 0; keyIndex < keyList.length; keyIndex++)
		{
			if (Jeeves.serverConfig.hasKey(serverId, keyList[keyIndex]) == false)
			{
				Jeeves.serverConfig.setValue(serverId, keyList[keyIndex], defaultConfig.get(keyList[keyIndex]));
				updated = true;
			}
		}

		if (updated == true)
		{
			Jeeves.serverConfig.saveConfig();
		}
	}

	public static boolean debugException(Exception e)
	{
		String debugging = (String)Jeeves.clientConfig.getValue("debugging");

		if (debugging.equals("1") == true)
		{
			e.printStackTrace();
			return true;
		}

		return false;
	}

	public static List<String> listToStringList(List<?> list)
	{
		List<String> stringList = new ArrayList<String>();

		for (int listIndex = 0; listIndex < list.size(); listIndex++)
		{
			Object item = list.get(listIndex);

			if (item.getClass() != String.class)
			{
				continue;
			}

			String value = (String)item;
			stringList.add(value);
		}

		return stringList;
	}

	public static IChannel findChannel(IGuild server, String channelName)
	{
		List<IChannel> channelList = server.getChannelsByName(channelName);

		if (channelList.size() > 0)
		{
			return channelList.get(0);
		}
		else
		{
			channelList = server.getChannels();

			for (int channelIndex = 0; channelIndex < channelList.size(); channelIndex++)
			{
				IChannel channel = channelList.get(channelIndex);

				if (channel.mention().equals(channelName) == true)
				{
					return channel;
				}
			}
		}

		return null;
	}

	public static IRole findRole(IGuild server, String roleName)
	{
		List<IRole> roleList = server.getRolesByName(roleName);

		if (roleList.size() > 0)
		{
			return roleList.get(0);
		}
		else
		{
			roleList = server.getRoles();

			for (int roleIndex = 0; roleIndex < roleList.size(); roleIndex++)
			{
				IRole role = roleList.get(roleIndex);

				if (role.mention().equals(roleName) == true)
				{
					return role;
				}
			}
		}

		return null;
	}

	public static IUser findUser(IGuild server, String userName)
	{
		List<IUser> userList = server.getUsersByName(userName);

		if (userList.size() > 0)
		{
			return userList.get(0);
		}
		else
		{
			userList = server.getUsers();

			for (int userIndex = 0; userIndex < userList.size(); userIndex++)
			{
				IUser user = userList.get(userIndex);

				if ((user.mention(true).equals(userName) == true) || (user.mention(false).equals(userName) == true))
				{
					return user;
				}
			}
		}

		return null;
	}

	public static boolean isIgnored(IChannel channel)
	{
		Object ignoredChannels = Jeeves.serverConfig.getValue(channel.getGuild().getLongID(), "ignoredChannels");

		if (ignoredChannels.getClass() == String.class)
		{
			return false;
		}

		List<String> ignoredChannelList = Jeeves.listToStringList((List<?>)ignoredChannels);
		return ignoredChannelList.contains(Long.toString(channel.getLongID()));
	}

	public static boolean isIgnored(long serverId, IUser user)
	{
		Object ignoredUsers = Jeeves.serverConfig.getValue(serverId, "ignoredUsers");

		if (ignoredUsers.getClass() == String.class)
		{
			return false;
		}

		List<String> ignoredUserList = Jeeves.listToStringList((List<?>)ignoredUsers);
		return ignoredUserList.contains(Long.toString(user.getLongID()));
	}

	public static boolean isDisabled(long serverId, BotModule module)
	{
		Long discordId = module.getDiscordId();

		if ((discordId != null) && (discordId.equals(serverId) == false))
		{
			return true;
		}

		Object disabledModules = Jeeves.serverConfig.getValue(serverId, "disabledModules");

		if (disabledModules.getClass() == String.class)
		{
			return false;
		}

		String moduleName = module.getClass().getSimpleName().toLowerCase();
		List<String> disabledModuleList = Jeeves.listToStringList((List<?>)disabledModules);
		return disabledModuleList.contains(moduleName);
	}

	public static String getUtcTime()
	{
		Instant now = Instant.now();
		String timeString = now.toString();
		Pattern regEx = Pattern.compile("^([\\d]{4})-([\\d]{2})-([\\d]{2})T([\\d]{2}:[\\d]{2}:[\\d]{2})\\.[\\d]{3}Z$");
		Matcher regExMatcher = regEx.matcher(timeString);

		if (regExMatcher.matches() == false)
		{
			return timeString;
		}

		String year = regExMatcher.group(1);
		String month = regExMatcher.group(2);
		String day = regExMatcher.group(3);
		String time = regExMatcher.group(4);
		timeString = year + "-" + month + "-" + day + " " + time;
		return timeString;
	}

	private static class ShutdownHook extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				Scheduler scheduler = new StdSchedulerFactory().getScheduler();
				scheduler.shutdown();
			}
			catch (SchedulerException e)
			{
				Jeeves.debugException(e);
			}

			if (Jeeves.bot.isLoggedIn() == true)
			{
				System.out.println("Logging out.");

				try
				{
					Jeeves.bot.logout();
				}
				catch (DiscordException e)
				{
					Jeeves.debugException(e);
				}
			}
		}
	}

	public static void main(String[] args)
	{
		try
		{
			Jeeves.clientConfig = new ClientConfig();
		}
		catch (ParserConfigurationException | SAXException | IOException e)
		{
			Jeeves.debugException(e);
			return;
		}

		try
		{
			Jeeves.serverConfig = new ServerConfig();
		}
		catch (ParserConfigurationException | SAXException e)
		{
			Jeeves.debugException(e);
			return;
		}

		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		Jeeves.loadModules();
		Jeeves.bot = Jeeves.createClient((String)Jeeves.clientConfig.getValue("loginToken"));
		EventDispatcher dispatcher = Jeeves.bot.getDispatcher();
		dispatcher.registerListener(new DiscordEventHandlers.ReadyEventListener());
	}
}
