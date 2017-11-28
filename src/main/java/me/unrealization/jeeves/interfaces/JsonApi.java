package me.unrealization.jeeves.interfaces;

import java.io.IOException;

import me.unrealization.jeeves.bot.JSONHandler;
import me.unrealization.jeeves.bot.Jeeves;
import me.unrealization.jeeves.bot.WebClient;

import com.google.gson.JsonSyntaxException;

public abstract class JsonApi
{
	final protected Object apiCall(String url, Class<?> modelClass) throws IOException
	{
		String response = WebClient.getPage(this.getServerString() + url);
		Object data = null;

		try
		{
			data = JSONHandler.parseJSON(response, modelClass);
		}
		catch (JsonSyntaxException e)
		{
			Jeeves.debugException(e);
		}

		return data;
	}

	protected abstract String getServerString();
}
