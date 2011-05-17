package com.mot.webadmin;

import java.security.SecureRandom;
import java.util.Calendar;

public class Session 
{	
	public static final int duration = 3;
	
	private String id;
	private Calendar expires;
	private int lastMessage;
	
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
	
	public int getLastMessage()
	{
		return lastMessage;
	}
	
	public void setLastMessage(int id)
	{
		this.lastMessage = id;
	}
}
