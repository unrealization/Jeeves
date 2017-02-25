package me.unrealization.jeeves.interfaces;

import java.util.HashMap;

public interface BotModule
{
	public HashMap<String, String> getDefaultConfig();
	public String getHelp();
	public String getVersion();
	public String[] getCommands();
	public String getDiscordId();
	public boolean canDisable();
}
