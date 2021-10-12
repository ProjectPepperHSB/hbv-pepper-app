#!/bin/bash
echo im $$
while read line; do
  echo [$line]
  SUBSTR=$( echo $line | cut -c1-9)
  if [ $SUBSTR == "timetable" ]; then
    c=$( echo $line | cut -d'[' -f2 | cut -d',' -f1)
    s=$( echo $line | cut -d'[' -f2 | cut -d',' -f2)
    kw=$( echo $line | cut -d'[' -f2 | cut -d',' -f3 | cut -d']' -f1)
    /usr/bin/python3 ~/websocketStuff/scripts/getTimeTableData.py -c $c -s $s -kw $kw
    #mv ~/timetable_tmp.csv /var/www/html/docker-hbv-kms-web 
    /usr/bin/python3 ~/websocketStuff/scripts/csv2json.py -i /tmp/timetable_tmp.csv
     
    echo "jajaj"
  else
    echo "..."
  fi
  echo $line >> ws.log
done

