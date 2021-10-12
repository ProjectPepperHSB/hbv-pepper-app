#!/bin/bash
scp -r websocketStuff/www/* mydocker:/var/www/html/docker-$USER-web/
scp -r websocketStuff/ mydocker:/home/docker-$USER
ssh mydocker killall websocketd
ssh mydocker /home/docker-$USER/websocketStuff/startserver.sh
