package com.mot.webadmin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Random;
import java.util.logging.Level;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class HttpsServer extends Thread
{
	public static String keystore = "plugins/Web Admin/store.ks";
	public static String passwd = "keypwd";
	public static int port = 443;
	
	public HttpsServer()
	{
		super("Https Server");
	}

	@Override
	public void run()
	{
		try {

			KeyStore ks = KeyStore.getInstance("JKS");
			if(new File(keystore).exists())
			{
				ks.load(new FileInputStream(keystore), passwd.toCharArray());
			}
			else
			{
				System.out.println("You have to create a keystore for using Web Admin");
				return;
			}

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, passwd.toCharArray());

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(kmf.getKeyManagers(), null, null);

			SSLServerSocketFactory ssf = context.getServerSocketFactory();
			SSLServerSocket server = null;
			try {
			server = 
				(SSLServerSocket) ssf.createServerSocket(port);
			} catch(IOException e) {
				port = new Random().nextInt(30000)+20000;
				WebAdmin.log.log(Level.WARNING, "HTTPS port already taken! Chose port "+port);
				server = 
					(SSLServerSocket) ssf.createServerSocket(port);
			}

			while(!WebAdmin.exit)
			{
				SSLSocket socket = 
					(SSLSocket) server.accept();

				new HTTPProcessor(socket, true).start();
			}

		} catch(FileNotFoundException e) {
			WebAdmin.log.log(Level.SEVERE, "HTTPS Keystore not found!");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
