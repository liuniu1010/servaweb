docker run -d \
-p 8080:8080 \
--add-host=mydb:<dbip> \
--name servaweb_local \
--restart=unless-stopped \
servaweb_local:0.1
