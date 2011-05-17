package com.mot.webadmin;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MessageHandler extends Handler
{
	public static ArrayList<String> messages = new ArrayList<String>();
	public static ArrayList<HTTPProcessor> waiting = new ArrayList<HTTPProcessor>();
	
	public static volatile int last = -1;
	
	@Override
	public void close() throws SecurityException {
		
	}

	@Override
	public void flush() {
		
	}

	@Override
	public void publish(LogRecord record) 
	{
		addRecord(record);
	}

	public static String[] getMessagesSince(int last)
	{		
		String[] erg = new String[messages.size()-(last+1)];
		
		for(int i = 0; i < erg.length; i++)
		{
			//last + 1 is the first new message. +i then loops through the recent messages
			erg[i] = messages.get(last + 1 + i);
		}
		
		return erg;
	}
	
	public static void addRecord(LogRecord record)
	{
		addMessage(recordToMessage(record));
	}
	
	public static void addMessage(String message)
	{
		messages.add("<div class='out'>"+message+"</div>");
		last++;
		
		for(HTTPProcessor p : waiting)
		{
			synchronized (p) {
				p.notify();
			}
		}
	}
	
	public static String recordToMessage(LogRecord record)
	{
		DateFormat format = DateFormat.getTimeInstance();
		return format.format(new Date(record.getMillis()))+"["+record.getLevel().toString()+"]: "+record.getMessage();
	}
}
