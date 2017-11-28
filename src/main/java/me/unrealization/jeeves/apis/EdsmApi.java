package me.unrealization.jeeves.apis;

import java.io.IOException;

import me.unrealization.jeeves.interfaces.JsonApi;
import me.unrealization.jeeves.jsonModels.EdsmModels;

public class EdsmApi extends JsonApi
{
	private boolean useBetaServer = true;

	protected String getServerString()
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
		this.useBetaServer = useBetaServer;
	}

	public EdsmModels.EDStatus getEDStatus() throws IOException
	{
		EdsmModels.EDStatus data = (EdsmModels.EDStatus)this.apiCall("/api-status-v1/elite-server", EdsmModels.EDStatus.class);
		return data;
	}

	public EdsmModels.CommanderLocation getCommanderLocation(String commanderName) throws IOException
	{
		EdsmModels.CommanderLocation data = (EdsmModels.CommanderLocation)this.apiCall("/api-logs-v1/get-position?commanderName=" + commanderName + "&showCoordinates=1", EdsmModels.CommanderLocation.class);
		return data;
	}

	public EdsmModels.SystemInfo getSystemInfo(String systemName) throws IOException
	{
		EdsmModels.SystemInfo data = (EdsmModels.SystemInfo)this.apiCall("/api-v1/system?systemName=" + systemName + "&showId=1&showCoordinates=1&showPermit=1&showInformation=1&showPrimaryStar=1", EdsmModels.SystemInfo.class);
		return data;
	}

	public EdsmModels.SystemBodies getSystemBodies(String systemName) throws IOException
	{
		EdsmModels.SystemBodies data = (EdsmModels.SystemBodies)this.apiCall("/api-system-v1/bodies?systemName=" + systemName, EdsmModels.SystemBodies.class);
		return data;
	}

	public EdsmModels.SystemStations getSystemStations(String systemName) throws IOException
	{
		EdsmModels.SystemStations data = (EdsmModels.SystemStations)this.apiCall("/api-system-v1/stations?systemName=" + systemName, EdsmModels.SystemStations.class);
		return data;
	}

	public EdsmModels.SystemFactions getSystemFactions(String systemName) throws IOException
	{
		EdsmModels.SystemFactions data = (EdsmModels.SystemFactions)this.apiCall("/api-system-v1/factions?systemName=" + systemName, EdsmModels.SystemFactions.class);
		return data;
	}
}
