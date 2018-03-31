set -x 

docker pull docker.io/stilliard/pure-ftpd
#CHANGE THE IP Address
docker run -d --name ftpd_server -p 21:21 -p 30000-30009:30000-30009 -e "PUBLICHOST=192.168.0.155" stilliard/pure-ftpd
docker exec -it ftpd_server /bin/bash
# run following
# pure-pw useradd vodafone -f /etc/pure-ftpd/passwd/pureftpd.passwd -m -u ftpuser -d /home/ftpusers/vodafone

# ftp -v 192.168.0.155
# from inside the pod curl ftp://vodafone:password@192.168.0.155
