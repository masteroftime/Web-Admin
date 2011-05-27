package com.mot.webadmin;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.logging.Logger;

import net.minecraft.server.MinecraftServer;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.util.Base64Coder;

public class WebAdmin extends JavaPlugin
{
	public static WebAdmin plugin;
	public static MinecraftServer mcserver;
	public static Logger log = Logger.getLogger("Minecraft");
	
	private MessageHandler logger;
	private HttpsServer server;
	private HttpServer http;
	
	public volatile static boolean exit = false;

	@Override
	public void onDisable() {
		exit = true;
		server.interrupt();
		http.interrupt();
		
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
		
		loadProperties();
		
		logger = new MessageHandler();
		getServer().getLogger().addHandler(logger);
		
		plugin = this;
		server = new HttpsServer();
		server.start();
		
		if(HttpServer.active)
		{
			http = new HttpServer();
			http.start();
		}
	}
	
	public void loadProperties()
	{
		File p = new File("plugins/Web Admin/settings.properties");
		
		try {
			if(p.exists())
			{
				Properties prop = new Properties();
				prop.load(new FileInputStream(p));
				HttpsServer.port = Integer.parseInt(prop.getProperty("https-port"));
				HttpsServer.keystore = prop.getProperty("keystore");
				HttpsServer.passwd = prop.getProperty("keystore-pass");
				HttpServer.port = Integer.parseInt(prop.getProperty("http-port"));
				HttpServer.active = Boolean.parseBoolean(prop.getProperty("use-http"));
				if(prop.getProperty("username").equals("") || prop.getProperty("password").equals(""))
				{
					SecureRandom r = new SecureRandom();
					char id[] = new char[25];
					
					for(int i = 0; i < 25; i++)
					{
						id[i] = (char)('a' + r.nextInt(26));
					}
					
					HTTPProcessor.setupID = new String(id);
					
					if(Desktop.isDesktopSupported())
					{
						Desktop d = Desktop.getDesktop();
						if(d.isSupported(Action.BROWSE))
						{
							d.browse(new URI("https://localhost/setup?id="+HTTPProcessor.setupID));
						}
						else System.out.println("Could not launch Browser! Please type the following address into your browser: https://localhost/setup?"+HTTPProcessor.setupID);
					}
					else System.out.println("Could not launch Browser! Please type the following address into your browser: https://localhost/setup?"+HTTPProcessor.setupID);;
				}
				else
				{
					HTTPProcessor.user = prop.getProperty("username");
					HTTPProcessor.password = Base64Coder.decode(
							prop.getProperty("password").toCharArray());
				}
			}
			else
			{
				Properties prop = new Properties();
				prop.setProperty("https-port", "443");
				prop.setProperty("keystore", "plugins/Web Admin/store.ks");
				prop.setProperty("keystore-pass", "");
				prop.setProperty("username", "");
				prop.setProperty("password", "");
				prop.setProperty("http-port", "80");
				prop.setProperty("use-http", "true");
				try {
					prop.store(new FileOutputStream("plugins/Web Admin/settings.properties"), "Web Admin Configuration file. Do not set the password here. If you want to change it leave it empty.");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("Please edit the Web Admin configuration and restart the server.");
				getPluginLoader().disablePlugin(this);
			}
		} catch (Exception e) {
			
		}
	}
	
	public void saveProperties()
	{
		Properties prop = new Properties();
		prop.setProperty("https-port", ""+HttpsServer.port);
		prop.setProperty("keystore", HttpsServer.keystore);
		prop.setProperty("keystore-pass", HttpsServer.passwd);
		prop.setProperty("username", HTTPProcessor.user);
		prop.setProperty("password", new String(Base64Coder.encode(HTTPProcessor.password)));
		prop.setProperty("http-port", ""+HttpServer.port);
		prop.setProperty("use-http", ""+HttpServer.active);
		try {
			prop.store(new FileOutputStream("plugins/Web Admin/settings.properties"), "Web Admin Configuration file");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static byte[] password(String password)
	{
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return md.digest(password.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean arrayEquals(byte[] r1, byte[] r2)
	{
		if(r1.length == r2.length)
		{
			for(int i = 0; i < r1.length; i++)
			{
				if(r1[i] != r2[i]) return false;
			}
			return true;
		}
		else return false;
	}
}
