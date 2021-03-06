package com.chromaclypse.sleeplite;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;

import com.chromaclypse.api.messages.Text;
import com.chromaclypse.sleeplite.SleepConfig.SleepWorld;
import com.chromaclypse.sleeplite.SleepConfig.SleepWorld.SleepIgnored;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class Control {
	private SleepConfig config;
	private HashMap<String, Integer> sleepNeeded = new HashMap<>();
	
	public Control(SleepConfig config) {
		this.config = config;
	}
	
	private boolean ignoring(Player p) {
		SleepIgnored ignoring = getWorldConfig(p.getWorld().getName()).ignoring;
		return p.isSleepingIgnored()
				|| ignoring.creative && p.getGameMode() == GameMode.CREATIVE
				|| ignoring.spectator && p.getGameMode() == GameMode.SPECTATOR
				|| ignoring.dead && p.isDead()
				|| p.hasPermission("sleeplite.ignore");
	}
	
	//public HashMap<String, Long> fakePlayers = new HashMap<>();

	private int countablePlayers(World world) {
		int countable = world.getPlayers().size();// + fakePlayers.size();
		for(Player player : world.getPlayers())
			if(!player.isSleeping() && ignoring(player))
				--countable;
		
		return countable;
	}
	
	private SleepWorld getWorldConfig(String worldName) {
		SleepWorld worldConfig = config.worlds.get(worldName);
		if(worldConfig == null)
			worldConfig = config.worlds.get("default");
		
		return worldConfig;
	}
	
	public int getNeeded(int sleeping, int total, SleepWorld worldConfig) {
		// Pick the largest of needed players to fit ratio and needed players for minimum
		int minPlayers = Math.min(worldConfig.minimum, total);
		int ratioPlayers = Math.max((int)Math.ceil(worldConfig.ratio * total), 1);
		
		return Math.min(Math.max(ratioPlayers, minPlayers), worldConfig.maximum) - sleeping;
	}
	
	public static final boolean isNight(long time) {
		return time >= 12541 && time <= 23458;
	}

	public void checkSkip(WorldData data) {
		checkSkip(data, false);
	}
	
	public void checkSkip(WorldData data, boolean ignoreLast) {
		//Log.info("calling checkSkip");
		World world = data.getWorld();

		boolean isNight = isNight(world.getTime());

		if(!isNight && !world.isThundering()) {
			return;
		}

		String worldName = world.getName();
		SleepWorld worldConfig = getWorldConfig(worldName);
		if(worldConfig == null || !worldConfig.enabled)
			return;
		
		int sleep = data.getSleepers();
		int total = countablePlayers(world);
		//Log.info("Checking sleep: " + sleep + "/" + total);
		int needed = getNeeded(sleep, total, worldConfig);

		int oldNeeded;
		{
			Integer i = sleepNeeded.get(world.getName());

			if(i == null) {
				oldNeeded = needed + 1;
			}
			else {
				oldNeeded = i.intValue();
			}
		}

		if(!ignoreLast) {
			if(sleep == 0 || oldNeeded == needed) {
				return;
			}
		}

		String fancyName = worldConfig.customName.length() > 0 ? worldConfig.customName : worldName;
		String condition = isNight ? "night" : "storm";
		String message;
		
		if(needed > 0) {
			sleepNeeded.put(world.getName(), needed);

			message = config.messages.notEnough
					.replaceAll("%needed%", String.valueOf(needed))
					.replaceAll("%s%", needed == 1 ? "" : "s")
					.replaceAll("%condition%", condition)
					.replaceAll("%world%", fancyName);
		}
		else {
			sleepNeeded.remove(world.getName());
			data.reset();
			world.setTime(0);
			world.setThundering(false);
			world.setStorm(false);
			
			message = config.messages.skipping
					.replaceAll("%condition%", condition)
					.replaceAll("%world%", fancyName);
		}
		
		BaseComponent[] components = TextComponent.fromLegacyText(Text.format().colorize(message));
		for(Player p : world.getPlayers())
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
	}
}
