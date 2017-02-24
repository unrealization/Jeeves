package me.unrealization.jeeves.bot;

import com.google.gson.Gson;

public class JSONHandler
{
	public static Object parseJSON(String jsonData, Class modelClass)
	{
		Gson gson = new Gson();
		Object model = gson.fromJson(jsonData, modelClass);
		return model;
	}

	public static String writeJSON(Object model)
	{
		Gson gson = new Gson();
		String jsonData = gson.toJson(model);
		return jsonData;
	}
}
