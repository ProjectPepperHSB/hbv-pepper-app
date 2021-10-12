import json,csv

from argparse import ArgumentParser

# ------ ----- ----- ----- ------ ------ ----- ------ ----- ----- ------
# usage:  python getTimeTableData.py -c Wirtschaftsinformatik -s 1 -kw 42

parser = ArgumentParser()
parser.add_argument("-i", "--ifile", dest="inputfile", help="inputfile")
args = parser.parse_args()

idata = {}

with open(args.inputfile) as csvFile:
    csvReader = csv.DictReader(csvFile)
    for rows in csvReader:
        id = rows["id"]
        idata[id] = rows

with open("/tmp/timetable.json", "w") as jsonFile:
    jsonFile.wirte(json.dumps(idata)
