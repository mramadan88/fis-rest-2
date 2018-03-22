set -x 

sudo docker pull docker.io/stilliard/pure-ftpd
sudo docker run -d --name ftpd_server -p 21:21 -p 30000-30009:30000-30009 -e "PUBLICHOST=192.168.5.121" stilliard/pure-ftpd
sudo docker exec -it ftpd_server /bin/bash
# run following
# pure-pw useradd vodafone -f /etc/pure-ftpd/passwd/pureftpd.passwd -m -u ftpuser -d /home/ftpusers/vodafone

# ftp -v 192.1688.0.155
