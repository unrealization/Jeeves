package me.unrealization.jeeves.apis;

import java.io.IOException;

import me.unrealization.jeeves.jsonModels.EdsmModels;
import me.unrealization.jeeves.bot.JSONHandler;
import me.unrealization.jeeves.bot.WebClient;
import me.unrealization.jeeves.interfaces.BotApi;

public class EdsmApi implements BotApi
{
	private boolean useBetaServer = true;

	private String getServerString()
	{
		if (this.useBetaServer == true)
		{
			return "http://beta.edsm.net:8080";
		}
		else
		{
			return "https://www.edsm.net";
		}
	}

	public void setUseBetaServer(boolean useBetaServer)
	{
		this.useBetaServer =  useBetaServer;
	}

	public EdsmModels.EDStatus getEDStatus() throws IOException
	{
		String response = WebClient.getPage(this.getServerString() + "/api-status-v1/elite-server");
		EdsmModels.EDStatus data = (EdsmModels.EDStatus)JSONHandler.parseJSON(response, EdsmModels.EDStatus.class);
		return data;
	}

	public EdsmModels.CommanderLocation locateCommander(String commanderName) throws IOException
	{
		String response = WebClient.getPage(this.getServerString() + "/api-logs-v1/get-position?commanderName=" + commanderName);
		EdsmModels.CommanderLocation data = (EdsmModels.CommanderLocation)JSONHandler.parseJSON(response, EdsmModels.CommanderLocation.class);
		return data;
	}
}
