package com.mot.webadmin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.ICommandListener;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;

public class HTTPProcessor extends Thread implements CommandSender, ICommandListener
{
	public static String user = "martin";
	public static byte[] password;

	public static String setupID;

	public static HashMap<String, Session> sessions = new HashMap<String, Session>();

	private Socket socket;
	private InputStream in;
	private OutputStream out;

	private boolean secure;

	private Session session;
	private ArrayList<String> headers;
	private HashMap<String, String> cookies;
	private HashMap<String, String> get;

	public HTTPProcessor(Socket socket, boolean secure)
	{
		super("Web Admin");
		try {
			this.socket = socket;
			this.secure = secure;
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

			String request;
			try {
				request = reader.readLine();
			} catch(IOException e) {
				return;
			}

			if(request == null)
			{
				return;
			}

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

			get = new HashMap<String, String>();
			//if request contains ? we store the given parameters in the get map
			if(args[1].contains("?"))
			{
				String s = args[1].split("\\?", 2)[1];
				String[] params = s.split("&");
				for(String p : params)
				{
					String[] val = p.split("=", 2);
					get.put(val[0], val[1]);
				}
			}

			if(cookies.containsKey("MCSSID"))
			{
				String id = cookies.get("MCSSID");
				session = sessions.get(id);
			}

			if(secure)
			{
				if(args[0].equals("GET"))
				{				
					if(session != null)
					{
						if(args[1].equals("/log"))
						{
							sendFile(new File("server.log"));
						}
						else if(args[1].startsWith("/cmdline"))
						{
							if(session.getLastMessage() >= MessageHandler.last)
							{
								synchronized (this) {
									MessageHandler.waiting.add(this);
									this.wait();
								}
								if(WebAdmin.exit)
								{
									if(socket.isClosed()) return;
									writer.write("HTTP/1.1 200 OK");
									writer.newLine();
									writer.write("Content-Type: text/html");
									writer.newLine();
									writer.newLine();
									writer.write("<div class='error'>Web Admin has been disabled</div>");
									writer.flush();
									return;
								}
							}

							writer.write("HTTP/1.1 200 OK");
							writer.newLine();
							writer.write("Content-Type: text/html");
							writer.newLine();
							writer.newLine();

							int last = MessageHandler.last;

							for(String msg : MessageHandler.getMessagesSince(session.getLastMessage()))
							{
								writer.write(msg);
								writer.newLine();
							}

							session.setLastMessage(last);
							try {
								writer.flush();
							} catch (SocketException e) {}
						}
						else if(args[1].equals("/logout"))
						{
							sessions.remove(session);

							writer.write("HTTP/1.1 200 OK");
							writer.newLine();
							writer.write("Content-Type: text/html");
							writer.newLine();
							writer.write(deleteCookieString("MCSSID"));
							writer.newLine();
							writer.newLine();
							writer.flush();

							transmitFile("plugins/Web Admin/html/logout.html");
						}
						else if(args[1].equals("/settings"))
						{
							HashMap<String, String> params = new HashMap<String, String>();
							params.put("httpsport", ""+HttpsServer.port);
							params.put("http", HttpServer.active?"checked":"");
							params.put("httpport", ""+HttpServer.port);
							sendParamFile(new File("plugins/Web Admin/html/settings.html"), params);
						}
						else
						{

							File f = new File("plugins/Web Admin/html"+args[1]);

							if(f.exists())
							{
								if(f.isDirectory())
								{
									sendFile(new File(f.getAbsolutePath()+"/index.html"));
								}
								else
								{
									sendFile(f);
								}
							}
							else
							{
								writer.write("HTTP/1.1 404 Page not Found");
								writer.newLine();
								writer.write("Content-Type: text/html");
								writer.newLine();
								writer.newLine();
								writer.flush();

								transmitFile("plugins/Web Admin/html/404.html");
							}
						}
					}
					else if(args[1].startsWith("/style"))
					{
						File f = new File("plugins/Web Admin/html"+args[1]);

						if(f.exists())
						{
							if(f.isDirectory())
							{
								writer.write("HTTP/1.1 404 Page not Found");
								writer.newLine();
								writer.write("Content-Type: text/plain");
								writer.newLine();
								writer.newLine();
								writer.write("The page you requested was not found.");
								writer.flush();
							}
							else
							{
								sendFile(f);
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
					else if(args[1].startsWith("/setup"))
					{
						if(get.get("id") != null && get.get("id").equals(setupID))
						{
							writer.write("HTTP/1.1 200 OK");
							writer.newLine();
							writer.write("Content-Type: text/html");
							writer.newLine();
							writer.write(setCookieString("SETUPID", setupID));
							writer.newLine();
							writer.newLine();
							writer.flush();

							transmitFile("plugins/Web Admin/html/setup.html");
						}
					}
					else
					{
						writer.write("HTTP/1.1 200 OK");
						writer.newLine();
						writer.write("Content-Type: text/html");
						writer.newLine();
						writer.newLine();
						writer.flush();

						transmitFile("plugins/Web Admin/html/login.html");
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

					if(session == null)
					{
						if(args[1].startsWith("/setup"))
						{
							if(cookies.get("SETUPID").equals(HTTPProcessor.setupID))
							{
								String user = post.get("username");
								String pass = post.get("password");
								String pass2 = post.get("retype");
								if(!user.equals("") && !pass.equals(""))
								{
									if(pass.equals(pass2))
									{
										HTTPProcessor.user = user;
										HTTPProcessor.password = WebAdmin.password(pass);
										HTTPProcessor.setupID = null;
										WebAdmin.plugin.saveProperties();

										sendFile(new File("plugins/Web Admin/html/setup_success.html"));
									}
									else
									{
										sendParamFile(new File("plugins/Web Admin/html/setup_error.html"), "error", "The given passwords do not match. Try again!");
									}
								}
								else
								{
									sendParamFile(new File("plugins/Web Admin/html/setup_error.html"), "error", "Invalid username or password!");
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
						else
						{
							Session s = handleLogin(post.get("username"), post.get("password"));

							if(s != null)
							{
								writer.write("HTTP/1.1 200 OK");
								writer.newLine();
								writer.write("Content-Type: text/html");
								writer.newLine();
								writer.write(setCookieString("MCSSID", s.getID()));
								writer.newLine();
								writer.newLine();
								writer.flush();

								transmitParamFile(new File("plugins/Web Admin/html/redirect.html"), "message", "Login successful!");
							}
							else
							{
								writer.write("HTTP/1.1 200 OK");
								writer.newLine();
								writer.write("Content-Type: text/html");
								writer.newLine();
								writer.newLine();
								writer.flush();

								transmitFile("plugins/Web Admin/html/login_failed.html");
							}
						}
					}
					else
					{
						if(args[1].equals("/command"))
						{
							String command = post.get("command");
							WebAdmin.mcserver.issueCommand(command, this);
						}
						else if(args[1].startsWith("/config"))
						{
							String change = get.get("change");
							if(change != null)
							{
								if(change.equals("user"))
								{
									String user = post.get("username");
									String pass = post.get("password");
									String pass2 = post.get("retype");
									if(!user.equals("") && !pass.equals(""))
									{
										if(pass.equals(pass2))
										{
											HTTPProcessor.user = user;
											HTTPProcessor.password = WebAdmin.password(pass);
											HTTPProcessor.setupID = null;
											WebAdmin.plugin.saveProperties();

											sendFile(new File("plugins/Web Admin/html/settings.html"));
										}
										else
										{
											sendFile(new File("plugins/Web Admin/html/settings.html"));
										}
									}
									else
									{
										sendFile(new File("plugins/Web Admin/html/settings.html"));
									}
								}
								else if(change.equals("webadmin"))
								{
									String https = post.get("httpsport");
									String http = post.get("httpport");
									String on = post.get("usehttp");
									try {
										int httpsport = Integer.parseInt(https);
										int httpport = Integer.parseInt(http);
										boolean active = on.equals("on");
										HttpsServer.port = httpsport;
										HttpServer.port = httpport;
										HttpServer.active = active;
										WebAdmin.plugin.saveProperties();
										sendParamFile(new File("plugins/Web Admin/html/redirect.html"),"message","Saved Settings");
									} catch(NumberFormatException e) {
										sendParamFile(new File("plugins/Web Admin/html/error.html"), "error", "Invlid Number");
									}									
								}
							}
							else
							{
								sendFile(new File("plugins/Web Admin/html/404.html"));
							}
						}
					}
				}
			}
			else
			{
				sendFile(new File("plugins/Web Admin/html/https.html"));
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Sends the file to the client with an appropriate HTTP Header.
	 * @param file The file to transmit
	 * @throws Exception
	 */
	public void sendFile(File file) throws Exception
	{
		if(!file.isDirectory())
		{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

			String[] s = file.getName().split("\\.");
			String extension = s[s.length-1];
			String mime = "text/plain";

			if(extension.equals("html")) mime = "text/html";
			else if(extension.equals("js")) mime = "text/javascript";
			else if(extension.equals("css")) mime = "text/css";
			else if(extension.equals("gif")) mime = "image/gif";
			else if(extension.equals("jpg")) mime = "image/jpeg";
			else if(extension.equals("png")) mime = "image/png";

			writer.write("HTTP/1.1 200 OK");
			writer.newLine();
			writer.write("Content-Type: "+mime);
			writer.newLine();
			writer.newLine();
			writer.flush();

			transmitFile(file);
		}
	}

	/**
	 * Convenience method for sending a parameterized file with only one parameter.
	 * @param file
	 * @param name The name of the parameter
	 * @param value The value of the parameter
	 * @throws Exception
	 */
	public void sendParamFile(File file, String name, String value) throws Exception
	{
		HashMap<String, String> params = new HashMap<String, String>(1);
		params.put(name, value);
		sendParamFile(file, params);
	}

	/**
	 * Sends a parameterized file. Such a file contains [@paramname] which 
	 * are then substituted with the specified parameters in the params map.
	 * @param file
	 * @param params
	 * @throws Exception
	 */
	public void sendParamFile(File file, Map<String, String> params) throws Exception
	{
		if(!file.isDirectory())
		{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

			String[] s = file.getName().split("\\.");
			String extension = s[s.length-1];
			String mime = "text/plain";

			if(extension.equals("html")) mime = "text/html";
			else if(extension.equals("js")) mime = "text/javascript";
			else if(extension.equals("css")) mime = "text/css";
			else if(extension.equals("gif")) mime = "image/gif";
			else if(extension.equals("jpg")) mime = "image/jpeg";
			else if(extension.equals("png")) mime = "image/png";

			writer.write("HTTP/1.1 200 OK");
			writer.newLine();
			writer.write("Content-Type: "+mime);
			writer.newLine();
			writer.newLine();

			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			String newline;
			while((line = reader.readLine()) != null)
			{
				if(line.contains("<!--@"))
				{
					s = line.split("<!--\\@");
					newline = s[0];
					for(int i = 1; i < s.length; i++)
					{
						String[] args = s[1].split("-->", 2);
						if(args.length != 2 || !params.containsKey(args[0])) continue;

						newline += params.get(args[0]) + args[1];
					}
					writer.write(newline);
					writer.newLine();
				}
				else
				{
					writer.write(line);
					writer.newLine();
				}
			}
			writer.flush();
			reader.close();
		}
	}
	
	public void transmitParamFile(File file, String name, String value) throws Exception
	{
		HashMap<String, String> params = new HashMap<String, String>(1);
		params.put(name, value);
		transmitParamFile(file, params);
	}
	
	public void transmitParamFile(File file, Map<String, String> params) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		String newline;
		String[] s;
		while((line = reader.readLine()) != null)
		{
			if(line.contains("<!--@"))
			{
				s = line.split("<!--\\@");
				newline = s[0];
				for(int i = 1; i < s.length; i++)
				{
					String[] args = s[1].split("-->", 2);
					if(args.length != 2 || !params.containsKey(args[0])) continue;

					newline += params.get(args[0]) + args[1];
				}
				writer.write(newline);
				writer.newLine();
			}
			else
			{
				writer.write(line);
				writer.newLine();
			}
		}
		writer.flush();
		reader.close();
	}

	/**
	 * Writes the content of the given file to the client;
	 * @throws Exception
	 */
	public void transmitFile(File file) throws Exception
	{
		FileInputStream fin = new FileInputStream(file);

		byte[] buffer = new byte[1024];
		int n = 0;
		while((n = fin.read(buffer)) != -1)
		{
			out.write(buffer, 0, n);
		}
		fin.close();
	}

	/**
	 * Writes the content of the given file to the client;
	 * @throws Exception
	 */
	public void transmitFile(String filename) throws Exception
	{
		transmitFile(new File(filename));
	}

	/**
	 * creates a string which can be added to the http header to set the cookie
	 * with the given parameters
	 * @param name The name of the cookie
	 * @param value The value of the cookie
	 */
	public String setCookieString(String name, String value)
	{
		return "Set-Cookie: "+name+"="+value+"\n";
	}

	public String deleteCookieString(String name)
	{
		return "Set-Cookie: "+name+"=deleted; expires=Thu, 01-Jan-1970 00:00:01 GMT; httponly; secure;\n";
	}

	public Session handleLogin(String user, String password)
	{
		if(user != null && password != null)
		{
			if(user.equals(HTTPProcessor.user) && WebAdmin.arrayEquals(HTTPProcessor.password,WebAdmin.password(password)))
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

	@Override
	public Server getServer() {
		return null;
	}

	@Override
	public boolean isOp() {
		return true;
	}

	@Override
	public void sendMessage(String msg) 
	{
		MessageHandler.addMessage(msg);
	}
}
