FROM tomcat:9.0.64-jre8

RUN rm -rf /usr/local/tomcat/webapps/*

COPY ./target/ServaWeb.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]

