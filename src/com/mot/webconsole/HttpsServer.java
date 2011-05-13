package com.mot.webconsole;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

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
		this.plugin = plugin;
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

				new HTTPProcessor(socket).start();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
