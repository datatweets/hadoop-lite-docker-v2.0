CREATE DATABASE IF NOT EXISTS hue;
GRANT ALL PRIVILEGES ON hue.* TO 'root'@'%' IDENTIFIED BY 'secret';
FLUSH PRIVILEGES;
