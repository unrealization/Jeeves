package me.unrealization.jeeves.modules;

import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.interfaces.BotCommand;
import me.unrealization.jeeves.interfaces.BotModule;
import me.unrealization.jeeves.interfaces.UserJoinedHandler;

public class Roles extends BotModule implements UserJoinedHandler
{
	public Roles()
	{
		this.version = "0.4.1";

		this.commandList = new String[4];
		this.commandList[0] = "GetRoles";
		this.commandList[1] = "Join";
		this.commandList[2] = "Leave";
		this.commandList[3] = "Members";

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

		try
		{
			event.getUser().addRole(role);
		}
		catch (MissingPermissionsException | RateLimitException | DiscordException e)
		{
			Jeeves.debugException(e);
		}
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
				Jeeves.sendMessage(message.getChannel(), "The bot does not have the permission to manage roles.");
				return;
			}

			if (manageableRoles.size() == 0)
			{
				Jeeves.sendMessage(message.getChannel(), "No manageable roles found.");
				return;
			}

			String output = "The following roles can be managed by the bot:\n";

			for (int roleIndex = 0; roleIndex < manageableRoles.size(); roleIndex++)
			{
				IRole role = manageableRoles.get(roleIndex);
				output += "\t" + role.getName() + "\n";
			}

			Jeeves.sendMessage(message.getChannel(), output);
		}
	}

	public static class Join extends BotCommand
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
			String roleName = String.join(" ", arguments).trim();

			if (roleName.isEmpty() == true)
			{
				Jeeves.sendMessage(message.getChannel(), "You need to provide a role name.");
				return;
			}

			IRole role = Jeeves.findRole(message.getGuild(), roleName);

			if (role == null)
			{
				Jeeves.sendMessage(message.getChannel(), "Cannot find the role " + roleName);
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
				Jeeves.sendMessage(message.getChannel(), "The bot does not have the permission to manage roles.");
				return;
			}

			if (manageableRoles.contains(role) == false)
			{
				Jeeves.sendMessage(message.getChannel(), "The bot is not allowed to manage the role " + role.getName());
				return;
			}

			List<IRole> userRoles = message.getAuthor().getRolesForGuild(message.getGuild());

			if (userRoles.contains(role) == true)
			{
				Jeeves.sendMessage(message.getChannel(), message.getAuthor().getName() + " already has the role " + role.getName());
				return;
			}

			try
			{
				message.getAuthor().addRole(role);
			}
			catch (MissingPermissionsException | RateLimitException | DiscordException e)
			{
				Jeeves.debugException(e);
				Jeeves.sendMessage(message.getChannel(), "Cannot add the role " + role.getName() + " to " + message.getAuthor().getName());
				return;
			}

			Jeeves.sendMessage(message.getChannel(), "Added the role " + role.getName() + " to " + message.getAuthor().getName());
		}
	}

	public static class Leave extends BotCommand
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
			String roleName = String.join(" ", arguments).trim();

			if (roleName.isEmpty() == true)
			{
				Jeeves.sendMessage(message.getChannel(), "You need to provide a role name.");
				return;
			}

			IRole role = Jeeves.findRole(message.getGuild(), roleName);

			if (role == null)
			{
				Jeeves.sendMessage(message.getChannel(), "Cannot find the role " + roleName);
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
				Jeeves.sendMessage(message.getChannel(), "The bot does not have the permission to manage roles.");
				return;
			}

			if (manageableRoles.contains(role) == false)
			{
				Jeeves.sendMessage(message.getChannel(), "The bot is not allowed to manage the role " + role.getName());
				return;
			}

			List<IRole> userRoles = message.getAuthor().getRolesForGuild(message.getGuild());

			if (userRoles.contains(role) == false)
			{
				Jeeves.sendMessage(message.getChannel(), message.getAuthor().getName() + " does not have the role " + role.getName());
				return;
			}

			try
			{
				message.getAuthor().removeRole(role);
			}
			catch (MissingPermissionsException | RateLimitException | DiscordException e)
			{
				Jeeves.debugException(e);
				Jeeves.sendMessage(message.getChannel(), "Cannot remove the role " + role.getName() + " from " + message.getAuthor().getName());
				return;
			}

			Jeeves.sendMessage(message.getChannel(), "Removed the role " + role.getName() + " from " + message.getAuthor().getName());
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
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void execute(IMessage message, String[] arguments)
		{
			// TODO Auto-generated method stub
			
		}
	}
}
