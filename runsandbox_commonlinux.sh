docker run -d \
-p <sandboxip>:9090:8080 \
--name sandbox_commonlinux \
--restart=unless-stopped \
sandbox_commonlinux:0.1
