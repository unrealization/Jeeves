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
		public String systemId;
		public String systemId64;
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

			public static class Materials
			{
				public String Carbon;
				public String Iron;
				public String Nickel;
				public String Phosphorus;
				public String Sulphur;
				public String Chromium;
				public String Germanium;
				public String Manganese;
				public String Vanadium;
				public String Zinc;
				public String Zirconium;
				public String Arsenic;
				public String Niobium;
				public String Selenium;
				public String Tungsten;
				public String Cadmium;
				public String Mercury;
				public String Molybdenum;
				public String Ruthenium;
				public String Tin;
				public String Yttrium;
				public String Antimony;
				public String Polonium;
				public String Technetium;
				public String Tellurium;
			}

			public String id;
			public String id64;
			public String name;
			public String type;
			public String subType;
			public String offset;
			public String distanceToArrival;
			public String isMainStar;
			public String isScoopable;
			public String age;
			public String luminosity;
			public String absoluteMagnitude;
			public String solarMasses;
			public String solarRadius;
			public String surfaceTemperature;
			public String isLandable;
			public String gravity;
			public String earthMasses;
			public String radius;
			public String volcanismType;
			public String atmosphereType;
			public String terraformingState;
			public String orbitalPeriod;
			public String semiMajorAxis;
			public String orbitalEccentricity;
			public String orbitalInclination;
			public String argOfPeriapsis;
			public String rotationalPeriod;
			public String rotationalPeriodTidallyLocked;
			public String axialTilt;
			public EdsmModels.SystemBodies.Body.Ring[] belts;
			public EdsmModels.SystemBodies.Body.Ring[] rings;
			public EdsmModels.SystemBodies.Body.Materials materials;
			public String updateTime;
		}

		public String id;
		public String id64;
		public String name;
		public EdsmModels.SystemBodies.Body[] bodies;
	}

	public static class SystemStations
	{
		public static class Station
		{
			public static class UpdateTime
			{
				public String information;
				public String market;
				public String shipyard;
				public String outfitting;
			}

			public String id;
			public String marketId;
			public String name;
			public String type;
			public String distanceToArrival;
			public String allegiance;
			public String government;
			public String economy;
			public String haveMarket;
			public String haveShipyard;
			public String[] otherServices;
			public EdsmModels.SystemFactions.Faction controllingFaction;
			public EdsmModels.SystemStations.Station.UpdateTime updateTime;
		}

		public String id;
		public String id64;
		public String name;
		public EdsmModels.SystemStations.Station[] stations;
	}

	public static class SystemFactions
	{
		public static class Faction
		{
			public static class State
			{
				public String state;
				public String trend;
			}

			public String id;
			public String name;
			public String allegiance;
			public String government;
			public String influence;
			public String state;
			public EdsmModels.SystemFactions.Faction.State[] recoveringStates;
			public EdsmModels.SystemFactions.Faction.State[] pendingStates;
			public String isPlayer;
			public String lastUpdate;
		}

		public String id;
		public String id64;
		public String name;
		public EdsmModels.SystemFactions.Faction controllingFaction;
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
		//public EdsmModels.SystemInfo.SystemInformation information;
		public EdsmModels.SystemInfo.PrimaryStar primaryStar;
	}
}
