package com.mot.webadmin;

import java.lang.reflect.Field;

import net.minecraft.server.MinecraftServer;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

public class WebAdmin extends JavaPlugin
{
	public static WebAdmin plugin;
	public static MinecraftServer mcserver;
	
	private MessageHandler logger;
	private HttpsServer server;
	
	public volatile static boolean exit = false;

	@Override
	public void onDisable() {
		exit = true;
		server.interrupt();
		
		getServer().getLogger().removeHandler(logger);
		
		for(HTTPProcessor p : MessageHandler.waiting)
		{
			p.interrupt();
		}
	}

	@Override
	public void onEnable() {
		try {
			Field cfield = CraftServer.class.getDeclaredField("console");
			cfield.setAccessible(true);
			mcserver = (MinecraftServer) cfield.get((CraftServer)getServer());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger = new MessageHandler();
		getServer().getLogger().addHandler(logger);
		
		plugin = this;
		server = new HttpsServer();
		server.start();
	}
}
