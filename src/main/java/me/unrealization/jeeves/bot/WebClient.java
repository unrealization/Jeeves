package me.unrealization.jeeves.bot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebClient
{
	private static String getPage(URL url, String method, String parameters, boolean allowRetry) throws IOException
	{
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod(method);
		connection.setRequestProperty("User-Agent", "Jeeves " + Jeeves.version + " (Discord Bot)");

		if (method.equals("POST") == true)
		{
			connection.setDoOutput(true);
			DataOutputStream paramWriter = new DataOutputStream(connection.getOutputStream());
			paramWriter.writeBytes(parameters);
			paramWriter.flush();
			paramWriter.close();
		}

		int responseCode = connection.getResponseCode();

		if ((responseCode == 429) && (allowRetry == true))
		{
			int delay = connection.getHeaderFieldInt("Retry-After", -2) + 1;
			System.out.println("Retrying after " + Integer.toString(delay) + " seconds.");

			if (delay == -1)
			{
				System.out.println("Server wants me to wait, but doesn't tell me how long.");
				return null;
			}

			try
			{
				Thread.sleep(delay * 1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				return null;
			}

			return WebClient.getPage(url, method, parameters, false);
		}

		if (responseCode != HttpURLConnection.HTTP_OK)
		{
			System.out.println("Status Code: " + responseCode);
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
		connection.disconnect();
		return response.toString();
	}

	public static String getPage(URL url, String method, String parameters) throws IOException
	{
		return WebClient.getPage(url, method, parameters, true);
	}

	public static String getPage(URL url, String method) throws IOException
	{
		return WebClient.getPage(url, method, "", true);
	}

	public static String getPage(URL url) throws IOException
	{
		return WebClient.getPage(url, "GET", "", true);
	}

	public static String getPage(String address, String method, String parameters) throws IOException
	{
		URL url = new URL(address);
		return WebClient.getPage(url, method, parameters, true);
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
