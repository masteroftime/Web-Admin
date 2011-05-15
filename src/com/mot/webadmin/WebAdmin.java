package com.mot.webadmin;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class WebAdmin extends JavaPlugin
{

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEnable() {
		HTTPProcessor.plugin = this;
		new HttpsServer(this).start();
	}
}
