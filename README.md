curl -X POST 'http://localhost:8080/realms/kanrealm/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password' \
  -d 'client_id=kanclient' \
  -d 'client_secret=ja1PtGjL4QbMkfVpcgP28z4ohJRRH8QY' \
  -d 'username=testuser1' \
  -d 'password=123456'
  
  
 create a file kanAuth.env
 add credentials and ips
 
  sudo nano /etc/systemd/system/kanauth.service
  
add below in kanauth.service

[Unit]
Description=KanAuth Spring Boot App
After=network.target

[Service]
User=ubuntu
EnvironmentFile=/home/ubuntu/kanAuth.env
ExecStart=/usr/bin/java -jar /home/ubuntu/kanAuth.jar
Restart=always
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target


sudo systemctl daemon-reload
sudo systemctl enable kanauth

