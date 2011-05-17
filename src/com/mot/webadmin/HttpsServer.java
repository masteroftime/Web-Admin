package com.mot.webadmin;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class HttpsServer extends Thread
{
	private static final String keystore = "plugins/Web Admin/store.ks";
	private static final String passwd = "keypwd";
	private static final int port = 443;
	
	public HttpsServer()
	{
		super("Https Server");
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

			while(!WebAdmin.exit)
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
