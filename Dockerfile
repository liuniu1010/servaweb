FROM tomcat:9.0.90-jre8

RUN rm -rf /usr/local/tomcat/webapps/*

COPY ./target/ServaWeb.war /usr/local/tomcat/webapps/ROOT.war

COPY fullchain.cer /usr/local/tomcat/conf/
COPY neoai.bot.key /usr/local/tomcat/conf/
COPY ca.cer /usr/local/tomcat/conf/
COPY server.xml /usr/local/tomcat/conf/server.xml

EXPOSE 8443

CMD ["catalina.sh", "run"]

