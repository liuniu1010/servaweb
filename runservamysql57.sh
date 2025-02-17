docker run -d \
-p <dbip>:3306:3306 \
--name servamysql57 \
--restart=unless-stopped \
-v <datapathonhost>:/var/lib/mysql \
-v <initpathonhost>:/docker-entrypoint-initdb.d \
-e MYSQL_ROOT_PASSWORD=abcd1234 \
servamysql:5.7
