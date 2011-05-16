package com.mot.webadmin;

import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.plugin.java.JavaPlugin;

public class WebAdmin extends JavaPlugin
{
	private LogHandler logger;
	private HttpsServer server;
	
	public volatile static boolean exit = false;

	@Override
	public void onDisable() {
		exit = true;
		server.interrupt();
		
		getServer().getLogger().removeHandler(logger);
		
		for(HTTPProcessor p : LogHandler.waiting)
		{
			p.interrupt();
		}
	}

	@Override
	public void onEnable() {
		logger = new LogHandler();
		getServer().getLogger().addHandler(logger);
		
		HTTPProcessor.plugin = this;
		server = new HttpsServer(this);
		server.start();
	}
}
