package com.mot.webadmin;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

public class HttpServer extends Thread 
{
	public static int port = 80;
	public static boolean active = true;
	
	public HttpServer()
	{
		super("HTTP Server");
	}
	
	@Override
	public void run() {
		try {
			ServerSocket server = new ServerSocket(port);
			
			while(!WebAdmin.exit && active)
			{
				Socket s = server.accept();
				new HTTPProcessor(s, false).process();
				s.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
