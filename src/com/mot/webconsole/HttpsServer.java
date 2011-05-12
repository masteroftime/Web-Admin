package com.mot.webconsole;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class HttpsServer extends Thread
{
	private static final String keystore = "plugins/WebConsole/store.ks";
	private static final String passwd = "keypwd";
	private static final int port = 443;
	
	private WebConsole plugin;
	
	public HttpsServer(WebConsole plugin)
	{
		
	}

	@Override
	public void run() 
	{
		try {

			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(keystore), passwd.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, passwd.toCharArray());

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(kmf.getKeyManagers(), null, null);

			SSLServerSocketFactory ssf = context.getServerSocketFactory();
			SSLServerSocket server = 
				(SSLServerSocket) ssf.createServerSocket(port);

			while(true)
			{
				SSLSocket socket = 
					(SSLSocket) server.accept();

				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();
				processHTTP(in, out);
				socket.close();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processHTTP(InputStream in, OutputStream out)
	{
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
			
			String request = reader.readLine();
			String[] args = request.split(" ");

			if(args.length < 3) System.out.println("Less than 3 arguments in request string");

			if(args[0].equals("GET"))
			{
				if(args[1].equals("/"))
				{
					//Header
					writer.write("HTTP/1.1 200 OK");
					writer.newLine();
					writer.write("Content-Type: text/html");
					writer.newLine();
					writer.newLine();
					writer.flush();
					
					transmitFile(out, "login.html");
				}
				else if(args[1].equals("/log"))
				{
					//Header
					writer.write("HTTP/1.1 200 OK");
					writer.newLine();
					writer.write("Content-Type: text/html");
					writer.newLine();
					writer.newLine();
					
					writer.write("<html><head><title>MC Web Console - Log</title></head>");
					writer.write("<body>");
					
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void transmitFile(OutputStream out, String filename) throws Exception
	{
		FileInputStream fin = new FileInputStream("plugins/WebConsole/"+filename);
		
		byte[] buffer = new byte[1024];
		int n = 0;
		while((n = fin.read(buffer)) != -1)
		{
			out.write(buffer, 0, n);
		}
		fin.close();
	}
}
