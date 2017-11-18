package me.unrealization.jeeves.modules;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import me.unrealization.jeeves.bot.JSONHandler;
import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.MessageQueue;
import me.unrealization.jeeves.bot.RoleQueue;
import me.unrealization.jeeves.bot.WebClient;
import me.unrealization.jeeves.dataLists.EdsmUserList;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

public class Ccn extends BotModule implements UserJoinedHandler
{
	public Ccn()
	{
		this.version = "0.7.0";

		this.commandList = new String[3];
		this.commandList[0] = "ProximityCheck";
		this.commandList[1] = "GetCcnProximityRole";
		this.commandList[2] = "SetCcnProximityRole";

		this.defaultConfig.put("ccnProximityRole", "");
		//this.defaultConfig.put("ccnEdsmUseBetaServer", "0");
		this.defaultConfig.put("ccnEdsmId", "");
		this.defaultConfig.put("ccnEdsmApiKey", "");
		this.defaultConfig.put("ccnProximityRadius", "1000");
	}

	@Override
	public Long getDiscordId()
	{
		return Long.parseLong("209372315673165825");
	}

	@Override
	public void userJoinedHandler(UserJoinEvent event)
	{
		/*String message = "Welcome to the Colonia Citizens Network, " + event.getUser().getName() + "\n\n";
		message += "In order to make the most out of your experience here we have set up a number of roles which you can assign to yourself, using our bot Jeeves in our **#bots** channel. These roles allow access to special channels dedicated to different topics, where you can meet players who share your interests.\n\n";
		message += "The bot commands ``roles``, ``join`` and ``leave`` will help you to find out which roles are currently available for you to use, and allow you to give yourself a role, or take it away again.\n\n";
		message += "Please note that all bot commands have to be prefixed by pinging the bot using ``@Jeeves``\n\n";
		message += "To query what roles are available, type:\n\t``@Jeeves roles``\n\n";
		message += "To assign the role **Exploration Wing Member**:\n\t``@Jeeves join Exploration Wing Member``\n\n";
		message += "To remove the role **Exploration Wing Member**:\n\t``@Jeeves leave Exploration Wing Member``\n\n";
		message += "Our bot can also do quite a few other things to help you. Feel free to ask him for help using ``@Jeeves help``\n\n";
		message += "Have a pleasant stay on the Colonia Citizens Network Discord!\n";
		message += "The CCN Team";

		MessageQueue.sendMessage(event.getUser(), message);*/
	}

	private static class ProximityCheckModel
	{
		@SuppressWarnings("unused")
		public String countPublic;
		@SuppressWarnings("unused")
		public String countPrivate;
		public String[] commanders;
	}

	public static class ProximityCheck extends BotCommand
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
		public Permissions[] permissions()
		{
			Permissions[] permissionList = new Permissions[1];
			permissionList[0] = Permissions.MANAGE_SERVER;
			return permissionList;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			EdsmUserList edsmUserList;

			try
			{
				edsmUserList = new EdsmUserList();
			}
			catch (ParserConfigurationException | SAXException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM userlist is not available. Cannot process proximity check.");
				return;
			}

			String roleIdString = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "ccnProximityRole");

			if (roleIdString.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "No CCN proximity role has been set.");
				return;
			}

			long roleId = Long.parseLong(roleIdString);
			IRole role = message.getGuild().getRoleByID(roleId);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel(), "The current CCN proximity role is invalid.");
				//TODO Reset
				return;
			}

			String ccnEdsmId = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "ccnEdsmId");

			if (ccnEdsmId.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "No CCN EDSM Id has been set.");
				return;
			}

			String ccnEdsmApiKey = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "ccnEdsmApiKey");

			if (ccnEdsmApiKey.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "No CCN EDSM API Key has been set.");
				return;
			}

			String ccnProximityRadius = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "ccnProximityRadius");

			if (ccnProximityRadius.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "No proximity radius has been set.");
				return;
			}

			MessageQueue.sendMessage(message.getChannel(), "This might take a while.");
			String response;

			try
			{
				response = WebClient.getPage("https://www.edsm.net/tools/ccn/colonist?id=" + ccnEdsmId + "&apiKey=" + ccnEdsmApiKey + "&radius=" + ccnProximityRadius);
			}
			catch (IOException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "EDSM communication error.");
				return;
			}

			Ccn.ProximityCheckModel data = (Ccn.ProximityCheckModel)JSONHandler.parseJSON(response, Ccn.ProximityCheckModel.class);
			List<IUser> userList = message.getGuild().getUsers();
			String output = "";

			for (int userIndex = 0; userIndex < userList.size(); userIndex++)
			{
				IUser user = userList.get(userIndex);
				String userIdString = Long.toString(user.getLongID());

				if (edsmUserList.hasKey(userIdString) == false)
				{
					continue;
				}

				String edsmUserName = (String)edsmUserList.getValue(userIdString);

				if (user.hasRole(role) == true)
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
						output += user.getName() + " is no longer in Colonia.\n";
					}
				}
				else
				{
					for (int edsmUserIndex = 0; edsmUserIndex < data.commanders.length; edsmUserIndex++)
					{
						if (data.commanders[edsmUserIndex].toLowerCase().equals(edsmUserName.toLowerCase()) == true)
						{
							RoleQueue.addRoleToUser(role, user);
							output += user.getName() + " has arrived in Colonia.\n";
							break;
						}
					}
				}
			}

			if (output.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "No changes to process.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel(), output);
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
		public Permissions[] permissions()
		{
			Permissions[] permissionList = new Permissions[1];
			permissionList[0] = Permissions.MANAGE_SERVER;
			return permissionList;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String roleIdString = (String)Jeeves.serverConfig.getValue(message.getGuild().getLongID(), "ccnProximityRole");

			if (roleIdString.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "No CCN proximity role has been set.");
				return;
			}

			long roleId = Long.parseLong(roleIdString);
			IRole role = message.getGuild().getRoleByID(roleId);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel(), "A CCN proximity role has been set, but it does not exist.");

				Ccn ccn = new Ccn();
				Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "ccnProximityRole", ccn.getDefaultConfig().get("ccnProximityRole"));

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

			MessageQueue.sendMessage(message.getChannel(), "The CCN proximity role is: " + role.getName());
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
		public Permissions[] permissions()
		{
			Permissions[] permissionList = new Permissions[1];
			permissionList[0] = Permissions.MANAGE_SERVER;
			return permissionList;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String roleName = String.join(" ", arguments).trim();
			IRole role = null;

			if (roleName.isEmpty() == true)
			{
				Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "ccnProximityRole", "");
			}
			else
			{
				role = Jeeves.findRole(message.getGuild(), roleName);

				if (role == null)
				{
					MessageQueue.sendMessage(message.getChannel(), "Cannot find the role " + roleName);
					return;
				}

				String roleIdString = Long.toString(role.getLongID());
				Jeeves.serverConfig.setValue(message.getGuild().getLongID(), "ccnProximityRole", roleIdString);
			}

			try
			{
				Jeeves.serverConfig.saveConfig();
			}
			catch (ParserConfigurationException | TransformerException e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "Cannot store the setting.");
				return;
			}

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel(), "The CCN proximity role has been cleared.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel(), "The CCN proximity role has been set to: " + role.getName());
			}
		}
	}
}
