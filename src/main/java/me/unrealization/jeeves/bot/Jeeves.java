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
import me.unrealization.jeeves.modules.ParadoxWing;
import me.unrealization.jeeves.modules.Roles;
import me.unrealization.jeeves.modules.UserLog;
import me.unrealization.jeeves.modules.Welcome;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.xml.sax.SAXException;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;

public class Jeeves
{
	public static String version = null;
	public static DiscordClient bot = null;
	public static ClientConfig clientConfig = null;
	public static ServerConfig serverConfig = null;
	private static HashMap<String, BotModule> modules = null;

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
		Jeeves.modules.put("paradoxWing", new ParadoxWing());
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

	public static GuildChannel findChannel(Guild server, String channelName)
	{
		List<GuildChannel> channelList = server.getChannels().collectList().block();

		for (int channelIndex = 0; channelIndex < channelList.size(); channelIndex++)
		{
			GuildChannel channel = channelList.get(channelIndex);

			if ((channel.getName().equals(channelName) == true) || (channel.getMention().equals(channelName) == true))
			{
				return channel;
			}
		}

		return null;
	}

	public static Role findRole(Guild server, String roleName)
	{
		List<Role> roleList = server.getRoles().collectList().block();

		for (int roleIndex = 0; roleIndex < roleList.size(); roleIndex++)
		{
			Role role = roleList.get(roleIndex);

			if ((role.getName().equals(roleName) == true) || (role.getMention().equals(roleName) == true))
			{
				return role;
			}
		}

		return null;
	}

	public static Member findUser(Guild server, String userName)
	{
		List<Member> userList = server.getMembers().collectList().block();

		for (int userIndex = 0; userIndex < userList.size(); userIndex++)
		{
			Member user = userList.get(userIndex);

			if ((user.getDisplayName().equals(userName)) || (user.getNickname().toString().equals(userName) == true) || (user.getMention().equals(userName) == true) || (user.getNicknameMention().equals(userName) == true))
			{
				return user;
			}
		}

		return null;
	}

	public static boolean isIgnored(long serverId, Channel channel)
	{
		Object ignoredChannels = Jeeves.serverConfig.getValue(serverId, "ignoredChannels");

		if (ignoredChannels.getClass() == String.class)
		{
			return false;
		}

		List<String> ignoredChannelList = Jeeves.listToStringList((List<?>)ignoredChannels);
		return ignoredChannelList.contains(channel.getId().asString());
	}

	public static boolean isIgnored(long serverId, User user)
	{
		Object ignoredUsers = Jeeves.serverConfig.getValue(serverId, "ignoredUsers");

		if (ignoredUsers.getClass() == String.class)
		{
			return false;
		}

		List<String> ignoredUserList = Jeeves.listToStringList((List<?>)ignoredUsers);
		return ignoredUserList.contains(user.getId().asString());
	}

	public static boolean isIgnored(long serverId, Role role)
	{
		Object ignoredRoles = Jeeves.serverConfig.getValue(serverId, "ignoredRoles");

		if (ignoredRoles.getClass() == String.class)
		{
			return false;
		}

		List<String> ignoredRoleList = Jeeves.listToStringList((List<?>)ignoredRoles);
		return ignoredRoleList.contains(role.getId().asString());
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

	public static String[] splitArguments(String argumentString)
	{
		String[] arguments = argumentString.split(":");

		for (int index = 0; index < arguments.length; index++)
		{
			arguments[index] = arguments[index].trim();
		}

		return arguments;
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

			/*if (Jeeves.bot.isLoggedIn() == true)
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
			}*/

			if (Jeeves.bot.isConnected() == true)
			{
				Jeeves.bot.logout().block();
			}
		}
	}

	public static void main(String[] args)
	{
		Jeeves.version = Jeeves.class.getPackage().getImplementationVersion();

		if (Jeeves.version == null)
		{
			Jeeves.version = "Testing";
		}

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
		Jeeves.bot = new DiscordClientBuilder((String)Jeeves.clientConfig.getValue("loginToken")).build();
		EventDispatcher dispatcher = Jeeves.bot.getEventDispatcher();
		dispatcher.on(ReadyEvent.class).subscribe(event -> new DiscordEventHandlers.ReadyEventListener().execute(event));
		Jeeves.bot.login().block();
	}
}
