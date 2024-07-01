FROM tomcat:9.0.90-jre8

RUN rm -rf /usr/local/tomcat/webapps/*

COPY ./target/ServaWeb.war /usr/local/tomcat/webapps/ROOT.war

COPY keystore.jks /usr/local/tomcat/conf/keystore.jks

EXPOSE 8443

CMD ["catalina.sh", "run"]

