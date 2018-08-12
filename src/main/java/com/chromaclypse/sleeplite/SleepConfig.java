package com.chromaclypse.sleeplite;

import java.util.Map;

import com.chromaclypse.api.Defaults;
import com.chromaclypse.api.config.ConfigObject;

public class SleepConfig extends ConfigObject {
	
	public Messages messages = new Messages();
	public static class Messages {
		public String notEnough = "&e%needed% more player%s% needed to skip the %condition% in %world%";
		public String skipping = "&3Skipping the %condition% in %world%, rise and shine!";
	}
	
	public Map<String, SleepWorld> worlds = Defaults.Keys("default").Values(new SleepWorld());
	public static class SleepWorld {
		public boolean enabled = true;
		public String customName = "";
		public double ratio = 0.5;
		public int minimum = 1;
		public int maximum = 20;
		
		public SleepIgnored ignoring = new SleepIgnored();
		public static class SleepIgnored {
			public boolean creative = true;
			public boolean spectator = true;
			public boolean dead = true;
		}
	}
}
