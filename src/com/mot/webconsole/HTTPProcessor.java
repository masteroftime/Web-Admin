package com.mot.webconsole;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.SSLSocket;

public class HTTPProcessor extends Thread
{
	public static String user = "martin";
	public static String password = "hellomc";
	
	public static WebConsole plugin;
	public static HashMap<String, Session> sessions = new HashMap<String, Session>();
	
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	
	private Session session;
	private ArrayList<String> headers;
	private HashMap<String, String> cookies;
	
	public HTTPProcessor(SSLSocket socket)
	{
		try {
			this.socket = socket;
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		process();
		try {
			out.flush();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void process()
	{
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
			
			String request = reader.readLine();
			String[] args = request.split(" ");
			
			headers = new ArrayList<String>();
			
			String input = reader.readLine();
			while(!input.equals(""))
			{
				headers.add(input);
				input = reader.readLine();
			}
			
			cookies = new HashMap<String, String>();
			
			for(String header : headers)
			{
				if(header.startsWith("Cookie:"))
				{
					String[] c = header.substring(8).split("; ");
					for(String x : c)
					{
						String[] y = x.split("=", 2);
						if(y.length < 2)
						{
							break;
						}
						cookies.put(y[0], y[1]);
					}
				}
			}
				
			if(args.length < 3) System.out.println("Less than 3 arguments in request string");

			if(args[0].equals("GET"))
			{
				if(cookies.containsKey("MCSSID"))
				{
					String id = cookies.get("MCSSID");
					session = sessions.get(id);
				}
				
				if(args[1].equals("/"))
				{
					if(session != null)
					{
						writer.write("HTTP/1.1 200 OK");
						writer.newLine();
						writer.write("Content-Type: text/plain");
						writer.newLine();
						writer.newLine();
						writer.flush();
						
						transmitFile("server.log");
					}
					else
					{
						writer.write("HTTP/1.1 200 OK");
						writer.newLine();
						writer.write("Content-Type: text/html");
						writer.newLine();
						writer.newLine();
						writer.flush();

						transmitFile("plugins/WebConsole/login.html");
					}
				}
				else
				{
					writer.write("HTTP/1.1 404 Page not Found");
					writer.newLine();
					writer.write("Content-Type: text/plain");
					writer.newLine();
					writer.newLine();
					writer.write("The page you requested was not found.");
					writer.flush();
				}
			}
			else if(args[0].equals("POST"))
			{				
				String cl = getHeader("Content-Length");
				if(cl == null) return;
				int length = Integer.parseInt(cl);
				
				char[] buffer = new char[length];
				reader.read(buffer, 0, length);
				String[] data = new String(buffer).split("&");
				
				HashMap<String, String> post = new HashMap<String, String>(data.length);
				
				for(String s : data)
				{
					String[] x = s.split("=", 2);
					post.put(x[0], x[1]);
				}
				
				Session s = handleLogin(post.get("username"), post.get("password"));
				
				if(s != null)
				{
					writer.write("HTTP/1.1 200 OK");
					writer.newLine();
					writer.write("Content-Type: text/html");
					writer.newLine();
					writer.flush();
					setCookie("MCSSID", s.getID());
					writer.newLine();
					writer.flush();

					transmitFile("plugins/WebConsole/redirect.html");
				}
				else
				{
					writer.write("HTTP/1.1 200 OK");
					writer.newLine();
					writer.write("Content-Type: text/html");
					writer.newLine();
					writer.newLine();
					writer.flush();

					transmitFile("plugins/WebConsole/login_failed.html");
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void transmitFile(String filename) throws Exception
	{
		FileInputStream fin = new FileInputStream(filename);
		
		byte[] buffer = new byte[1024];
		int n = 0;
		while((n = fin.read(buffer)) != -1)
		{
			out.write(buffer, 0, n);
		}
		fin.close();
	}
	
	public void setCookie(String name, String value)
	{
		try {
			out.write(("Set-Cookie: "+name+"="+value+"\n").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Session handleLogin(String user, String password)
	{
		if(user != null && password != null)
		{
			if(user.equals(HTTPProcessor.user) && password.equals(HTTPProcessor.password))
			{
				Session s = new Session();
				sessions.put(s.getID(), s);
				return s;
			}
		}
		return null;
	}
	
	public String getHeader(String name)
	{
		for(String header : headers)
		{
			if(header.startsWith(name))
			{
				return header.substring(name.length()+2);
			}
		}
		
		return null;
	}
}
