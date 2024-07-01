FROM tomcat:9.0.90-jre8

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy your WAR file
COPY ./target/ServaWeb.war /usr/local/tomcat/webapps/ROOT.war

# Copy the keystore file into the container
COPY keystore.jks /usr/local/tomcat/conf/keystore.jks

# Expose ports
EXPOSE 8443

# Add HTTPS configuration to server.xml
RUN sed -i 's#</Host>#<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol" maxThreads="150" SSLEnabled="true" scheme="https" secure="true" clientAuth="false" sslProtocol="TLS" keystoreFile="/usr/local/tomcat/conf/keystore.jks" keystorePass="your_password"/> </Host>#' /usr/local/tomcat/conf/server.xml

CMD ["catalina.sh", "run"]

