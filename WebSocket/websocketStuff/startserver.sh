#!/bin/bash
nohup websocketd --port 7000 ~/websocketStuff/backend.sh &>~/websocketStuff/ws.log &
