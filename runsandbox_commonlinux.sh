docker run -d \
-p 9090:8080 \
--name sandbox_commonlinux \
--restart=unless-stopped \
sandbox_commonlinux:0.1
