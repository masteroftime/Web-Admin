   MINECRAFT WEB ADMIN
=========================

MC Web Admin allows you to connect to a bukkit server with your web browser via HTTP and then issue server commands.

 INSTALLATION
--------------

1) Copy the WebAdmin.jar and WebAdmin directory into the plugins diretory of your bukkit server.

2) Open up command prompt and navigate to the WebAdmin folder.

3) Include the bin directory of your java installation to your PATH

   Windows: path "C:\Program Files\Java\jre6\bin"
      You may have to modify the path depending on the installed java version, or where you have installed java

4) Create a keystore and a key for the https server with the following command

   keytool -genkey -alias serverKey -keyalg RSA -keystore store.ks
   
   You can export a certificate for your server and install it in your browser to remove the security warning messages.

   keytool -export -alias serverKey -file server.crt -keystore store.ks

5) Set the keystore password in the settings file and adjust the server settings.
   Do not change the password directly in the config file. If you want to change the password clear the content of the password field or change it on the Web Admin settings page.

6) Start the bukkit server. This will launch your default Browser and display a setup page. Enter a username and password to access Web Admin. If you don't enter a login the page will display again the next time you start bukkit.