package com.chromaclypse.sleeplite;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.chromaclypse.api.menu.Menu;
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
		
		getCommand("sleeplite").setExecutor(this);
		getCommand("sleep").setExecutor(this);
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

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName() == "sleep") {
			if(sender instanceof Player) {
				Player player = (Player) sender;
				
				if(player.isSleeping()) {
					WorldData worldData = data.get(player.getWorld().getName());
					
					skipControl.checkSkip(worldData);
				}
				else {
					player.sendMessage(Text.format().colorize("&cYou are not sleeping!"));
				}
			}
			else {
				sender.sendMessage(Text.format().colorize("&cYou are not a player!"));
			}
			
			return true;
		}
		
		if(args.length > 0) {
			String arg1 = args[0].toLowerCase();
			if(arg1.equals("reload")) {
				config.init(this);
				return true;
			}
			else if(arg1.equals("menu")) {
				Menu menu = new Menu(3, "Menu test");
				
				menu.put(0, new ItemStack(Material.STONE), event -> {
					event.getWhoClicked().sendMessage("Test");
				});

				((Player)sender).openInventory(menu.getInventory());
			}
			/*else if(arg1.equals("sleep")) {
				String arg2 = args[1].toLowerCase();
				Long old = skipControl.fakePlayers.get(arg2);
				if(old == null || old.longValue() != -1L)
					return true;
				
				skipControl.fakePlayers.put(arg2, ticks);
				data.get("world").enter(arg2);
			}
			else if(arg1.equals("wake")) {
				String arg2 = args[1].toLowerCase();
				Long old = skipControl.fakePlayers.get(arg2);
				if(old == null || old.longValue() == -1L)
					return true;
				
				skipControl.fakePlayers.put(arg2, -1L);
				data.get("world").exit(arg2, ticks - old.longValue());
				
			}
			else if(arg1.equals("add")) {
				String arg2 = args[1].toLowerCase();
				skipControl.fakePlayers.put(arg2, -1L);
			}
			else if(arg1.equals("rem")) {
				String arg2 = args[1].toLowerCase();
				skipControl.fakePlayers.remove(arg2);
			}
			else if(arg1.equals("list")) {
				sender.sendMessage( skipControl.fakePlayers.keySet().toString());
			}*/
			else
				sender.sendMessage("Unknown "+arg1);
			
			return true;
		}
		
		return false;
	}
}
