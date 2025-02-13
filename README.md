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

## Source dependents
- https://github.com/liuniu1010/servaframe.git
- https://github.com/liuniu1010/servaaibase.git
- https://github.com/liuniu1010/servaaiagent.git
- https://github.com/liuniu1010/servaweb.git

## Local Deploy steps

### step1: Building source code
<!-- setup project folder -->
```shell
mkdir <YourPreferredprojectFolder>
```

<!-- build servaframe -->
```shell
cd <YourPreferredProjectFolder>
git clone https://github.com/liuniu1010/servaframe.git
cd servaframe
mvn clean install -DskipTests
```

<!-- build servaaibase -->
```shell
cd <YourPreferredProjectFolder>
git clone https://github.com/liuniu1010/servaaibase.git
cd servaaibase
mvn clean install -DskipTests
```

<!-- build servaaiagent -->
```shell
cd <YourPreferredProjectFolder>
git clone https://github.com/liuniu1010/servaaiagent.git
cd servaaiagent
mvn clean install -DskipTests
```

<!-- build servaweb -->
```shell
cd <YourPreferredProjectFolder>
git clone https://github.com/liuniu1010/servaweb.git
cd servaweb
mvn clean package -DskipTests
```

### step2: Install and start DB
<!--
edit initservamysql57.sql
setup your own OpenAI api key, google api key and email configurations in the script
-->
- ./initservamysql57.sql

<!--
edit runservermysql57.sh
update <dbip> to the ip of the database, update <pathonhost> to your preferred data folder on the host
-->
- ./runservamysql57.sh

<!-- build and start db image -->
```shell
./buildservamysql57.sh
./runservamysql57.sh
```

### step3: Edit configuration files
<!-- 
   edit database.conf to point to the database
-->
- ./src/main/resources/database.conf

<!--
    edit WhiteListAdmin.txt to set the administrators
-->
- ./src/main/resources/WhiteListAdmin.txt

<!--
    edit runimage_local.sh, modify <dbIP> to the database ip
-->
- ./runimage_local.sh

<!-- build servaweb -->
```shell
mvn clean package -DskipTests
```

<!-- build local image  and common sandbox imags -->
```shell
./buildimage_local.sh
./buildsandbox_commonlinux.sh
```

<!-- start all images -->
```shell
./runimage_local.sh
./runsandbox_commonlinux.sh
```

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
