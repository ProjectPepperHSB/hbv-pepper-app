from bs4 import BeautifulSoup
import requests
import pandas as pd

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

def getCourse(name):
    url = "https://www4.hs-bremerhaven.de/fb2/ws2122.php?action=showfb&fb=%23SPLUS938DBF"
    res = requests.get(url).content
    selection = BeautifulSoup(res, 'html.parser').find("select", attrs={"name":"identifier"})

    courseId = None
    for row in selection.find_all("option"):
        if row.get_text() == name:
            courseId = row.get("value")

    return courseId


FACH = "Wirtschaftsinformatik"
SEMESTER = "1"
courseId = getCourse(courses[FACH] + "_B" + SEMESTER)
KW = "42"
url = "https://www4.hs-bremerhaven.de/fb2/ws2122.php?action=showplan&weeks=" + KW + "&fb=%23SPLUS938DBF&idtype=&listtype=Text-Listen&template=Set&objectclass=Studenten-Sets&identifier=" + courseId + "&days=1;2;3;4;5&tabstart=41"
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
df.to_csv("testData.csv", index=False, sep=";")









#----- EOF -----
