from bs4 import BeautifulSoup
import requests
import pandas as pd
from argparse import ArgumentParser

# ------ ----- ----- ----- ------ ------ ----- ------ ----- ----- ------
# usage:  python getTimeTableData.py -c Wirtschaftsinformatik -s 1 -kw 42
parser = ArgumentParser()
parser.add_argument("-c", "--course", dest="FACH", help="Fach")
parser.add_argument("-s", "--semester", dest="SEMESTER", help="Semester")
parser.add_argument("-kw", "--kw", dest="KW", help="Kalenderwoche")

args = parser.parse_args()

courses = {
    "Wirtschaftsinformatik": "WI",
    "Informatik": "INF",
    "Betriebswirtschaftslehre": "BWL",
    "Crouse Tourism Management": "CTM",
    "": "DIIM",
    "Erasmus": "Erasmus",
    "Digitale Medien Produktion": "DMP",
    "": "GIF",
    "": "ISSM",
    "": "LEM",
    "": "TWL",
}

# ------ ----- ----- ----- ------ ------ ----- ------ ----- ----- ------

def getCourseId(name):
    url = "https://www4.hs-bremerhaven.de/fb2/ws2122.php?action=showfb&fb=%23SPLUS938DBF"
    res = requests.get(url).content
    selection = BeautifulSoup(res, 'html.parser').find("select", attrs={"name":"identifier"})

    courseId = None
    for row in selection.find_all("option"):
        if row.get_text() == name:
            courseId = row.get("value")

    return courseId

def main():
    course = courses[args.FACH] + "_B" + args.SEMESTER
    courseId = getCourseId(course)
    url = "https://www4.hs-bremerhaven.de/fb2/ws2122.php?action=showplan&weeks=" + args.KW + "&fb=%23SPLUS938DBF&idtype=&listtype=Text-Listen&template=Set&objectclass=Studenten-Sets&identifier=" + courseId + "&days=1;2;3;4;5&tabstart=41"
    response = requests.post(url)
    html = response.content

    table = BeautifulSoup(html, 'html.parser').find("table", attrs={"class":"spreadsheet"})
    data, headings = [], None
    for i, row in enumerate(table.find_all("tr")):
        if i == 0:
            headings = [td.get_text() for td in row.find_all("td")]
        else:
            values = [td.get_text() for td in row.find_all("td")]
            data.append(values)

    data_ = {}
    for i, headline in enumerate(headings):
        data_[headline] = [x[i] for x in data]

    df = pd.DataFrame.from_dict(data_)
    df.to_csv("timetable_"+ course +".csv", index=False, sep=";")

# ------ ----- ----- ----- ------ ------ ----- ------ ----- ----- ------

if __name__ == "__main__":
    main()





#----- EOF -----
