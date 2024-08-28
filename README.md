# servaweb

## Project description
ServaWeb provides Web Application CoderBot, acts as an automated programming machine. with input requirement, it would try to generate source code to meet requirement.

## Demo environment
visit
https://www.neoai.bot/coderbot.html
to try the Auto Coding function

visit 
https://www.neoai.bot/chatwithassistant.html
to chat with assistant

visit
https://www.neoai.bot/chatwithadmin.html
to chat with administrator ( only administrators have the permission to login )

## Environment Suggestion

- **Operating System**: Linux
- **Java Version**: 1.8.0_412 (OpenJDK)
- **Maven Version**: 3.6.3
- **Database**: mysql5.7.26
- **Container**: docker 26.1.0
- **Web Container**: apache-tomcat-9.0.86

## Source dependents
https://github.com/liuniu1010/servaframe.git
https://github.com/liuniu1010/servaaibase.git
https://github.com/liuniu1010/servaaiagent.git
https://github.com/liuniu1010/servaweb.git

## Local Deploy steps

### step1: Install Tomcat
Please refer to https://tomcat.apache.org/tomcat-9.0-doc/index.html for details

### step2: Install Mysql
Please refer to https://dev.mysql.com/doc for details

### step3: Building source code
<!-- setup project folder -->
mkdir <projectFolder>

<!-- build servaframe -->
cd <projectFolder>
git clone https://github.com/liuniu1010/servaframe.git
cd servaframe
mvn clean install -DskipTests

<!-- build servaaibase -->
cd ..
git clone https://github.com/liuniu1010/servaaibase.git
cd servaaibase
mvn clean install -DskipTests

<!-- build servaaiagent -->
cd ..
git clone https://github.com/liuniu1010/servaaiagent.git
cd servaaiagent
mvn clean install -DskipTests

<!-- build servaweb and setup mysql and tomcat -->
cd ..
git clone https://github.com/liuniu1010/servaweb.git
cd servaweb

<!-- 
    edit createDatabase.sql for specified database name and privilege
    edit createDataStructure.sql to setup your own OpenAI api key, google api key and email configuration
    execute the two dbscripts to setup mysql
-->
- ./src/main/resources/dbscripts/mysql/createDatabase.sql
- ./src/main/resources/dbscripts/mysql/createDataStructure.sql

<!-- 
   edit database.conf to point the the database
-->
- ./src/main/resources/database.conf

<!--
    edit WhiteListAdmin.txt to set the administrators
-->
- ./src/main/resources/WhiteListAdmin.txt

<!-- build servaweb -->
mvn clean package -DskipTests

<!-- build local image -->
./buildimage_local.sh

<!-- build local sandbox images -->
./buildsandbox_javamavenlinux.sh
./buildsandbox_javagradlelinux.sh
./buildsandbox_dotnetlinux.sh
./buildsandbox_python3linux.sh
./buildsandbox_nodejslinux.sh
./buildsandbox_bashlinux.sh
./buildsandbox_cmakegcclinux.sh

<!-- start all images -->
./runimage_local.sh
./runsandbox_javamavenlinux.sh
./runsandbox_javagradlelinux.sh
./runsandbox_dotnetlinux.sh
./runsandbox_python3linux.sh
./runsandbox_nodejslinux.sh
./runsandbox_bashlinux.sh
./runsandbox_cmakegcclinux.sh

### step4: Visit Local Deployment
visit
http://localhost:8080/coderbot.html
to try the Auto Coding function

visit 
http://localhost:8080/chatwithassistant.html
to chat with assistant

visit
http://localhost:8080/chatwithadmin.html
to chat with administrator ( only administrators have the permission to login )
