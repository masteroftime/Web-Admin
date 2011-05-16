package com.mot.webadmin;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class Session 
{	
	public static final int duration = 3;
	
	private String id;
	private Calendar expires;
	private long lastMessage;
	
	public Session()
	{
		expires = Calendar.getInstance();
		expires.add(Calendar.HOUR, duration);
		generateID();
	}
	
	public void generateID()
	{
		SecureRandom r = new SecureRandom();
		char id[] = new char[25];
		
		for(int i = 0; i < 25; i++)
		{
			id[i] = (char)('a' + r.nextInt(26));
		}
		
		this.id = new String(id);
	}
	
	public String getID()
	{
		return this.id;
	}
	
	public long getLastMessage()
	{
		return lastMessage;
	}
	
	public void setLastMessage(long time)
	{
		this.lastMessage = time;
	}
}
