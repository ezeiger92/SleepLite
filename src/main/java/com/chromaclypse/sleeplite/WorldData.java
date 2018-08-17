package com.chromaclypse.sleeplite;

import java.util.HashMap;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class WorldData implements Listener {
	private static final long DEEP_TICKS = 100L;
	private final World world;
	private HashMap<String, BukkitTask> insomniacs = new HashMap<>();
	private int deepSleep;
	private int lightSleep;
	private Consumer<WorldData> checkSkip;
	
	public WorldData(World world, Consumer<WorldData> checkSkip) {
		this.world = world;
		this.checkSkip = checkSkip;
		initSleepers();
		Bukkit.getPluginManager().registerEvents(this, SleepLite.get());
	}
	
	private void initSleepers() {
		deepSleep = lightSleep = 0;

		for(Player player : world.getPlayers())
			if(player.isSleeping()) {
				++lightSleep;
				if(player.getSleepTicks() >= DEEP_TICKS)
					++deepSleep;
				else
					putInsomniac(player.getName());
			}
		
		if(deepSleep > 0)
			checkSkip.accept(this);
	}
	
	@EventHandler
	public void onWorldUnload(WorldUnloadEvent event) {
		if(event.getWorld() != world)
			return;
		
		reset();
		HandlerList.unregisterAll(this);
	}
	
	public int getSleepers() {
		return deepSleep;
		//return (deepSleep <= 0) ? 0 : lightSleep;
	}
	
	public World getWorld() {
		return world;
	}
	
	private void putInsomniac(String playerName) {
		//Log.info("calling putInsomniac: " + playerName);
		insomniacs.put(playerName, new BukkitRunnable() {
			@Override
			public void run() {
				//Log.info("running delayed: " + playerName);
				
				if(insomniacs.remove(playerName) != null) {
					++deepSleep;
					//Log.info("  has data w\\ deep=" + deepSleep);
					//if(deepSleep == 1)
						checkSkip.accept(WorldData.this);
				}
				//else
				//	Log.info("  empty");
			}
		}.runTaskLater(SleepLite.get(), DEEP_TICKS));
	}
	
	public void enter(String playerName) {
		++lightSleep;

		//Log.info("calling onEnterBed: " + playerName + " deep: " + deepSleep + " sleep: " + lightSleep);
		
		putInsomniac(playerName);
		//if(deepSleep > 0)
		//	checkSkip.accept(this);
	}
	
	@EventHandler
	public void onEnterBed(PlayerBedEnterEvent event) {
		if(event.getPlayer().getWorld() != world)
			return;
		
		enter(event.getPlayer().getName());
	}
	
	public void reset() {
		//Log.info("calling reset");
		for(BukkitTask task : insomniacs.values())
			task.cancel();
		
		insomniacs.clear();
		deepSleep = lightSleep = 0;
	}
	
	public void exit(String playerName, long ticks) {
		int old = deepSleep;
		--lightSleep;
		
		if(ticks >= DEEP_TICKS)
			--deepSleep;
		else {
			BukkitTask insomniac = insomniacs.remove(playerName);
			if(insomniac != null)
				insomniac.cancel();
		}
		
		if(old > 0)
			checkSkip.accept(this);
	}
	
	@EventHandler
	public void onLeaveBed(PlayerBedLeaveEvent event) {
		if(lightSleep <= 0 || event.getPlayer().getWorld() != world)
			return;
		
		exit(event.getPlayer().getName(), event.getPlayer().getSleepTicks());
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		if(event.getFrom() == world) {
			
		}
		else if(event.getPlayer().getWorld() == world) {
			
		}
	}
	
	@EventHandler
	public void onPlayerSpawn(PlayerRespawnEvent event) {
		checkSkip.accept(this);
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		checkSkip.accept(this);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		checkSkip.accept(this);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		checkSkip.accept(this);
	}
	
	@EventHandler
	public void onPlayerKicked(PlayerKickEvent event) {
		checkSkip.accept(this);
	}
}
