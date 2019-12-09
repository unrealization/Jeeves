package me.unrealization.jeeves.modules;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import me.unrealization.jeeves.bot.JSONHandler;
import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.MessageQueue;
import me.unrealization.jeeves.bot.RoleQueue;
import me.unrealization.jeeves.bot.WebClient;
import me.unrealization.jeeves.dataLists.EdsmUserList;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.MessageReceivedHandler;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;

public class Ccn extends BotModule implements UserJoinedHandler, MessageReceivedHandler
{
	private static long ccnLobbyChannelId = 349187463614562316L;
	private static long ccnBarChannelId = 209372315673165825L;
	private static long ccnActionLogChannelId = 389782634441605120L;
	private static long ccnGuestRoleId = 349156047467970561L;
	private static long ccnColonistRoleId = 210509328753491968L;

	public Ccn()
	{
		this.version = "1.99.0";

		this.commandList = new String[6];
		this.commandList[0] = "CcnProximityCheck";
		this.commandList[1] = "GetCcnProximityRole";
		this.commandList[2] = "SetCcnProximityRole";
		this.commandList[3] = "GetCcnWelcomeMessageEnabled";
		this.commandList[4] = "SetCcnWelcomeMessageEnabled";
		this.commandList[5] = "SendGuidelines";

		this.defaultConfig.put("ccnProximityRole", "");
		//this.defaultConfig.put("ccnEdsmUseBetaServer", "0");
		this.defaultConfig.put("ccnEdsmId", "");
		this.defaultConfig.put("ccnEdsmApiKey", "");
		this.defaultConfig.put("ccnProximityRadius", "1000");
		this.defaultConfig.put("ccnWelcomeMessageEnabled", "0");
	}

	@Override
	public Long getDiscordId()
	{
		return Long.parseLong("209372315673165825");
	}

	@Override
	public void userJoinedHandler(MemberJoinEvent event)
	{
		String ccnWelcomeMessageEnabled = (String)Jeeves.serverConfig.getValue(event.getGuildId().asLong(), "ccnWelcomeMessageEnabled");

		if (ccnWelcomeMessageEnabled.equals("1") == true)
		{
			String message = "Welcome to the Colonia Citizens Network, " + event.getMember().getDisplayName() + "\n\n";
			message += "In order to make the most out of your experience here we have set up a number of roles which you can assign to yourself, using our bot Jeeves in our **#bots** channel. These roles allow access to special channels dedicated to different topics, where you can meet players who share your interests.\n\n";
			message += "The bot commands ``roles``, ``join`` and ``leave`` will help you to find out which roles are currently available for you to use, and allow you to give yourself a role, or take it away again.\n\n";
			message += "Please note that all bot commands have to be prefixed by pinging the bot using ``@Jeeves``\n\n";
			message += "To query what roles are available, type:\n\t``@Jeeves roles``\n\n";
			message += "To assign the role **Exploration Wing Member**:\n\t``@Jeeves join Exploration Wing Member``\n\n";
			message += "To remove the role **Exploration Wing Member**:\n\t``@Jeeves leave Exploration Wing Member``\n\n";
			message += "Our bot can also do quite a few other things to help you. Feel free to ask him for help using ``@Jeeves help``\n\n";
			message += "Have a pleasant stay on the Colonia Citizens Network Discord!\n";
			message += "The CCN Team";

			MessageQueue.sendMessage(event.getMember(), message);
		}

		Channel lobby = event.getGuild().block().getChannelById(Snowflake.of(Ccn.ccnLobbyChannelId)).block();

		if (lobby == null)
		{
			return;
		}

		String message = "Welcome to the Colonia Citizens Network, " + event.getMember().getMention() + "!\n";
		message += "Please take a moment and read through the CCN community guidelines at http://bit.ly/2lOQPWd\n";
		message += "Once you are done please respond as follows:\n";
		message += "If you agree with the guidelines, please post **@Jeeves guidelines yes**\n";
		message += "If you disagree, please post **@Jeeves guidelines no**\n";
		MessageQueue.sendMessage(lobby, message);
	}

	@Override
	public boolean messageReceivedHandler(Message message)
	{
		User botUser = Jeeves.bot.getSelf().block();

		if (message.getAuthor().get() == botUser)
		{
			return false;
		}

		if (message.getChannelId().asLong() != Ccn.ccnLobbyChannelId)
		{
			return false;
		}

		String messageContent = message.getContent().get().trim();
		String[] messageParts = messageContent.split(" ");
		int nonEmptyParts = 0;

		for (int index = 0; index < messageParts.length; index++)
		{
			if (messageParts[index].isEmpty() == false)
			{
				nonEmptyParts++;
			}
		}

		if (nonEmptyParts == 0)
		{
			return false;
		}

		if (nonEmptyParts != messageParts.length)
		{
			String tmpParts[] = new String[nonEmptyParts];
			int tmpIndex = 0;

			for (int index = 0; index < messageParts.length; index++)
			{
				if (messageParts[index].isEmpty() == true)
				{
					continue;
				}

				tmpParts[tmpIndex] = messageParts[index];
				tmpIndex++;
			}

			messageParts = tmpParts;
		}

		if (messageParts.length != 3)
		{
			return false;
		}

		if (messageParts[1].toLowerCase().equals("guidelines") == false)
		{
			return false;
		}

		if (messageParts[2].toLowerCase().equals("yes") == true)
		{
			Role guestRole = message.getGuild().block().getRoleById(Snowflake.of(Ccn.ccnGuestRoleId)).block();
			Role colonistRole = message.getGuild().block().getRoleById(Snowflake.of(Ccn.ccnColonistRoleId)).block();

			if ((guestRole == null) || (colonistRole == null))
			{
				Channel actionLogChannel = message.getGuild().block().getChannelById(Snowflake.of(Ccn.ccnActionLogChannelId)).block();

				if (actionLogChannel != null)
				{
					MessageQueue.sendMessage(actionLogChannel, "Error: Cannot tag up " + message.getAuthor().get().getUsername() + ", the IDs role the guest- and/or colonist-role must have changed!");
				}
				else
				{
					System.out.println("Error: Cannot tag up " + message.getAuthor().get().getUsername() + ", the IDs role the guest- and/or colonist-role must have changed!");
				}

				return true;
			}

			RoleQueue.removeRoleFromUser(guestRole, message.getAuthorAsMember().block());
			RoleQueue.addRoleToUser(colonistRole, message.getAuthorAsMember().block());
			Channel barChannel = message.getGuild().block().getChannelById(Snowflake.of(Ccn.ccnBarChannelId)).block();

			if (barChannel != null)
			{
				MessageQueue.sendMessage(barChannel, "Welcome to the Colonia Citizens Network, " + message.getAuthor().get().getMention() + "!");
			}

			Channel actionLogChannel = message.getGuild().block().getChannelById(Snowflake.of(Ccn.ccnActionLogChannelId)).block();

			if (actionLogChannel != null)
			{
				MessageQueue.sendMessage(actionLogChannel, "The user " + message.getAuthor().get().getUsername() + " agrees with the guidelines and has therefore been tagged up.");
			}

			return true;
		}
		else if (messageParts[2].toLowerCase().equals("no") == true)
		{
			boolean kicked = false;
			message.getGuild().block().kick(message.getAuthor().get().getId()).block();
			kicked = true;
			Channel actionLogChannel = message.getGuild().block().getChannelById(Snowflake.of(ccnActionLogChannelId)).block();

			if (actionLogChannel != null)
			{
				if (kicked == true)
				{
					MessageQueue.sendMessage(actionLogChannel, "The user " + message.getAuthor().get().getUsername() + " did not agree with the guidelines and has therefore been kicked.");
				}
				else
				{
					MessageQueue.sendMessage(actionLogChannel, "The user " + message.getAuthor().get().getUsername() + " did not agree with the guidelines, but could not be kicked.");
				}
			}

			return true;
		}

		return false;
	}

	private static class ProximityCheckModel
	{
		@SuppressWarnings("unused")
		public String countPublic;
		@SuppressWarnings("unused")
		public String countPrivate;
		public String[] commanders;
	}

	public static class CcnProximityCheck extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Automatically assign a role to users that are in proximity of Colonia.";
			return output;
		}

		@Override
		public String getParameters()
		{
			return null;
		}

		@Override
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_GUILD;
			return permissionList;
		}

		@Override
		public void execute(Message message, String argumentString)
		{
			EdsmUserList edsmUserList;

			try
			{
				edsmUserList = new EdsmUserList();
			}
			catch (ParserConfigurationException | SAXException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel().block(), "EDSM userlist is not available. Cannot process proximity check.");
				return;
			}

			String roleIdString = (String)Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "ccnProximityRole");

			if (roleIdString.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No CCN proximity role has been set.");
				return;
			}

			long roleId = Long.parseLong(roleIdString);
			Role role = message.getGuild().block().getRoleById(Snowflake.of(roleId)).block();

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The current CCN proximity role is invalid.");
				//TODO Reset
				return;
			}

			String ccnEdsmId = (String)Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "ccnEdsmId");

			if (ccnEdsmId.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No CCN EDSM Id has been set.");
				return;
			}

			String ccnEdsmApiKey = (String)Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "ccnEdsmApiKey");

			if (ccnEdsmApiKey.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No CCN EDSM API Key has been set.");
				return;
			}

			String ccnProximityRadius = (String)Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "ccnProximityRadius");

			if (ccnProximityRadius.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No proximity radius has been set.");
				return;
			}

			MessageQueue.sendMessage(message.getChannel().block(), "This might take a while.");
			String response;

			try
			{
				response = WebClient.getPage("https://www.edsm.net/tools/ccn/colonist?id=" + ccnEdsmId + "&apiKey=" + ccnEdsmApiKey + "&radius=" + ccnProximityRadius);
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel().block(), "EDSM communication error.");
				return;
			}

			Ccn.ProximityCheckModel data = (Ccn.ProximityCheckModel)JSONHandler.parseJSON(response, Ccn.ProximityCheckModel.class);
			Iterable<Member> userList = message.getGuild().block().getMembers().toIterable();
			String output = "";

			for (Member user: userList)
			{
				String userIdString = user.getId().asString();

				if (edsmUserList.hasKey(userIdString) == false)
				{
					continue;
				}

				String edsmUserName = (String)edsmUserList.getValue(userIdString);

				if (user.getRoleIds().contains(role.getId()) == true)
				{
					boolean found = false;

					for (int edsmUserIndex = 0; edsmUserIndex < data.commanders.length; edsmUserIndex++)
					{
						if (data.commanders[edsmUserIndex].toLowerCase().equals(edsmUserName.toLowerCase()) == true)
						{
							found = true;
							break;
						}
					}

					if (found == false)
					{
						RoleQueue.removeRoleFromUser(role, user);
						output += user.getDisplayName() + " is no longer in Colonia.\n";
					}
				}
				else
				{
					for (int edsmUserIndex = 0; edsmUserIndex < data.commanders.length; edsmUserIndex++)
					{
						if (data.commanders[edsmUserIndex].toLowerCase().equals(edsmUserName.toLowerCase()) == true)
						{
							RoleQueue.addRoleToUser(role, user);
							output += user.getDisplayName() + " has arrived in Colonia.\n";
							break;
						}
					}
				}
			}

			if (output.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No changes to process.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel().block(), output);
			}
		}
	}

	public static class GetCcnProximityRole extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Get the current CCN proximity role.";
			return output;
		}

		@Override
		public String getParameters()
		{
			return null;
		}

		@Override
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_GUILD;
			return permissionList;
		}

		@Override
		public void execute(Message message, String argumentString)
		{
			String roleIdString = (String)Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "ccnProximityRole");

			if (roleIdString.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No CCN proximity role has been set.");
				return;
			}

			long roleId = Long.parseLong(roleIdString);
			Role role = message.getGuild().block().getRoleById(Snowflake.of(roleId)).block();

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "A CCN proximity role has been set, but it does not exist.");

				Ccn ccn = new Ccn();
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "ccnProximityRole", ccn.getDefaultConfig().get("ccnProximityRole"));

				try
				{
					Jeeves.serverConfig.saveConfig();
				}
				catch (ParserConfigurationException | TransformerException e)
				{
					Jeeves.debugException(e);
				}

				return;
			}

			MessageQueue.sendMessage(message.getChannel().block(), "The CCN proximity role is: " + role.getName());
		}
	}

	public static class SetCcnProximityRole extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Set or clear the CCN proximity role.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "[role]";
			return output;
		}

		@Override
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_GUILD;
			return permissionList;
		}

		@Override
		public void execute(Message message, String roleName)
		{
			Role role = null;

			if (roleName.isEmpty() == true)
			{
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "ccnProximityRole", "");
			}
			else
			{
				role = Jeeves.findRole(message.getGuild().block(), roleName);

				if (role == null)
				{
					MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the role " + roleName);
					return;
				}

				String roleIdString = role.getId().asString();
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "ccnProximityRole", roleIdString);
			}

			try
			{
				Jeeves.serverConfig.saveConfig();
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot store the setting.");
				return;
			}

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The CCN proximity role has been cleared.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The CCN proximity role has been set to: " + role.getName());
			}
		}
	}

	public static class GetCcnWelcomeMessageEnabled extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Check if the CCN welcome message is enabled.";
			return output;
		}

		@Override
		public String getParameters()
		{
			return null;
		}

		@Override
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_GUILD;
			return permissionList;
		}

		@Override
		public void execute(Message message, String argumentString)
		{
			String ccnWelcomeMessageEnabled = (String)Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "ccnWelcomeMessageEnabled");

			if (ccnWelcomeMessageEnabled.equals("0") == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The CCN welcome message is disabled.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The CCN welcome message is enabled.");
			}
		}
	}

	public static class SetCcnWelcomeMessageEnabled extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Enable/disable the CCN welcome message.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<1|0>";
			return output;
		}

		@Override
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_GUILD;
			return permissionList;
		}

		@Override
		public void execute(Message message, String ccnWelcomeMessageEnabled)
		{
			if ((ccnWelcomeMessageEnabled.equals("0") == false) && (ccnWelcomeMessageEnabled.equals("1") == false))
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Invalid value");
				return;
			}

			Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "ccnWelcomeMessageEnabled", ccnWelcomeMessageEnabled);

			try
			{
				Jeeves.serverConfig.saveConfig();
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot store the setting.");
				return;
			}

			if (ccnWelcomeMessageEnabled.equals("0") == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The CCN welcome message has been disabled.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The CCN welcome message has ben enabled.");
			}
		}
	}

	public static class SendGuidelines extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Post a link to the guidelines and instructions in the lobby.";
			return output;
		}

		@Override
		public String getParameters()
		{
			return null;
		}

		@Override
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_GUILD;
			return permissionList;
		}

		@Override
		public void execute(Message message, String argumentString)
		{
			Channel lobby = message.getGuild().block().getChannelById(Snowflake.of(Ccn.ccnLobbyChannelId)).block();

			if (lobby == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot send the guidelines message. The ID of the lobby must have changed.");
				return;
			}

			String output = "Welcome to the Colonia Citizens Network! " + "\n";
			output += "Please take a moment and read through the CCN community guidelines at http://bit.ly/2lOQPWd\n";
			output += "Once you are done please respond as follows:\n";
			output += "If you agree with the guidelines, please post **@Jeeves guidelines yes**\n";
			output += "If you disagree, please post **@Jeeves guidelines no**\n";
			MessageQueue.sendMessage(lobby, output);
		}
	}
}
