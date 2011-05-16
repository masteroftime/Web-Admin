package com.mot.webadmin;

import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogHandler extends Handler
{
	public static ArrayList<LogRecord> messages = new ArrayList<LogRecord>();
	public static ArrayList<HTTPProcessor> waiting = new ArrayList<HTTPProcessor>();
	
	public static long lastTime = 0;
	
	@Override
	public void close() throws SecurityException {
		
	}

	@Override
	public void flush() {
		
	}

	@Override
	public void publish(LogRecord record) 
	{
		messages.add(record);
		lastTime = record.getMillis();
		
		for(HTTPProcessor p : waiting)
		{
			synchronized (p) {
				p.notify();
			}
		}
	}

	public static LogRecord[] getMessagesSince(long time)
	{
		if(messages.size() == 0) return new LogRecord[0];
		int i;
		for(i = messages.size() - 1; i >= 0 ; i--)
		{
			if(messages.get(i).getMillis() < time || i == 0)
			{
				i++;
				break;
			}
		}
		
		LogRecord[] records = new LogRecord[messages.size()-i];
		
		for(int x = 0; i < messages.size(); x++)
		{
			records[x] = messages.get(i);
			i++;
		}
		
		return records;
	}
}
