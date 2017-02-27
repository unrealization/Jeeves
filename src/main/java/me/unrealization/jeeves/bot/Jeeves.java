package me.unrealization.jeeves.bot;

import me.unrealization.jeeves.interfaces.BotModule;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import me.unrealization.jeeves.modules.Ccn;
import me.unrealization.jeeves.modules.Edsm;
import me.unrealization.jeeves.modules.Internal;
import me.unrealization.jeeves.modules.UserLog;
import me.unrealization.jeeves.modules.Welcome;

import org.xml.sax.SAXException;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class Jeeves
{
	public static String version = "0.7";
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
		Jeeves.modules.put("edsm", new Edsm());
		Jeeves.modules.put("internal", new Internal());
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

	public static void checkConfig(String serverId, HashMap<String, Object> defaultConfig) throws ParserConfigurationException, TransformerException
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

	public static boolean sendMessage(IChannel channel, String message)
	{
		MessageBuilder messageBuilder = new MessageBuilder(Jeeves.bot);

		try
		{
			messageBuilder.withContent(message).withChannel(channel).build();
		}
		catch (RateLimitException | DiscordException | MissingPermissionsException e)
		{
			Jeeves.debugException(e);
			return false;
		}

		return true;
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
		Object ignoredChannels = Jeeves.serverConfig.getValue(channel.getGuild().getID(), "ignoredChannels");

		if (ignoredChannels.getClass() == String.class)
		{
			return false;
		}

		String[] ignoredChannelList = (String[])ignoredChannels;

		for (int channelIndex = 0; channelIndex < ignoredChannelList.length; channelIndex++)
		{
			if (channel.getID().equals(ignoredChannelList[channelIndex]) == true)
			{
				return true;
			}
		}

		return false;
	}

	public static boolean isIgnored(String serverId, IUser user)
	{
		Object ignoredUsers = Jeeves.serverConfig.getValue(serverId, "ignoredUsers");

		if (ignoredUsers.getClass() == String.class)
		{
			return false;
		}

		String[] ignoredUserList = (String[])ignoredUsers;

		for (int userIndex = 0; userIndex < ignoredUserList.length; userIndex++)
		{
			if (user.getID().equals(ignoredUserList[userIndex]) == true)
			{
				return true;
			}
		}

		return false;
	}

	public static boolean isDisabled(String serverId, BotModule module)
	{
		String discordId = module.getDiscordId();

		if ((discordId != null) && (discordId.equals(serverId) == false))
		{
			return true;
		}

		Object disabledModules = Jeeves.serverConfig.getValue(serverId, "disabledModules");

		if (disabledModules.getClass() == String.class)
		{
			return false;
		}

		String[] disabledModuleList = (String[])disabledModules;
		String moduleName = module.getClass().getSimpleName().toLowerCase();

		for (int moduleIndex = 0; moduleIndex < disabledModuleList.length; moduleIndex++)
		{
			if (disabledModuleList[moduleIndex].toLowerCase().equals(moduleName) == true)
			{
				return true;
			}
		}

		return false;
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

		Jeeves.loadModules();
		Jeeves.bot = Jeeves.createClient((String)Jeeves.clientConfig.getValue("loginToken"));
		EventDispatcher dispatcher = Jeeves.bot.getDispatcher();
		dispatcher.registerListener(new DiscordEventHandlers.ReadyEventListener());
	}
}
