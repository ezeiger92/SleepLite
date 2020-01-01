package com.chromaclypse.sleeplite;

import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.chromaclypse.api.command.CommandBase;
import com.chromaclypse.api.command.Context;
import com.chromaclypse.api.messages.Text;

public class SleepLite extends JavaPlugin implements Listener {
	private SleepConfig config = new SleepConfig();
	private Control skipControl = new Control(config);
	private HashMap<String, WorldData> data = new HashMap<>();
	
	private static SleepLite instance;
	public static SleepLite get() {
		return instance;
	}
	
	public SleepLite() {
		instance = this;
	}
	
	@Override
	public void onEnable() {
		config.init(this);
		for(World w : getServer().getWorlds())
			data.put(w.getName(), new WorldData(w, skipControl::checkSkip));
		getServer().getPluginManager().registerEvents(this, this);
		
		getCommand("sleeplite").setExecutor(new CommandBase().with().arg("reload").calls(this::reloadCommand).getCommand());
		getCommand("sleep").setExecutor(new CommandBase().calls(this::sleepNag).getCommand());
	}
	
	@Override
	public void onDisable() {
		instance = null;
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		data.put(event.getWorld().getName(), new WorldData(event.getWorld(), skipControl::checkSkip));
	}
	
	@EventHandler
	public void onWorldUnload(WorldUnloadEvent event) {
		data.remove(event.getWorld().getName());
	}
	
	private boolean sleepNag(Context context) {
		Player player = context.Player();
		
		if(player.isSleeping()) {
			WorldData worldData = data.get(player.getWorld().getName());
			
			skipControl.checkSkip(worldData, true);
		}
		else {
			player.sendMessage(Text.format().colorize("&cYou are not sleeping!"));
		}
		
		return true;
	}
	
	private boolean reloadCommand(Context context) {
		context.Sender().sendMessage("Reloading config...");
		config.init(this);
		return true;
	}
}
