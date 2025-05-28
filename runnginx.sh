docker run -d \
-p 443:443 \
--name nginx_web \
--restart=unless-stopped \
-v <sslpathonhost>:/etc/nginx/ssl:ro \
nginx_web:0.1
