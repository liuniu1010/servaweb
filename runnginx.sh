docker run -d \
-p 443:443 \
--name nginx_ssl \
--restart=unless-stopped \
-v <conffileonhost>:/etc/nginx/nginx.conf:ro \
-v <sslpathonhost>:/etc/nginx/ssl:ro \
nginx_ssl:0.1
