path "C:\Program Files\Java\jre6\bin"
keytool -genkey -alias serverKey -keyalg RSA -keystore store.ks
keytool -export -alias serverKey -file server.crt -keystore store.ks