package bot;

import interfaces.BotModule;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import modules.Edsm;
import modules.Internal;
import modules.Welcome;

import org.xml.sax.SAXException;

import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class Jeeves
{
	public static String version = "Jeeves4J 0.4";
	public static IDiscordClient bot = null;
	public static ClientConfig clientConfig = null;
	public static ServerConfig serverConfig = null;
	public static HashMap<String, BotModule> modules = new HashMap< String, BotModule>();

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
			e.printStackTrace();
		}

		return client;
	}

	private static void loadModules()
	{
		Edsm edsm = new Edsm();
		Jeeves.modules.put("edsm", edsm);

		Internal internal = new Internal();
		Jeeves.modules.put("internal", internal);

		Welcome welcome = new Welcome();
		Jeeves.modules.put("welcome", welcome);
	}

	public static void checkConfig(String serverId, HashMap<String, String> defaultConfig) throws ParserConfigurationException, TransformerException
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
			e.printStackTrace();
			return false;
		}

		return true;
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

	public static void main(String[] args)
	{
		try
		{
			Jeeves.clientConfig = new ClientConfig();
		}
		catch (ParserConfigurationException | SAXException | IOException e)
		{
			e.printStackTrace();
			return;
		}

		try
		{
			Jeeves.serverConfig = new ServerConfig();
		}
		catch (ParserConfigurationException | SAXException e)
		{
			e.printStackTrace();
			return;
		}

		Jeeves.loadModules();
		Jeeves.bot = Jeeves.createClient(Jeeves.clientConfig.getValue("loginToken"));
		EventDispatcher dispatcher = Jeeves.bot.getDispatcher();
		dispatcher.registerListener(new DiscordEventHandlers.ReadyEventListener());
	}
}
