package me.unrealization.jeeves.bot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebClient
{
	public static String getPage(URL url, String method, String parameters) throws IOException
	{
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod(method);
		connection.setRequestProperty("User-Agent", "Java Test");

		if (method.equals("POST") == true)
		{
			connection.setDoOutput(true);
			DataOutputStream paramWriter = new DataOutputStream(connection.getOutputStream());
			paramWriter.writeBytes(parameters);
			paramWriter.flush();
			paramWriter.close();
		}

		int responseCode = connection.getResponseCode();

		System.out.println("Status Code: " + responseCode);

		if (responseCode != 200)
		{
			return null;
		}

		BufferedReader webReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuffer response = new StringBuffer();
		String line;

		while ((line = webReader.readLine()) != null)
		{
			response.append(line);
		}

		webReader.close();
		return response.toString();
	}

	public static String getPage(URL url, String method) throws IOException
	{
		return WebClient.getPage(url, method, "");
	}

	public static String getPage(URL url) throws IOException
	{
		return WebClient.getPage(url, "GET", "");
	}

	public static String getPage(String address, String method, String parameters) throws IOException
	{
		URL url = new URL(address);
		return WebClient.getPage(url, method, parameters);
	}

	public static String getPage(String address, String method) throws IOException
	{
		return WebClient.getPage(address, method, "");
	}

	public static String getPage(String address) throws IOException
	{
		return WebClient.getPage(address, "GET", "");
	}
}
