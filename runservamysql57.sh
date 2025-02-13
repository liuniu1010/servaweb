docker run -d \
-p <dbip>:3306:3306 \
--name servamysql57 \
--restart=unless-stopped \
-v <pathonhost>:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=abcd1234 \
servamysql:5.7
