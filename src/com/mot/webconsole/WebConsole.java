package com.mot.webconsole;

import org.bukkit.plugin.java.JavaPlugin;

public class WebConsole extends JavaPlugin
{

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEnable() {
		new HttpsServer().start();
	}

}
