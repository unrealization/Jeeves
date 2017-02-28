package me.unrealization.jeeves.jsonModels;

public class EdsmModels
{
	public static class EDStatus
	{
		public String lastUpdate;
		public String type;
		public String message;
		public String status;
	}

	public static class CommanderLocation
	{
		public String msgnum;
		public String msg;
		public String system;
		public String firstDiscover;
		public String date;
	}

	public static class SystemCoordinates
	{
		public static class Coordinates
		{
			public String x;
			public String y;
			public String z;
		}

		public String name;
		public Coordinates coords;
	}
}
