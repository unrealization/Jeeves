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
		public EdsmModels.SystemInfo.Coordinates coordinates;
		public String isDocked;
		public String dateLastActivity;
		public String url;
	}

	public static class SystemBodies
	{
		public static class Body
		{
			public static class Ring
			{
				public String name;
				public String type;
				public String mass;
				public String innerRadius;
				public String outerRadius;
			}

			public String id;
			public String name;
			public String type;
			public String subType;
			public String distanceToArrival;
			//stars
			public String isMainStar;
			public String isScoopable;
			public String age;
			public String luminosity;
			public String absoluteMagnitude;
			public String solarMasses;
			public String solarRadius;
			public String surfaceTemperature;
			//planets
			public String isLandable;
			public String gravity;
			public String earthMasses;
			public String radius;
			public String volcanismType;
			public String atmosphereType;
			public String terraformingState;
			//common
			public String orbitalPeriod;
			public String semiMajorAxis;
			public String orbitalEccentricity;
			public String orbitalInclination;
			public String argOfPeriapsis;
			public String rotationalPeriod;
			public String rotationalPeriodTidallyLocked;
			public String axialTilt;
			public EdsmModels.SystemBodies.Body.Ring[] rings;
		}

		public String id;
		public String name;
		public EdsmModels.SystemBodies.Body[] bodies;
	}

	public static class SystemStations
	{
		public static class Station
		{
			public static class Faction
			{
				public String id;
				public String name;
			}

			public String id;
			public String name;
			public String type;
			public String distanceToArrival;
			public String allegiance;
			public String government;
			public String economy;
			public String haveMarket;
			public String haveShipyard;
			public EdsmModels.SystemStations.Station.Faction controllingFaction;
		}

		public String id;
		public String name;
		public EdsmModels.SystemStations.Station[] stations;
	}

	public static class SystemFactions
	{
		public static class ControllingFaction
		{
			public String id;
			public String name;
			public String allegiance;
			public String government;
		}

		public static class Faction
		{
			public String id;
			public String name;
			public String allegiance;
			public String government;
			public String influence;
			public String state;
			public String isPlayer;
		}

		public String id;
		public String name;
		public EdsmModels.SystemFactions.ControllingFaction controllingFaction;
		public EdsmModels.SystemFactions.Faction factions[];
	}

	public static class SystemInfo
	{
		public static class Coordinates
		{
			public String x;
			public String y;
			public String z;
		}

		public static class SystemInformation
		{
			public String allegiance;
			public String government;
			public String faction;
			public String factionState;
			public String population;
			public String reserve;
			public String security;
			public String economy;
		}

		public static class PrimaryStar
		{
			public String type;
			public String name;
			public String isScoopable;
		}

		public String name;
		public String id;
		public String id64;
		public EdsmModels.SystemInfo.Coordinates coords;
		public String requirePermit;
		public EdsmModels.SystemInfo.SystemInformation information;
		public EdsmModels.SystemInfo.PrimaryStar primaryStar;
	}
}
