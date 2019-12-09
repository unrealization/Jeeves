package me.unrealization.jeeves.modules;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
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
		this.version = "2.0.0";

		this.commandList = new String[13];
		this.commandList[0] = "GetRoles";
		this.commandList[1] = "Join";
		this.commandList[2] = "Leave";
		this.commandList[3] = "Members";
		this.commandList[4] = "MissingRole";
		this.commandList[5] = "GetUntaggedUsers";
		this.commandList[6] = "GetAutoRole";
		this.commandList[7] = "SetAutoRole";
		this.commandList[8] = "LockRole";
		this.commandList[9] = "UnlockRole";
		this.commandList[10] = "GetLockedRoles";
		this.commandList[11] = "AssignRole";
		this.commandList[12] = "UnassignRole";

		this.defaultConfig.put("autoRole", "");
		this.defaultConfig.put("lockedRoles", new ArrayList<String>());
	}

	@Override
	public void userJoinedHandler(MemberJoinEvent event)
	{
		String roleIdString = (String)Jeeves.serverConfig.getValue(event.getGuildId().asLong(), "autoRole");

		if (roleIdString.isEmpty() == true)
		{
			return;
		}

		long roleId = Long.parseLong(roleIdString);
		Role role = event.getGuild().block().getRoleById(Snowflake.of(roleId)).block();

		if (role == null)
		{
			Roles roles = new Roles();
			Jeeves.serverConfig.setValue(event.getGuildId().asLong(), "autoRole", roles.getDefaultConfig().get("autoRole"));
			return;
		}

		RoleQueue.addRoleToUser(role, event.getMember());
	}

	private static List<Role> getManageableRoles(Guild server) throws Exception
	{
		Iterable<Role> botRoles = Jeeves.bot.getSelf().block().asMember(server.getId()).block().getRoles().toIterable();
		int rolePosition = -1;

		for (Role role : botRoles)
		{
			if ((role.getPermissions().contains(Permission.MANAGE_ROLES) == true) && ((rolePosition == -1) || (role.getPosition().block() > rolePosition)))
			{
				rolePosition = role.getPosition().block();
			}
		}

		if (rolePosition == -1)
		{
			throw new Exception("Missing permission " + Permission.MANAGE_ROLES.name());
		}

		Iterable<Role> serverRoles = server.getRoles().toIterable();
		List<Role> manageableRoles = new ArrayList<Role>();

		for (Role role : serverRoles)
		{
			if (role.isEveryone() == true)
			{
				continue;
			}

			if (role.getPosition().block() < rolePosition)
			{
				manageableRoles.add(role);
			}
		}

		return manageableRoles;
	}

	private static boolean isLocked(Role role)
	{
		Object lockedRoles = Jeeves.serverConfig.getValue(role.getGuildId().asLong(), "lockedRoles");

		if (lockedRoles.getClass() == String.class)
		{
			return false;
		}

		List<String> lockedRoleList = Jeeves.listToStringList((List<?>)lockedRoles);
		String roleIdString = role.getId().asString();
		return lockedRoleList.contains(roleIdString);
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
		public void execute(Message message, String argumentString)
		{
			List<Role> manageableRoles;

			try
			{
				manageableRoles = Roles.getManageableRoles(message.getGuild().block());
			}
			catch (Exception e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel().block(), "The bot does not have the permission to manage roles.");
				return;
			}

			if (manageableRoles.size() == 0)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No manageable roles found.");
				return;
			}

			String output = "The following roles can be managed by the bot:\n";
			int foundRoles = 0;

			for (int roleIndex = 0; roleIndex < manageableRoles.size(); roleIndex++)
			{
				Role role = manageableRoles.get(roleIndex);

				if (Roles.isLocked(role) == true)
				{
					continue;
				}

				foundRoles++;
				output += "\t" + role.getName() + "\n";
			}

			if (foundRoles == 0)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "All manageable roles are locked.");
				return;
			}

			MessageQueue.sendMessage(message.getChannel().block(), output);
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
		public void execute(Message message, String roleName)
		{
			if (roleName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "You need to provide a role name.");
				return;
			}

			Role role = Jeeves.findRole(message.getGuild().block(), roleName);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the role " + roleName);
				return;
			}

			List<Role> manageableRoles;

			try
			{
				manageableRoles = Roles.getManageableRoles(message.getGuild().block());
			}
			catch (Exception e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel().block(), "The bot does not have the permission to manage roles.");
				return;
			}

			if (manageableRoles.contains(role) == false)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The bot is not allowed to manage the role " + role.getName());
				return;
			}

			if (Roles.isLocked(role) == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The role " + role.getName() + " is locked.");
				return;
			}

			if (message.getAuthorAsMember().block().getRoleIds().contains(role.getId()) == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), message.getAuthor().get().getUsername() + " already has the role " + role.getName());
				return;
			}

			RoleQueue.addRoleToUser(role, message.getAuthorAsMember().block(), message.getChannel().block());
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
		public void execute(Message message, String roleName)
		{
			if (roleName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "You need to provide a role name.");
				return;
			}

			Role role = Jeeves.findRole(message.getGuild().block(), roleName);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the role " + roleName);
				return;
			}

			List<Role> manageableRoles;

			try
			{
				manageableRoles = Roles.getManageableRoles(message.getGuild().block());
			}
			catch (Exception e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel().block(), "The bot does not have the permission to manage roles.");
				return;
			}

			if (manageableRoles.contains(role) == false)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The bot is not allowed to manage the role " + role.getName());
				return;
			}

			if (Roles.isLocked(role) == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The role " + role.getName() + " is locked.");
				return;
			}

			if (message.getAuthorAsMember().block().getRoleIds().contains(role.getId()) == false)
			{
				MessageQueue.sendMessage(message.getChannel().block(), message.getAuthor().get().getUsername() + " does not have the role " + role.getName());
				return;
			}

			RoleQueue.removeRoleFromUser(role, message.getAuthorAsMember().block(), message.getChannel().block());
		}
	}

	public static class Members extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Get a list of users with the given role.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<role>";
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
			if (roleName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "You need to provide a role name.");
				return;
			}

			Role role = Jeeves.findRole(message.getGuild().block(), roleName);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the role " + roleName);
			}

			List<User> userList = new ArrayList<User>();
			Iterable<Member> serverUserList = message.getGuild().block().getMembers().toIterable();

			for (Member serverUser : serverUserList)
			{
				if (serverUser.getRoleIds().contains(role.getId()) == true)
				{
					userList.add(serverUser);
				}
			}

			if (userList.size() == 0)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The role " + role.getName() + " has no members.");
				return;
			}

			String output = "The role " + role.getName() + " has the following members:\n";

			for (int userIndex = 0; userIndex < userList.size(); userIndex++)
			{
				output += "\t" + userList.get(userIndex).getUsername() + "\n";
			}

			MessageQueue.sendMessage(message.getAuthor().get(), output);
			MessageQueue.sendMessage(message.getChannel().block(), "Member list sent as private message.");
		}
	}

	public static class MissingRole extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Get a list of users missing the given role.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<role>";
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
			if (roleName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "You need to provide a role name.");
				return;
			}

			Role role = Jeeves.findRole(message.getGuild().block(), roleName);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the role " + roleName);
			}

			Iterable<Member> userList = message.getGuild().block().getMembers().toIterable();
			List<User> usersMissingRole = new ArrayList<User>();

			for (Member user : userList)
			{
				if (user.getRoleIds().contains(role.getId()) == true)
				{
					continue;
				}

				usersMissingRole.add(user);
			}

			if (usersMissingRole.size() == 0)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No users are missing the role " + role.getName());
				return;
			}

			String output = "The following users are missing the role " + role.getName() + "\n";

			for (int userIndex = 0; userIndex < usersMissingRole.size(); userIndex++)
			{
				output += "\t" + usersMissingRole.get(userIndex).getUsername() + "\n";
			}

			MessageQueue.sendMessage(message.getAuthor().get(), output);
			MessageQueue.sendMessage(message.getChannel().block(), "User list sent as private message.");
		}
	}

	public static class GetUntaggedUsers extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Get a list users without any roles.";
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
			Iterable<Member> userList = message.getGuild().block().getMembers().toIterable();
			List<User> untaggedUsers = new ArrayList<User>();

			for (Member user : userList)
			{
				if (user.getRoleIds().isEmpty() == false)
				{
					continue;
				}

				untaggedUsers.add(user);
			}

			if (untaggedUsers.size() == 0)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "There are no untagged users on this Discord.");
				return;
			}

			String output = "The following users have no role:\n";

			for (int userIndex = 0; userIndex < untaggedUsers.size(); userIndex++)
			{
				output += "\t" + untaggedUsers.get(userIndex).getUsername() + "\n";
			}

			MessageQueue.sendMessage(message.getAuthor().get(), output);
			MessageQueue.sendMessage(message.getChannel().block(), "User list sent as private message.");
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
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_GUILD;
			return permissionList;
		}

		@Override
		public void execute(Message message, String argumentString)
		{
			String roleIdString = (String)Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "autoRole");

			if (roleIdString.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No automatically assigned role has been set.");
				return;
			}

			long roleId = Long.parseLong(roleIdString);
			Role role = message.getGuild().block().getRoleById(Snowflake.of(roleId)).block();

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "An automatically assigned role has been set, but it does not exist.");

				Roles roles = new Roles();
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "autoRole", roles.getDefaultConfig().get("autoRole"));

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

			MessageQueue.sendMessage(message.getChannel().block(), "The automatically assigned role is: " + role.getName());
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
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "autoRole", "");
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
				Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "autoRole", roleIdString);
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
				MessageQueue.sendMessage(message.getChannel().block(), "The automatically assigned role has been cleared.");
			}
			else
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The automatically assigned role has been set to: " + role.getName());
			}
		}
	}

	public static class LockRole extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Lock a role so users can no longer assign it to themselves.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<role>";
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
			if (roleName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "You need to provide a role name.");
				return;
			}

			Role role = Jeeves.findRole(message.getGuild().block(), roleName);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the role " + roleName);
				return;
			}

			if (Roles.isLocked(role) == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The role " + role.getName() + " is locked already.");
				return;
			}

			Object lockedRoles = Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "lockedRoles");
			List<String> lockedRoleList;

			if (lockedRoles.getClass() == String.class)
			{
				lockedRoleList = new ArrayList<String>();
			}
			else
			{
				lockedRoleList = Jeeves.listToStringList((List<?>)lockedRoles);
			}

			String roleIdString = role.getId().asString();
			lockedRoleList.add(roleIdString);
			Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "lockedRoles", lockedRoleList);

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

			MessageQueue.sendMessage(message.getChannel().block(), "The following role has been locked: " + role.getName());
		}
	}

	public static class UnlockRole extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Unlock a role so users can assign it to themselves.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<role>";
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
			if (roleName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "You need to provide a role name.");
				return;
			}

			Role role = Jeeves.findRole(message.getGuild().block(), roleName);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the role " + roleName);
				return;
			}

			Object lockedRoles = Jeeves.serverConfig.getValue(message.getGuild().block().getId().asLong(), "lockedRoles");

			if (lockedRoles.getClass() == String.class)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No roles are locked.");
				return;
			}

			List<String> lockedRoleList = Jeeves.listToStringList((List<?>)lockedRoles);

			if (lockedRoleList.size() == 0)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No roles are locked.");
				return;
			}

			String roleIdString = role.getId().asString();
			boolean removed = lockedRoleList.remove(roleIdString);

			if (removed == false)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The role " + role.getName() + " is not locked.");
				return;
			}

			Jeeves.serverConfig.setValue(message.getGuild().block().getId().asLong(), "lockedRoles", lockedRoleList);

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

			MessageQueue.sendMessage(message.getChannel().block(), "The following role has been unlocked: " + role.getName());
		}
	}

	public static class GetLockedRoles extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Get the list of locked roles.";
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
			List<Role> manageableRoles;

			try
			{
				manageableRoles = Roles.getManageableRoles(message.getGuild().block());
			}
			catch (Exception e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel().block(), "The bot does not have the permission to manage roles.");
				return;
			}

			if (manageableRoles.size() == 0)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "No manageable roles found.");
				return;
			}

			String output = "The following roles are locked:\n";
			int foundRoles = 0;

			for (int roleIndex = 0; roleIndex < manageableRoles.size(); roleIndex++)
			{
				Role role = manageableRoles.get(roleIndex);

				if (Roles.isLocked(role) == false)
				{
					continue;
				}

				foundRoles++;
				output += "\t" + role.getName() + "\n";
			}

			if (foundRoles == 0)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "None of the manageable roles are locked.");
				return;
			}

			MessageQueue.sendMessage(message.getChannel().block(), output);
		}
	}

	public static class AssignRole extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Assign a role to another user.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<user> : <role>";
			return output;
		}

		@Override
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_ROLES;
			return permissionList;
		}

		@Override
		public void execute(Message message, String argumentString)
		{
			String[] arguments = Jeeves.splitArguments(argumentString);

			if (arguments.length < 2)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Insufficient amount of parameters.\n" + this.getParameters());
				return;
			}

			String userName = arguments[0];
			Member user;

			if (userName.isEmpty() == true)
			{
				user = message.getAuthorAsMember().block();
			}
			else
			{
				user = Jeeves.findUser(message.getGuild().block(), userName);
			}

			if (user == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the user " + userName);
			}

			String roleName = arguments[1];

			if (roleName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "You need to provide a role name.");
				return;
			}

			Role role = Jeeves.findRole(message.getGuild().block(), roleName);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the role " + roleName);
			}

			List<Role> manageableRoles;

			try
			{
				manageableRoles = Roles.getManageableRoles(message.getGuild().block());
			}
			catch (Exception e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel().block(), "The bot does not have the permission to manage roles.");
				return;
			}

			if (manageableRoles.contains(role) == false)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The bot is not allowed to manage the role " + role.getName());
				return;
			}

			if (user.getRoleIds().contains(role.getId()) == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), user.getDisplayName() + " already has the role " + role.getName());
				return;
			}

			RoleQueue.addRoleToUser(role, user, message.getChannel().block());
		}
	}

	public static class UnassignRole extends BotCommand
	{
		@Override
		public String getHelp()
		{
			String output = "Remove a role from another user.";
			return output;
		}

		@Override
		public String getParameters()
		{
			String output = "<user> : <role>";
			return output;
		}

		@Override
		public Permission[] permissions()
		{
			Permission[] permissionList = new Permission[1];
			permissionList[0] = Permission.MANAGE_ROLES;
			return permissionList;
		}

		@Override
		public void execute(Message message, String argumentString)
		{
			String[] arguments = Jeeves.splitArguments(argumentString);

			if (arguments.length < 2)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Insufficient amount of parameters.\n" + this.getParameters());
				return;
			}

			String userName = arguments[0];
			Member user;

			if (userName.isEmpty() == true)
			{
				user = message.getAuthorAsMember().block();
			}
			else
			{
				user = Jeeves.findUser(message.getGuild().block(), userName);
			}

			if (user == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the user " + userName);
			}

			String roleName = arguments[1];

			if (roleName.isEmpty() == true)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "You need to provide a role name.");
				return;
			}

			Role role = Jeeves.findRole(message.getGuild().block(), roleName);

			if (role == null)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "Cannot find the role " + roleName);
			}

			List<Role> manageableRoles;

			try
			{
				manageableRoles = Roles.getManageableRoles(message.getGuild().block());
			}
			catch (Exception e)
			{
				Jeeves.debugException(e);
				MessageQueue.sendMessage(message.getChannel().block(), "The bot does not have the permission to manage roles.");
				return;
			}

			if (manageableRoles.contains(role) == false)
			{
				MessageQueue.sendMessage(message.getChannel().block(), "The bot is not allowed to manage the role " + role.getName());
				return;
			}

			if (user.getRoleIds().contains(role.getId()) == false)
			{
				MessageQueue.sendMessage(message.getChannel().block(), user.getDisplayName() + " does not have the role " + role.getName());
				return;
			}

			RoleQueue.removeRoleFromUser(role, user, message.getChannel().block());
		}
	}
}
