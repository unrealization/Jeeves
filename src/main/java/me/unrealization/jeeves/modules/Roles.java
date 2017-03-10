package me.unrealization.jeeves.modules;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;
import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.MessageQueue;
import me.unrealization.jeeves.bot.RoleQueue;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;

public class Roles extends BotModule implements UserJoinedHandler
{
	public Roles()
	{
		this.version = "0.6";

		this.commandList = new String[6];
		this.commandList[0] = "GetRoles";
		this.commandList[1] = "Join";
		this.commandList[2] = "Leave";
		this.commandList[3] = "Members";
		this.commandList[4] = "GetAutoRole";
		this.commandList[5] = "SetAutoRole";

		this.defaultConfig.put("autoRole", "");
		this.defaultConfig.put("lockedRoles", new String[0]);
	}

	@Override
	public void userJoinedHandler(UserJoinEvent event)
	{
		String roleId = (String)Jeeves.serverConfig.getValue(event.getGuild().getID(), "autoRole");

		if (roleId.isEmpty() == true)
		{
			return;
		}

		IRole role = event.getGuild().getRoleByID(roleId);

		if (role == null)
		{
			Roles roles = new Roles();
			Jeeves.serverConfig.setValue(event.getGuild().getID(), "autoRole", roles.getDefaultConfig().get("autoRole"));
			return;
		}

		RoleQueue.addRoleToUser(role, event.getUser());
	}

	private static List<IRole> getManageableRoles(IGuild server) throws Exception
	{
		List<IRole> botRoles = Jeeves.bot.getOurUser().getRolesForGuild(server);

		int rolePosition = -1;

		for (int roleIndex = 0; roleIndex < botRoles.size(); roleIndex++)
		{
			IRole role = botRoles.get(roleIndex);

			if ((role.getPermissions().contains(Permissions.MANAGE_ROLES) == true) && ((rolePosition == -1) || (role.getPosition() > rolePosition)))
			{
				rolePosition = role.getPosition();
			}
		}

		if (rolePosition == -1)
		{
			throw new Exception("Missing permission " + Permissions.MANAGE_ROLES.name());
		}

		List<IRole> serverRoles = server.getRoles();
		List<IRole> manageableRoles = new ArrayList<IRole>();

		for (int roleIndex = 0; roleIndex < serverRoles.size(); roleIndex++)
		{
			IRole role = serverRoles.get(roleIndex);

			if (role.isEveryoneRole() == true)
			{
				continue;
			}

			if (role.getPosition() < rolePosition)
			{
				manageableRoles.add(role);
			}
		}

		return manageableRoles;
	}

	public static class GetRoles extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Check which roles the bot can manage.";
			return output;
		}

		@Override
		public String getParameters()
		{
			return null;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			List<IRole> manageableRoles;

			try
			{
				manageableRoles = Roles.getManageableRoles(message.getGuild());
			}
			catch (Exception e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "The bot does not have the permission to manage roles.");
				return;
			}

			if (manageableRoles.size() == 0)
			{
				MessageQueue.sendMessage(message.getChannel(), "No manageable roles found.");
				return;
			}

			String output = "The following roles can be managed by the bot:\n";

			for (int roleIndex = 0; roleIndex < manageableRoles.size(); roleIndex++)
			{
				IRole role = manageableRoles.get(roleIndex);
				output += "\t" + role.getName() + "\n";
			}

			MessageQueue.sendMessage(message.getChannel(), output);
		}
	}

	public static class Join extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Join one of the public roles.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<role>";
			return output;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String roleName = String.join(" ", arguments).trim();

			if (roleName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "You need to provide a role name.");
				return;
			}

			IRole role = Jeeves.findRole(message.getGuild(), roleName);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel(), "Cannot find the role " + roleName);
				return;
			}

			List<IRole> manageableRoles;

			try
			{
				manageableRoles = Roles.getManageableRoles(message.getGuild());
			}
			catch (Exception e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "The bot does not have the permission to manage roles.");
				return;
			}

			if (manageableRoles.contains(role) == false)
			{
				MessageQueue.sendMessage(message.getChannel(), "The bot is not allowed to manage the role " + role.getName());
				return;
			}

			List<IRole> userRoles = message.getAuthor().getRolesForGuild(message.getGuild());

			if (userRoles.contains(role) == true)
			{
				MessageQueue.sendMessage(message.getChannel(), message.getAuthor().getName() + " already has the role " + role.getName());
				return;
			}

			RoleQueue.addRoleToUser(role, message.getAuthor(), message.getChannel());
		}
	}

	public static class Leave extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Leave one of the public roles.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<role>";
			return output;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			String roleName = String.join(" ", arguments).trim();

			if (roleName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "You need to provide a role name.");
				return;
			}

			IRole role = Jeeves.findRole(message.getGuild(), roleName);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel(), "Cannot find the role " + roleName);
				return;
			}

			List<IRole> manageableRoles;

			try
			{
				manageableRoles = Roles.getManageableRoles(message.getGuild());
			}
			catch (Exception e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel(), "The bot does not have the permission to manage roles.");
				return;
			}

			if (manageableRoles.contains(role) == false)
			{
				MessageQueue.sendMessage(message.getChannel(), "The bot is not allowed to manage the role " + role.getName());
				return;
			}

			List<IRole> userRoles = message.getAuthor().getRolesForGuild(message.getGuild());

			if (userRoles.contains(role) == false)
			{
				MessageQueue.sendMessage(message.getChannel(), message.getAuthor().getName() + " does not have the role " + role.getName());
				return;
			}

			RoleQueue.removeRoleFromUser(role, message.getAuthor(), message.getChannel());
		}
	}

	public static class Members extends BotCommand
	{
		@Override
		public String getHelp()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getParameters()
		{
			String output = "<role>";
			return output;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			// TODO Auto-generated method stub
			
		}
	}

	public static class GetAutoRole extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Get the role automatically assigned to new users.";
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
			String roleId = (String)Jeeves.serverConfig.getValue(message.getGuild().getID(), "autoRole");

			if (roleId.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel(), "No automatically assigned role has been set.");
				return;
			}

			IRole role = message.getGuild().getRoleByID(roleId);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel(), "An automatically assigned role has been set, but it does not exist.");

				Roles roles = new Roles();
				Jeeves.serverConfig.setValue(message.getGuild().getID(), "autoRole", roles.getDefaultConfig().get("autoRole"));

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

			MessageQueue.sendMessage(message.getChannel(), "The automatically assigned role is: " + role.getName());
		}
	}

	public static class SetAutoRole extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Set or clear the role automatically assigned to new users.";
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
				Jeeves.serverConfig.setValue(message.getGuild().getID(), "autoRole", "");
			}
			else
			{
				role = Jeeves.findRole(message.getGuild(), roleName);

				if (role == null)
				{
					MessageQueue.sendMessage(message.getChannel(), "Cannot find the role " + roleName);
					return;
				}

				Jeeves.serverConfig.setValue(message.getGuild().getID(), "autoRole", role.getID());
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
				MessageQueue.sendMessage(message.getChannel(), "The automatically assigned role has been cleared.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel(), "The automatically assigned role has been set to: " + role.getName());
			}
		}
	}
}
