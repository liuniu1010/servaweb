docker run -d \
-p 443:8443 \
--add-host=mydb:<dbip> \
--name servaweb \
--restart=unless-stopped \
servaweb:0.1
